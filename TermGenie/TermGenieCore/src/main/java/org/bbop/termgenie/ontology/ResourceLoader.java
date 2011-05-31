package org.bbop.termgenie.ontology;

import java.io.InputStream;

public class ResourceLoader {

	public ResourceLoader() {
		super();
	}

	protected InputStream loadResource(String name) {
		if (name == null) {
			throw new RuntimeException("Impossible to load a 'null' resource.");
		}
		InputStream inputStream = loadResourceSimple(name);
		if (inputStream == null) {
			throw new RuntimeException("Could not load resource: "+name);
		}
		return inputStream;
	}

	protected InputStream loadResourceSimple(String name) {
		InputStream inputStream = getClass().getResourceAsStream(name);
		if (inputStream == null) {
			inputStream = ClassLoader.getSystemResourceAsStream(name);
		}
		return inputStream;
	}

}