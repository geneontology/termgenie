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
	DefaultOntologyConfiguration(@Named("DefaultOntologyConfigurationResource") String resource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles)
	{
		super(tryResourceLoadAsFiles);
		configuration = loadOntologyConfiguration(resource);
	}

	@Override
	public Map<String, ConfiguredOntology> getOntologyConfigurations() {
		return configuration;
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
					if (line.startsWith("[Ontology]")) {
						parseOntology(lineIterator, map);
					}
					else if (line.startsWith("[OntologyBranch]")) {
						parseOntologyBranch(lineIterator, map);
					}
				}
			}
			if (map.isEmpty()) {
				throw new RuntimeException("No ontology configurations found in resource: "+resource);
			}
			return Collections.unmodifiableMap(map);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
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

	private void parseOntologyBranch(LineIterator reader, Map<String, ConfiguredOntology> ontologies)
	{
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
		DefaultOntologyConfiguration c = new DefaultOntologyConfiguration(SETTINGS_FILE, false);
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
				System.out.println("Support: " + support);
			}
			for (String requires : ontology.getRequires()) {
				System.out.println("Requires: " + requires);
			}
			System.out.println();
		}
	}
}
