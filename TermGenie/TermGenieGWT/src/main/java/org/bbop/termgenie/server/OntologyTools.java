package org.bbop.termgenie.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;

/**
 * Stub for providing some basic ontology and pattern input.
 * TODO replace this with calls to the proper load methods: to be implemented 
 */
public class OntologyTools {
	
	static final OntologyTools instance = new OntologyTools();
	
	private final Map<String, Ontology> ontologyInstances;
	private final Map<Ontology, String> reverseOntologyInstances;
	private final Map<String, List<TermTemplate>> templates;
	
	public OntologyTools() {
		ontologyInstances = new HashMap<String, Ontology>();
		reverseOntologyInstances = new HashMap<Ontology, String>();
		templates = new HashMap<String, List<TermTemplate>>();
		
		for(Ontology ontology : DefaultTermTemplates.defaultOntologies) {
			addOntology(ontology);
		}
		for (TermTemplate template : DefaultTermTemplates.defaultTemplates) {
			List<Ontology> ontologies = template.getCorrespondingOntologies();
			for (Ontology ontology : ontologies) {
				String name = getOntologyName(ontology);
				List<TermTemplate> list = templates.get(name);
				if (list == null) {
					list = new ArrayList<TermTemplate>();
					templates.put(name, list);
				}
				list.add(template);
			}
		}
	}
	
	private void addOntology(Ontology ontology) {
		StringBuilder sb = new StringBuilder();
		sb.append(ontology.getUniqueName());
		String branch = ontology.getBranch();
		if (branch != null) {
			sb.append('|');
			sb.append(branch);
		}
		String name = sb.toString();
		ontologyInstances.put(name, ontology);
		reverseOntologyInstances.put(ontology, name);
	}
	
	Ontology getOntology(String ontology) {
		return ontologyInstances.get(ontology);
	}

	String getOntologyName(Ontology ontology) {
		return reverseOntologyInstances.get(ontology);
	}
	
	List<String> getAvailableOntologyNames() {
		return new ArrayList<String>(templates.keySet());
	}
	
	List<TermTemplate> getTermTemplates(String ontologyName) {
		return templates.get(ontologyName);
	}
}
