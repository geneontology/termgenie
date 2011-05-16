package org.bbop.termgenie.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

public class LocalFileIRIMapper implements OWLOntologyIRIMapper {

	private final static Logger logger = Logger.getLogger(LocalFileIRIMapper.class);
	
	private final Map<IRI, IRI> iriMappings = new HashMap<IRI, IRI>();

	public LocalFileIRIMapper() {
		try {
			InputStream inputStream = loadResource("LocalFileIRIMapper.mappings");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0 && !line.startsWith("#")) {
					String[] strings = line.split("\\t");
					if (strings.length == 3) {
						addMapping(strings[0], strings[1], strings[2]);
					}
				}
			}
			reader.close();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		IRI documentIRI = iriMappings.get(ontologyIRI);
		if (documentIRI == null) {
			logger.info("Unknow IRI: "+ontologyIRI);
			documentIRI = ontologyIRI;
		}
		return documentIRI;
	}
	
	private void addMapping(String url, String local, String temp) throws IOException {
		IRI urlIRI = IRI.create(url);
		File tempFile = new File(System.getProperty("java.io.tmpdir"), temp);
		File file = tempFile;
		if (!file.exists()) {
			file = new File(local);
			if (!file.exists()) {
				InputStream inputStream = loadResourceSimple(local);
				if (inputStream == null) {
					URL u = new URL(url);
					inputStream = u.openStream();
				}
				// copy to temp location
				tempFile.getParentFile().mkdirs();
				tempFile.createNewFile();
				OutputStream outputStream = new FileOutputStream(tempFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = inputStream.read(buf)) > 0) {
					outputStream.write(buf, 0, len);
				}
				inputStream.close();
				outputStream.close();
				file = tempFile;
			}
		}
		iriMappings.put(urlIRI, IRI.create(file));
	}
	
	private InputStream loadResource(String name) {
		if (name == null) {
			throw new RuntimeException("Impossible to load a 'null' resource.");
		}
		InputStream inputStream = loadResourceSimple(name);
		if (inputStream == null) {
			throw new RuntimeException("Could not load resource: "+name);
		}
		return inputStream;
	}
	
	private InputStream loadResourceSimple(String name) {
		InputStream inputStream = getClass().getResourceAsStream(name);
		if (inputStream == null) {
			inputStream = ClassLoader.getSystemResourceAsStream(name);
		}
		return inputStream;
	}
	
	public static void main(String[] args) {
		LocalFileIRIMapper mapper = new LocalFileIRIMapper();
		System.out.println(mapper.iriMappings.size());
	}
}
