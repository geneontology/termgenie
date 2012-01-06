package org.bbop.termgenie.services.resources;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public interface ResourceProviderConfiguration {
	
	public Map<String, String> getResourceLocations();

	public static class EmptyResourceProviderConfiguration implements ResourceProviderConfiguration {
	
		@Override
		public Map<String, String> getResourceLocations() {
			return Collections.emptyMap();
		}
	}

	public static class ConfiguredResourceProvider implements ResourceProviderConfiguration {
	
		private final Map<String, String> locations;
		
		@Inject
		public ConfiguredResourceProvider(@Named("ConfiguredResourceProviderFile") String configFile) {
			super();
			File file = new File(configFile);
			if (!file.exists() || !file.isFile() || !file.canRead()) {
				throw new RuntimeException("Could not load config file: "+configFile);
			}
			Map<String, String> locations = new HashMap<String, String>();
			LineIterator lineIterator = null;
			try {
				lineIterator = FileUtils.lineIterator(file);
				while (lineIterator.hasNext()) {
					String line = lineIterator.next();
					if (line.length() > 1) {
						line = line.trim();
						if (line.charAt(0) == '#') {
							// skip line comments
							continue;
						}
						// split by tab char, expect only one tab!
						int tabPos = line.indexOf('\t');
						
						// check that each string has at least one char
						if (tabPos > 0 && (tabPos + 2) < line.length()) {
							String name = line.substring(0, tabPos).trim();
							String location = line.substring(tabPos + 1).trim();
							locations.put(name, location);
						}
					}
				}
			} catch (IOException exception) {
				throw new RuntimeException("Could not load config file: "+configFile, exception);
			}
			finally {
				LineIterator.closeQuietly(lineIterator);
			}
			this.locations = Collections.unmodifiableMap(locations);
		}
	
	
	
		@Override
		public Map<String, String> getResourceLocations() {
			return locations;
		}
		
	}
}