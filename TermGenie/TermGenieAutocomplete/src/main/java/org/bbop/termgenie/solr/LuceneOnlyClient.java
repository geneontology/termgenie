package org.bbop.termgenie.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.ontology.OntologyTaskManager;

import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Term suggestor using only an in-memory lucene index.
 */
public class LuceneOnlyClient implements OntologyTermSuggestor {

	private static final Logger logger = Logger.getLogger(LuceneOnlyClient.class);

	private final Map<String, BasicLuceneClient> luceneIndices;

	/**
	 * @param ontologies
	 * @param managers
	 * @param factory
	 */
	public LuceneOnlyClient(Collection<? extends Ontology> ontologies,
			Collection<OntologyTaskManager> managers,
			ReasonerFactory factory)
	{
		super();
		luceneIndices = createIndices(ontologies, managers, factory);
	}

	private static Map<String, BasicLuceneClient> createIndices(Collection<? extends Ontology> ontologies,
			Collection<OntologyTaskManager> managers,
			ReasonerFactory factory)
	{

		Map<String, List<Ontology>> groups = new HashMap<String, List<Ontology>>();
		Map<String, OntologyTaskManager> nameManagers = new HashMap<String, OntologyTaskManager>();
		for (OntologyTaskManager manager : managers) {
			nameManagers.put(manager.getOntology().getUniqueName(), manager);
		}
		for (Ontology ontology : ontologies) {
			String name = ontology.getUniqueName();
			List<Ontology> group = groups.get(name);
			if (group == null) {
				group = new ArrayList<Ontology>();
				groups.put(name, group);
			}
			group.add(ontology);
		}

		Map<String, BasicLuceneClient> indices = new HashMap<String, BasicLuceneClient>();
		for (String name : groups.keySet()) {
			OntologyTaskManager manager = nameManagers.get(name);
			if (manager == null) {
				logger.warn("No OntologyTaskManager found for name: " + name);
			}
			else {
				indices.put(name, BasicLuceneClient.create(groups.get(name), manager, factory));
			}
		}
		return indices;
	}

	@Override
	public List<OntologyTerm<ISynonym, IRelation>> suggestTerms(String query, Ontology ontology, int maxCount) {
		BasicLuceneClient index = luceneIndices.get(ontology.getUniqueName());
		if (index != null) {
			return index.suggestTerms(query, ontology, maxCount);
		}
		return null;
	}
}
