package org.bbop.termgenie.ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.io.ListHelper;
import org.bbop.termgenie.ontology.entities.OntologyIdInfo;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Configuration for an {@link OntologyIdStore} using a line based
 * configuration. The configuration is expected to be in the following format:
 * <ul>
 * <li>Each line represents one ontology</li>
 * <li>Each line has for fields, separated by tabulators</li>
 * <li>Field 1: ontology name</li>
 * <li>Field 2: id pattern</li>
 * <li>Field 3: ID range start</li>
 * <li>Field 4: ID range end</li>
 * </ul>
 */
@Singleton
public class PlainOntologyIdStoreConfiguration extends ResourceLoader implements
		OntologyIdStoreConfiguration
{

	private static final Logger logger = Logger.getLogger(PlainOntologyIdStoreConfiguration.class);

	private final Map<String, OntologyIdInfo> infos;

	/**
	 * Create a new configuration for an {@link OntologyIdStore}. Load the
	 * configuration from the given resource.
	 * 
	 * @param resource
	 * @param tryLoadAsFiles
	 */
	@Inject
	PlainOntologyIdStoreConfiguration(@Named("PlainOntologyIdStoreConfigurationResource") String resource,
			@Named("TryResourceLoadAsFiles") boolean tryLoadAsFiles)
	{
		super(tryLoadAsFiles);
		infos = new HashMap<String, OntologyIdInfo>();
		InputStream inputStream = null;
		loadResource(inputStream, infos);
	}

	/**
	 * Create a new configuration for an {@link OntologyIdStore}. This
	 * constructor is only used for testing purposes.
	 * 
	 * @param inputStream stream containing the configuration
	 */
	PlainOntologyIdStoreConfiguration(InputStream inputStream) {
		super(false);
		infos = new HashMap<String, OntologyIdInfo>();
		loadResource(inputStream, infos);
	}

	static void loadResource(InputStream inputStream, Map<String, OntologyIdInfo> infos) {
		try {
			int lineCount = 0;
			LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			while (lineIterator.hasNext()) {
				String string = lineIterator.next();
				lineCount += 1;
				if (string.startsWith("#") || string.length() < 7) {
					// ignore comments
					// ignore line with too few chars
					// (4 columns + 3 separators = 7 chars)
					continue;
				}
				List<String> fields = ListHelper.parseString(string, '\t');
				if (fields.size() == 4) {
					String ontologyName = fields.get(0);
					String pattern = fields.get(1);
					int start = Integer.parseInt(fields.get(2));
					int max = Integer.parseInt(fields.get(3));
					if (max <= start) {
						error("Invalid configuration line #" + lineCount + ": the start of the ID range must be smaller than the end: " + desc(ontologyName,
								start,
								max,
								pattern));
					}

					OntologyIdInfo info = infos.get(ontologyName);
					if (info != null) {
						error("Multiple configuration lines for ontology: " + ontologyName + " found at line: " + lineCount);
					}
					else {
						// create a new entry
						info = new OntologyIdInfo();
						info.setCurrent(start);
						info.setMaximum(max);
						info.setOntologyName(ontologyName);
						info.setPattern(pattern);
						infos.put(ontologyName, info);
					}
				}
				else {
					// warn, skip line
					warn("Skipping line #" + lineCount + " in configuration with unexpected number of fields: " + fields.size());
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
	public Map<String, OntologyIdInfo> getInfos() {
		return Collections.unmodifiableMap(infos);
	}

	// --------------------- Helper methods and classes ---------------------

	/**
	 * Create a description string for the given fields.
	 * 
	 * @param name
	 * @param start
	 * @param max
	 * @param pattern
	 * @return description string
	 */
	private static String desc(String name, int start, int max, String pattern) {
		StringBuilder sb = new StringBuilder();
		sb.append("ontology: ");
		sb.append(name);
		sb.append(" start: ");
		sb.append(start);
		sb.append(" max: ");
		sb.append(max);
		if (pattern != null) {
			sb.append(" pattern: ");
			sb.append(pattern);
		}
		return sb.toString();
	}

	/*
	 * helper to increase readability of the code
	 */
	private static void warn(String message) {
		logger.warn(message);
	}

	/*
	 * helper to increase readability of the code
	 */
	private static void error(String message) {
		throw new RuntimeException(message);
	}
}
