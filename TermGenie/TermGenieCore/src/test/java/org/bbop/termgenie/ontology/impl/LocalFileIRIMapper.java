package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.tools.ResourceLoader;
import org.bbop.termgenie.tools.Triple;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Map IRIs to local file urls. Get the resource from classpath and copy it to a
 * temp location. 
 */
@Singleton
public class LocalFileIRIMapper extends ResourceLoader implements IRIMapper {

	private final static Logger logger = Logger.getLogger(LocalFileIRIMapper.class);
	final static String SETTINGS_FILE = "default-ontology-iri-mapping-localfile.settings";

	private final File cacheFolder;

	final Map<String, URL> mappings = new HashMap<String, URL>();
	final Map<String, Triple<String, String, Boolean>> lazyMappings = new HashMap<String, Triple<String, String, Boolean>>();

	/**
	 * Create the mapper with the given config resource. The configuration
	 * format is as follows:
	 * <ul>
	 * <li>Line based format</li>
	 * <li>Comment lines start with '#' character</li>
	 * <li>Three columns per line, tabulator as separator character:</li>
	 * <ul>
	 * <li>1st column: original IRI</li>
	 * <li>2nd column: local resource in classpath</li>
	 * <li>3rd column: human readable tempfile name</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param resource config file in classpath
	 */
	@Inject
	LocalFileIRIMapper(@Named("LocalFileIRIMapperResource") String resource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles)
	{
		this(resource, tryResourceLoadAsFiles, FileUtils.getTempDirectory());
	}

	LocalFileIRIMapper(String resource, boolean tryResourceLoadAsFiles, File cacheFolder) {
		super(tryResourceLoadAsFiles);
		this.cacheFolder = cacheFolder;
		InputStream inputStream = null;
		try {
			inputStream = loadResource(resource);
			LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			while (lineIterator.hasNext()) {
				String line = lineIterator.next();
				if (line.length() > 0 && !line.startsWith("#")) {
					String[] strings = line.split("\\t");
					if (strings.length == 3) {
						addLazyMapping(strings[0], strings[1], strings[2]);
					}
				}
			}
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		URL url = mapUrl(ontologyIRI.toString());
		try {
			return IRI.create(url);
		} catch (URISyntaxException exception) {
			logger.warn("Could not create IRI from URL: "+url, exception);
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public synchronized URL mapUrl(String url) {
		URL documentIRI = mappings.get(url);
		if (documentIRI == null) {
			Triple<String, String, Boolean> tripple = lazyMappings.get(url);
			if (tripple == null) {
				logger.info("Unknow IRI: " + url);
				try {
					documentIRI = new URL(url);
				} catch (MalformedURLException exception) {
					throw new RuntimeException(exception);
				}
			}
			else {
				try {
					addMapping(url, tripple.getOne(), tripple.getTwo(), tripple.getThree());
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
				return mapUrl(url);
			}
		}
		return documentIRI;
	}

	private void addLazyMapping(String url, String local, String temp) {
		boolean convert = needsObo2Owl(local, temp);
		lazyMappings.put(url, new Triple<String, String, Boolean>(local, temp, convert));
	}
	
	private boolean needsObo2Owl(String local, String temp) {
		String localSuffix = getSuffix(local);
		String tempSuffix = getSuffix(temp);
		return "obo".equals(localSuffix) && "owl".equals(tempSuffix);
	}
	
	private String getSuffix(String file) {
		File f = new File(file);
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0 && (i + 2) < fileName.length()) {
			return fileName.substring(i + 1).toLowerCase();
		}
		return null;
	}

	private void addMapping(String url, String local, String temp, boolean convert) throws IOException {
		File tempFile = new File(cacheFolder, temp);
		File file = tempFile;
		if (!file.exists()) {
			file = new File(local);
			if (!file.exists()) {
				InputStream inputStream = loadResourceSimple(local);
				try {
					if (inputStream == null) {
						inputStream = loadRemote(url);
					}
					if (convert) {
						convertFromObo2Owl(tempFile, inputStream);
					}
					else {
						// copy directly to temporary location
						FileUtils.copyInputStreamToFile(inputStream, tempFile);
					}
				}
				finally {
					IOUtils.closeQuietly(inputStream);
				}
				file = tempFile;
			}
		}
		mappings.put(url, file.toURI().toURL());
	}

	protected void convertFromObo2Owl(File tempFile, InputStream inputStream)
			throws IOException
	{
		File oboTempFile = null;
		try {
			logger.info("Converting OBO to OWL for import of: "+tempFile.getName());
			// copy stream to OBO temporary file
			oboTempFile = File.createTempFile(tempFile.getName(), ".obo");
			FileUtils.copyInputStreamToFile(inputStream, oboTempFile);
			
			// load and convert to OWL
			OBOFormatParser p = new OBOFormatParser();
			OBODoc oboDoc = p.parse(oboTempFile);
			Obo2Owl obo2Owl = new Obo2Owl();
			OWLOntology owlOntology = obo2Owl.convert(oboDoc);
			
			// write OWL to intended tempFile
			OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
			manager.saveOntology(owlOntology, IRI.create(tempFile));
			
		} catch (OWLOntologyCreationException exception) {
			throw new IOException(exception);
		} catch (OWLOntologyStorageException exception) {
			throw new IOException(exception);
		}
		finally {
			FileUtils.deleteQuietly(oboTempFile);
		}
	}

	protected InputStream loadRemote(String url) throws MalformedURLException, IOException {
		URL u = new URL(url);
		InputStream inputStream = u.openStream();
		return inputStream;
	}
}
