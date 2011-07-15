package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.ontology.OntologyTaskManager;

public class LuceneOnlyClient implements OntologyTermSuggestor {
	
	private final Map<String, BasicLuceneClient> luceneIndices;

	public LuceneOnlyClient(Collection<OntologyTaskManager> managers) {
		super();
		luceneIndices = createIndices(managers);
	}
	
	private static Map<String, BasicLuceneClient> createIndices(Collection<OntologyTaskManager> ontologies) {
		
		Map<String, List<OntologyTaskManager>> groups = new HashMap<String, List<OntologyTaskManager>>();
		for (OntologyTaskManager ontology : ontologies) {
			String name = ontology.getOntology().getUniqueName();
			List<OntologyTaskManager> group = groups.get(name);
			if (group == null) {
				group = new ArrayList<OntologyTaskManager>();
				groups.put(name, group);
			}
			group.add(ontology);
		}

		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (String name : groups.keySet()) {
			indices.put(name, BasicLuceneClient.create(groups.get(name)));
		}
		return indices;
	}
	
	@Override
	public List<OntologyTerm> suggestTerms(String query, Ontology ontology, int maxCount) {
		BasicLuceneClient index = luceneIndices.get(ontology.getUniqueName());
		if (index != null) {
			return index.suggestTerms(query, ontology, maxCount);
		}
		return null;
	}
}
