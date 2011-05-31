package org.bbop.termgenie.core.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.ontology.DefaultOntologyConfiguration;

/**
 * Tool for the association of Ontologies to a string.
 */
public class OntologyHelper {

	private static Map<String, Ontology> ontologyMap = new HashMap<String, Ontology>();
	static {
		for(Ontology ontology : DefaultOntologyConfiguration.getOntologies().values()) {
			ontologyMap.put(serializeOntology(ontology), ontology);
		}
	}
	
	public static List<Ontology> readOntologies(String serializedNames) {
		List<String> ontologies = splitOntologies(serializedNames);
		if (ontologies != null) {
			List<Ontology> result = new ArrayList<Ontology>(ontologies.size());
			for (String serializedName : ontologies) {
				Ontology instance = ontologyMap.get(serializedName);
				if (instance != null) {
					result.add(instance);
				}
			}
			return result;
		}
		return null;
	}
	
	private static final Pattern splitPattern = Pattern.compile("\\|");
	
	private static List<String> splitOntologies(String serializedNames) {
		if (serializedNames == null) {
			return null;
		}
		String[] split = splitPattern.split(serializedNames);
		switch (split.length) {
		case 0:
			return null;
		case 1:
			return Collections.singletonList(split[0]);
		default:
			return Arrays.asList(split);
		}
	}
	
	public static String serializeOntologies(List<Ontology> ontologies) {
		StringBuilder sb = new StringBuilder();
		for (Ontology ontology : ontologies) {
			if (sb.length() > 0) {
				sb.append('|');
			}
			sb.append(serializeOntology(ontology));
		}
		return sb.toString();
	}
	
	
	private static String serializeOntology(Ontology ontology) {
		if (ontology.getBranch() == null) {
			return ontology.getUniqueName();
		}
		return ontology.getUniqueName()+"\t"+ontology.getBranch();
	}
}
