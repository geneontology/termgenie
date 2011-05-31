package org.bbop.termgenie.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class LocalFileIRIMapper extends ResourceLoader {

	private final static Logger logger = Logger.getLogger(LocalFileIRIMapper.class);
	
	private final Map<String, URL> mappings = new HashMap<String, URL>();

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
	
	public URL getUrl(String url) {
		URL documentIRI = mappings.get(url);
		if (documentIRI == null) {
			logger.info("Unknow IRI: "+url);
			try {
				documentIRI = new URL(url);
			} catch (MalformedURLException exception) {
				throw new RuntimeException(exception);
			}
		}
		return documentIRI;
	}
	
	private void addMapping(String url, String local, String temp) throws IOException {
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
		mappings.put(url, file.toURI().toURL());
	}
	
	public static void main(String[] args) {
		LocalFileIRIMapper mapper = new LocalFileIRIMapper();
		System.out.println(mapper.mappings.size());
	}
}
