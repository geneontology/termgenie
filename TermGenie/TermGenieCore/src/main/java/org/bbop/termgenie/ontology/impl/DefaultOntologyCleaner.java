package org.bbop.termgenie.ontology.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.OntologyCleaner;
import org.bbop.termgenie.ontology.impl.DefaultOntologyCleaner.CleanerConfig.CleanDetails;
import org.bbop.termgenie.tools.ResourceLoader;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DefaultOntologyCleaner extends ResourceLoader implements OntologyCleaner {

	private final static Logger LOGGER = Logger.getLogger(DefaultOntologyCleaner.class);
	final static String SETTINGS_FILE = "default-ontology-cleaner.settings";

	private final Map<String, CleanerConfig> cleanOntologies;

	@Inject
	DefaultOntologyCleaner(@Named("DefaultOntologyCleanerResource") String resource) {
		super();
		InputStream inputStream = loadResource(resource);
		cleanOntologies = CleanerConfig.loadSettings(inputStream);
	}

	@Override
	public void cleanOBOOntology(String ontology, OBODoc obodoc) {
		if (obodoc != null) {
			CleanerConfig cleanerConfig = cleanOntologies.get(ontology);
			if (cleanerConfig != null) {
				LOGGER.info("Cleaning ontology: " + ontology);
				cleanOBO(obodoc, cleanerConfig);
			}
		}
	}

	private void cleanOBO(OBODoc obodoc, CleanerConfig config) {
		cleanFrames(obodoc.getTermFrames(), config.retain_term_clauses);
		cleanFrames(obodoc.getInstanceFrames(), config.retain_instance_clauses);
		cleanFrames(obodoc.getTypedefFrames(), config.retain_typedef_clauses);
	}

	private void cleanFrames(Collection<Frame> frames, Map<String, CleanDetails> retains) {
		for (Frame frame : frames) {
			Collection<Clause> clauses = frame.getClauses();
			Iterator<Clause> iterator = clauses.iterator();
			while (iterator.hasNext()) {
				Clause clause = iterator.next();
				if (!keepClause(clause, retains)) {
					iterator.remove();
				}
				else {
					cleanClause(clause, retains);
				}
			}
		}
	}

	private void cleanClause(Clause clause, Map<String, CleanDetails> retains) {
		String tag = clause.getTag();
		CleanDetails details = retains.get(tag);
		if (details.clearQualifiers) {
			clause.getQualifierValues().clear();
		}
	}

	private boolean keepClause(Clause clause, Map<String, CleanDetails> retains) {
		String tag = clause.getTag();
		CleanDetails details = retains.get(tag);
		if (details == null) {
			return false;
		} else if (details.types.isEmpty()) {
			return true;
		} else {
			String value = clause.getValue().toString();
			return details.types.contains(value);
		}
	}

	static class CleanerConfig {

		public static class CleanDetails {
			Set<String> types = Collections.emptySet();
			boolean clearQualifiers = false;
		}

		Map<String, CleanDetails> retain_term_clauses = new HashMap<String, CleanDetails>();
		Map<String, CleanDetails> retain_instance_clauses = new HashMap<String, CleanDetails>();
		Map<String, CleanDetails> retain_typedef_clauses = new HashMap<String, CleanDetails>();

		static Map<String, CleanerConfig> loadSettings(InputStream inputStream) {
			Map<String, CleanerConfig> settings = new HashMap<String, CleanerConfig>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;

			CleanerConfig currentConfig = null;
			Map<String, CleanDetails> currentMap = null;
			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() < 1 || line.charAt(0) == '!') {
						continue;
					}
					if (line.equals("[Ontology]")) {
						currentConfig = new CleanerConfig();
						currentMap = null;
					} else if (line.startsWith("name:")) {
						if (currentConfig != null) {
							String name = line.substring(5).trim();
							settings.put(name, currentConfig);
						}
					} else if (line.equals("[Term]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_term_clauses;
						}
					} else if (line.equals("[Typdef]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_typedef_clauses;
						}
					} else if (line.equals("[Instance]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_instance_clauses;
						}
					} else {
						if (currentMap != null) {
							parseLine(line, currentMap);
						}
					}
				}
				return settings;
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			} finally {
				try {
					reader.close();
				} catch (IOException exception) {
					LOGGER.error("unable to close reader", exception);
				}
			}
		}

		private static void parseLine(String line, Map<String, CleanDetails> currentMap) {
			int spacePos = findWhitespace(line, 0);
			String tag = null;
			CleanDetails details = new CleanDetails();
			if (spacePos < 0) {
				tag = line;
			} else {
				tag = line.substring(0, spacePos);
				int startListPos = line.indexOf('[', spacePos);
				int endListPos = line.indexOf(']', startListPos);
				if (startListPos > 0) {
					if (endListPos > 0) {
						details.types = parseList(line.substring(startListPos, endListPos + 1));
						spacePos = findWhitespace(line, endListPos);
					} else {
						// irregular line, remove
						tag = null;
						spacePos = -1;
					}
				}
				if (spacePos > 0) {
					String clearQualifiersString = line.substring(spacePos).trim();
					if ("true".equals(clearQualifiersString)) {
						details.clearQualifiers = true;
					}
				}
			}
			if (tag != null) {
				currentMap.put(tag, details);
			}
		}

		private static Set<String> parseList(String s) {
			Set<String> set = new HashSet<String>();
			int prevPos = 1;
			for (int i = prevPos; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == ',' || c == ']') {
					if (i > prevPos) {
						set.add(s.substring(prevPos, i).trim());
					}
					prevPos = i + 1;
				}
			}
			return set;
		}

		private static int findWhitespace(String s, int start) {
			for (int i = start; i < s.length(); i++) {
				if (Character.isWhitespace(s.charAt(i))) {
					return i;
				}
			}
			return -1;
		}
	}
}
