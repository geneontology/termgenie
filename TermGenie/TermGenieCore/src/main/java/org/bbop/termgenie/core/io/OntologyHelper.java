package org.bbop.termgenie.core.io;

import java.util.HashMap;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;

/**
 * Tool for the association of Ontologies to a string.
 * TODO WARNING: This class is still a mock-up.
 *
 */
public class OntologyHelper {

	private static Map<String, Ontology> ontologyMap = new HashMap<String, Ontology>();
	static {
		for(Ontology ontology : DefaultTermTemplates.defaultOntologies) {
			ontologyMap.put(serializeOntology(ontology), ontology);
		}
	}
	
	public static Ontology readOntology(String serializedName) {
		return ontologyMap.get(serializedName);
	}
	
	public static String serializeOntology(Ontology ontology) {
		if (ontology.getBranch() == null) {
			return ontology.getUniqueName();
		}
		return ontology.getUniqueName()+"\t"+ontology.getBranch();
	}
}
