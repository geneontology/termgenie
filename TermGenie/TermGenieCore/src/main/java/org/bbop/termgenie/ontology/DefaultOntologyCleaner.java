package org.bbop.termgenie.ontology;

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
import org.bbop.termgenie.tools.ResourceLoader;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;

public class DefaultOntologyCleaner extends ResourceLoader {

	private final static Logger LOGGER = Logger.getLogger(DefaultOntologyCleaner.class);
	private final static String SETTINGS_FILE = "default-ontology-cleaner.settings";
	private volatile static DefaultOntologyCleaner instance = null;
	
	private final Map<String, CleanerConfig> cleanOntologies;
	
	private DefaultOntologyCleaner() {
		super();
		InputStream inputStream = loadResource(SETTINGS_FILE);
		cleanOntologies = CleanerConfig.loadSettings(inputStream);
	}
	
	private static final synchronized DefaultOntologyCleaner getInstance() {
		if (instance == null) {
			instance = new DefaultOntologyCleaner();
		}
		return instance;
	}
	
	static void cleanOntology(String ontology, OBODoc obodoc) {
		if (obodoc != null) {
			CleanerConfig cleanerConfig = getInstance().cleanOntologies.get(ontology);
			if (cleanerConfig != null) {
				LOGGER.info("Cleaning ontology: " + ontology);
				cleanOBO(obodoc, cleanerConfig);
			}
		}
	}
	
	private static void cleanOBO(OBODoc obodoc, CleanerConfig config) {
		cleanFrames(obodoc.getTermFrames(), config.retain_term_clauses);
		cleanFrames(obodoc.getInstanceFrames(), config.retain_instance_clauses);
		cleanFrames(obodoc.getTypedefFrames(), config.retain_typedef_clauses);
	}
	
	private static void cleanFrames(Collection<Frame> frames, Map<String, Set<String>> retains) {
		for (Frame frame : frames) {
			Collection<Clause> clauses = frame.getClauses();
			Iterator<Clause> iterator = clauses.iterator();
			while (iterator.hasNext()) {
				Clause clause = iterator.next();
				if (!keepClause(clause, retains)) {
					iterator.remove();
				}
			}
		}
	}
	
	private static boolean keepClause(Clause clause, Map<String, Set<String>> retains) {
		String tag = clause.getTag();
		Set<String> set = retains.get(tag);
		if (set == null) {
			return false;
		}
		else if (set.isEmpty()) {
			return true;
		}
		else {
			String value = clause.getValue().toString();
			return set.contains(value);
		}
	}
	
	static class CleanerConfig {
		Map<String, Set<String>> retain_term_clauses = new HashMap<String, Set<String>>();
		Map<String, Set<String>> retain_instance_clauses = new HashMap<String, Set<String>>();
		Map<String, Set<String>> retain_typedef_clauses = new HashMap<String, Set<String>>();
		
		static Map<String, CleanerConfig> loadSettings(InputStream inputStream) {
			Map<String, CleanerConfig> settings = new HashMap<String, CleanerConfig>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			
			CleanerConfig currentConfig = null;
			Map<String, Set<String>> currentMap = null;
			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() < 1 || line.charAt(0) == '!') {
						continue;
					}
					if (line.equals("[Ontology]")) {
						currentConfig = new CleanerConfig();
						currentMap = null;
					}
					else if (line.startsWith("name:")) {
						if (currentConfig != null) {
							String name = line.substring(5).trim();
							settings.put(name, currentConfig);
						}
					}
					else if (line.equals("[Term]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_term_clauses;
						}
					}
					else if (line.equals("[Typdef]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_typedef_clauses;
						}
					}
					else if (line.equals("[Instance]")) {
						if (currentConfig != null) {
							currentMap = currentConfig.retain_instance_clauses;
						}
					}
					else {
						if (currentMap != null) {
							int pos = line.indexOf('[');
							String tag = null;
							Set<String> set = Collections.emptySet();
							if (pos > 0) {
								tag = line.substring(0, pos).trim();
								String list = line.substring(pos);
								// split
								set = new HashSet<String>();
								int prevPos = 1;
								for(int i=prevPos; i<list.length(); i++) {
									char c = list.charAt(i);
									if (c == ',' || c == ']') {
										if (i > prevPos) {
											set.add(list.substring(prevPos, i).trim());
										}
										prevPos = i + 1;
									}
								}
							}
							else if (pos < 0) {
								tag = line;
							}
							if (tag != null) {
								currentMap.put(tag, set);
							}
						}
					}
				}
				return settings;
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
			finally {
				try {
					reader.close();
				} catch (IOException exception) {
					LOGGER.error("unable to close reader", exception);
				}
			}
		}
	}
}
