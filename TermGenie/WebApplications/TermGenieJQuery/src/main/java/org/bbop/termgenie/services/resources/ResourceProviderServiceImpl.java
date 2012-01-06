package org.bbop.termgenie.services.resources;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResourceProviderServiceImpl implements ResourceProviderService {

	private static final Logger logger = Logger.getLogger(ResourceProviderServiceImpl.class);
	
	private final Map<String, String> resourceLocations;
	
	/**
	 * @param config
	 */
	@Inject
	public ResourceProviderServiceImpl(ResourceProviderConfiguration config) {
		super();
		this.resourceLocations = config.getResourceLocations();
	}

	@Override
	public String[] getLinesFromResource(String sessionId, String resourceId) {
		String[] result = null;
		String location = resourceLocations.get(resourceId);
		File file = new File(location);
		if (file.exists() && file.isFile() && file.canRead()) {
			try {
				List<String> lines = FileUtils.readLines(file);
				if (lines != null && !lines.isEmpty()) {
					result = lines.toArray(new String[lines.size()]);
				}
			} catch (IOException exception) {
				logger.warn("Could not load resource: "+resourceId+" from location: "+location, exception);
			}
		}
		logger.info("Providing resource: "+resourceId);
		return result;
	}

}
