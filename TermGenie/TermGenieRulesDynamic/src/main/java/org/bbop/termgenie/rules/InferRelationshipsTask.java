package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

class InferRelationshipsTask implements ReasonerTask {
	
	private final OWLGraphWrapper ontology;
	private final OWLClassExpression cls;
	private final List<Relation> knownRelations;
	
	private List<Relation> relations = null;

	InferRelationshipsTask(OWLGraphWrapper ontology, OWLClassExpression cls, List<Relation> knownRelations) {
		super();
		this.ontology = ontology;
		this.cls = cls;
		this.knownRelations = knownRelations;
	}

	@Override
	public boolean run(OWLReasoner reasoner) {
		// TODO implement
		// infer implied relation ships from cross-product and given relation ships
		
		// only use direct ones
		NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(cls, true);
		String source = ontology.getIdentifier(cls);
		relations = new ArrayList<Relation>();
		for(OWLClass parent : superClasses.getFlattened()) {
			String target = ontology.getIdentifier(parent);
			Map<String, String> properties = new HashMap<String, String>();
			Relation.setType(properties, OboFormatTag.TAG_IS_A);
			getRelations().add(new Relation(source, target, properties));
		}
		
		// have some relations been removed by this?
		// assume no for now
		return false;
	}

	public List<Relation> getRelations() {
		return relations;
	}
}