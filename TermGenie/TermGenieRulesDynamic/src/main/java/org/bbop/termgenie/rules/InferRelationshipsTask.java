package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.IRelation;
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
	private final List<IRelation> knownRelations;
	
	private List<IRelation> relations = null;

	InferRelationshipsTask(OWLGraphWrapper ontology, OWLClassExpression cls, List<IRelation> knownRelations) {
		super();
		this.ontology = ontology;
		this.cls = cls;
		this.knownRelations = knownRelations;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		// TODO implement test
		// infer implied relation ships from cross-product and given relation ships
		
		NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(cls, true);
		String source = ontology.getIdentifier(cls);
		relations = new ArrayList<IRelation>();
		for(OWLClass parent : superClasses.getFlattened()) {
			String target = ontology.getIdentifier(parent);
			String targetLabel = ontology.getLabel(parent);
			if (targetLabel != null) {
				Map<String, String> properties = new HashMap<String, String>();
				Relation.setType(properties, OboFormatTag.TAG_IS_A);
				relations.add(new Relation(source, target, targetLabel, properties));
			}
		}
		
		return Modified.no;
	}

	public List<IRelation> getRelations() {
		return relations;
	}
}