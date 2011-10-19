package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Inject;
import com.google.inject.Injector;
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
	final Map<String, Pair<String, String>> lazyMappings = new HashMap<String, Pair<String, String>>();

	/**
	 * Create the mapper with the given config resource. The configuration
	 * format is as follows:
	 * <ul>
	 * <li>Line based format</li>
	 * <li>Comment lines start with '#' character</li>
	 * <li>Three columns per line, tabulartor as separator character:</li>
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
	public synchronized URL mapUrl(String url) {
		URL documentIRI = mappings.get(url);
		if (documentIRI == null) {
			Pair<String, String> pair = lazyMappings.get(url);
			if (pair == null) {
				logger.info("Unknow IRI: " + url);
				try {
					documentIRI = new URL(url);
				} catch (MalformedURLException exception) {
					throw new RuntimeException(exception);
				}
			}
			else {
				try {
					addMapping(url, pair.getOne(), pair.getTwo());
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
				return mapUrl(url);
			}
		}
		return documentIRI;
	}

	private void addLazyMapping(String url, String local, String temp) {
		lazyMappings.put(url, new Pair<String, String>(local, temp));
	}

	private void addMapping(String url, String local, String temp) throws IOException {
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
					// copy to temp location
					FileUtils.copyInputStreamToFile(inputStream, tempFile);
				}
				finally {
					IOUtils.closeQuietly(inputStream);
				}
				file = tempFile;
			}
		}
		mappings.put(url, file.toURI().toURL());
	}

	protected InputStream loadRemote(String url) throws MalformedURLException, IOException {
		InputStream inputStream;
		URL u = new URL(url);
		inputStream = u.openStream();
		return inputStream;
	}

	public static void main(String[] args) {
		Injector injector = TermGenieGuice.createInjector(new DefaultOntologyModule());
		IRIMapper mapper = injector.getInstance(IRIMapper.class);
		System.out.println(((LocalFileIRIMapper) mapper).mappings.size());

		System.out.println(mapper == injector.getInstance(IRIMapper.class));

		URL mapUrl = mapper.mapUrl("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology_xp.obo");
		System.out.println(mapUrl);
	}
}
