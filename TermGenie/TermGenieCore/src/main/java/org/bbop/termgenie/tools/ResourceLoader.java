package org.bbop.termgenie.tools;

import java.io.InputStream;

public class ResourceLoader {

	public ResourceLoader() {
		super();
	}

	protected InputStream loadResource(String name) {
		if (name == null) {
			throw new RuntimeException("Impossible to load a 'null' resource.");
		}
		if (name.isEmpty()) {
			throw new RuntimeException("Impossible to load an empty resource.");
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
		if (inputStream == null) {
			inputStream = ResourceLoader.class.getResourceAsStream(name);
		}
		if (name.charAt(0) != '/') {
			// this is required for the loading of resources in the servlet container.
			return loadResourceSimple("/"+name);
		}
		return inputStream;
	}

}