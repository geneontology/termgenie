package org.bbop.termgenie.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Stub for providing some basic ontology and pattern input.
 */
@Singleton
public class OntologyTools {
	
	private final Map<String, OntologyTaskManager> managerInstances;
	private final Map<String, List<TermTemplate>> templates;
	
	@Inject
	OntologyTools(TermGenerationEngine engine, OntologyLoader loader, OntologyConfiguration configuration) {
		managerInstances = new HashMap<String, OntologyTaskManager>();
		templates = new HashMap<String, List<TermTemplate>>();
		
		for(OntologyTaskManager manager : loader.getOntologies()) {
			if (manager != null) {
				Ontology ontology = manager.getOntology();
				managerInstances.put(ontology.getUniqueName(), manager);
			}
		}
		
		for (TermTemplate template : engine.getAvailableTemplates()) {
			Ontology ontology = template.getCorrespondingOntology();
			String name = getOntologyName(ontology);
			if (name != null) {
				List<TermTemplate> list = templates.get(name);
				if (list == null) {
					list = new ArrayList<TermTemplate>();
					templates.put(name, list);
				}
				list.add(template);
			}
		}
	}
	
	public OntologyTaskManager getManager(String ontology) {
		return managerInstances.get(ontology);
	}

	public String getOntologyName(Ontology ontology) {
		StringBuilder sb = new StringBuilder();
		sb.append(ontology.getUniqueName());
		String branch = ontology.getBranch();
		if (branch != null) {
			sb.append('|');
			sb.append(branch);
		}
		String name = sb.toString();
		return name;
	}
	
	public String[] getAvailableOntologyNames() {
		ArrayList<String> names = new ArrayList<String>(templates.keySet());
		//  sort names: prefer ontologies that have more templates
		// if two ontologies have the same number, use alphabetical sort
		Collections.sort(names, new Comparator<String>() {

			@Override
			public int compare(String s1, String s2) {
				int l1 = templates.get(s1).size();
				int l2 = templates.get(s2).size();
				if (l1 == l2) {
					return s1.compareTo(s2);
				}
				return l2 - l1;
			}
		});
		return names.toArray(new String[names.size()]);
	}
	
	public List<TermTemplate> getTermTemplates(String ontologyName) {
		return templates.get(ontologyName);
	}
}
