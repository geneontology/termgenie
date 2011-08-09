package org.bbop.termgenie.ontology.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.io.ListHelper;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DefaultOntologyConfiguration extends ResourceLoader implements OntologyConfiguration {
	
	static final String SETTINGS_FILE = "default-ontology-configuration.settings";

	private final Map<String, ConfiguredOntology> configuration;
	
	@Inject
	DefaultOntologyConfiguration(@Named("DefaultOntologyConfigurationResource") String resource) {
		super();
		configuration = loadOntologyConfiguration(resource);
	}
	
	@Override
	public Map<String, ConfiguredOntology> getOntologyConfigurations() {
		return configuration;
	}

	public static class ConfiguredOntology extends Ontology {
		
		String source = null;
		List<String> supports = null;
		List<String> requires = null;
		
		ConfiguredOntology(String name) {
			super(name, null, null);
		}
		
		void setRoots(List<String> roots) {
			this.roots = roots;
		}
		
		void addSupport(String support) {
			if (support == null) {
				return;
			}
			if (supports == null) {
				supports = new ArrayList<String>(6);
			}
			supports.add(support);
		}
		
		void addRequires(String requires) {
			if (requires == null) {
				return;
			}
			if (this.requires == null) {
				this.requires = new ArrayList<String>(3);
			}
			this.requires.add(requires);
		}

		/**
		 * @return the supports
		 */
		List<String> getSupports() {
			if (supports == null) {
				return Collections.emptyList();
			}
			return supports;
		}


		/**
		 * @return the requires
		 */
		List<String> getRequires() {
			if (requires == null) {
				return Collections.emptyList();
			}
			return requires;
		}
		
		protected ConfiguredOntology createBranch(String subOntologyName, List<String> roots) {
			ConfiguredOntology branch = new ConfiguredOntology(name);
			branch.requires = requires;
			branch.source = source;
			branch.supports = supports;
			branch.setBranch(subOntologyName, roots);
			return branch;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConfiguredOntology [");
			if (name != null) {
				builder.append("name=");
				builder.append(name);
				builder.append(", ");
			}
			if (subOntologyName != null) {
				builder.append("subOntologyName=");
				builder.append(subOntologyName);
				builder.append(", ");
			}
			if (roots != null) {
				builder.append("roots=");
				builder.append(roots);
				builder.append(", ");
			}
			if (source != null) {
				builder.append("source=");
				builder.append(source);
				builder.append(", ");
			}
			if (supports != null) {
				builder.append("supports=");
				builder.append(supports);
				builder.append(", ");
			}
			if (requires != null) {
				builder.append("requires=");
				builder.append(requires);
			}
			builder.append("]");
			return builder.toString();
		}
	}
	
	private Map<String, ConfiguredOntology> loadOntologyConfiguration(String resource) {
		InputStream inputStream = null;
		try {
			inputStream = loadResource(resource);
			LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
			Map<String, ConfiguredOntology> map = new LinkedHashMap<String, ConfiguredOntology>();
			while (lineIterator.hasNext()) {
				String line = lineIterator.next();
				if (line.length() > 0 && !line.startsWith("!")) {
					if(line.startsWith("[Ontology]")) {
						parseOntology(lineIterator, map);
					}
					else if (line.startsWith("[OntologyBranch]")) {
						parseOntologyBranch(lineIterator, map);
					}
				}
			}
			return Collections.unmodifiableMap(map);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	private void parseOntology(LineIterator reader, Map<String, ConfiguredOntology> ontologies) {
		ConfiguredOntology current = null;
		while (reader.hasNext()) {
			String line = reader.next().trim();
			if (line.length() <= 1) {
				if (current != null) {
					ontologies.put(current.getUniqueName(), current);
					current = null;
				}
				// empty lines as end marker
				return;
			}
			if (!line.startsWith("!")) {
				if (line.startsWith("name: ")) {
					current = new ConfiguredOntology(getValue(line, "name: "));
				}
				else if (current != null && line.startsWith("source: ")) {
					current.source = getValue(line, "source: ");
				}
				else if (current != null && line.startsWith("support: ")) {
					current.addSupport(getValue(line, "support: "));
				}
				else if (current != null && line.startsWith("requires: ")) {
					current.addRequires(getValue(line, "requires: "));
				}
				else if (current != null && line.startsWith("roots: ")) {
					current.setRoots(getValues(null, line, "roots: "));
				}
			}
		}
		if (current != null) {
			ontologies.put(current.getUniqueName(), current);
		}
	}
	
	private void parseOntologyBranch(LineIterator reader, Map<String, ConfiguredOntology> ontologies) {
		String name = null;
		String ontology = null;
		List<String> roots = null;
		while (reader.hasNext()) {
			String line = reader.next().trim();
			if (line.length() <= 1) {
				if (name != null && ontology != null && roots != null && !roots.isEmpty()) {
					ConfiguredOntology full = ontologies.get(ontology);
					if (full != null) {
						ontologies.put(name, full.createBranch(name, roots));
					}
				}
				// empty lines as end marker
				return;
			}
			if (!line.startsWith("!")) {
				if (line.startsWith("name: ")) {
					name = getValue(line, "name: ");
				}
				else if (line.startsWith("ontology: ")) {
					ontology = getValue(line, "ontology: ");
				}
				else if (line.startsWith("parent: ")) {
					roots = getValues(roots, line, "parent: ");
				}
			}
		}
	}
	
	private String getValue(String line, String prefix) {
		String value = line.substring(prefix.length());
		int comment = value.indexOf(" !");
		if (comment > 0) {
			value = value.substring(0, comment);
		}
		value = value.trim();
		return value;
	}
	
	private List<String> getValues(List<String> results, String line, String prefix) {
		if (results == null) {
			results = new ArrayList<String>();
		}
		String value = line.substring(prefix.length());
		int comment = value.indexOf(" !");
		if (comment > 0) {
			value = value.substring(0, comment);
		}
		value = value.trim();
		List<String> result = ListHelper.parseString(value, ' ');
		if (result != null && !result.isEmpty()) {
			results.addAll(result);
		}
		return results;
	}
	
	public static void main(String[] args) {
		DefaultOntologyConfiguration c = new DefaultOntologyConfiguration(SETTINGS_FILE);
		Map<String, ConfiguredOntology> ontologies = c.getOntologyConfigurations();
		for (String key : ontologies.keySet()) {
			ConfiguredOntology ontology = ontologies.get(key);
			System.out.print(ontology.getUniqueName());
			if (ontology.getBranch() != null) {
				System.out.print(" - ");
				System.out.print(ontology.getBranch());
				System.out.print(" ! ");
				System.out.print(ontology.getRoots());
			}
			System.out.println();
			System.out.println(ontology.source);
			for (String support : ontology.getSupports()) {
				System.out.println("Support: "+support);
			}
			for (String requires : ontology.getRequires()) {
				System.out.println("Requires: "+requires);
			}
			System.out.println();
		}
	}
}
