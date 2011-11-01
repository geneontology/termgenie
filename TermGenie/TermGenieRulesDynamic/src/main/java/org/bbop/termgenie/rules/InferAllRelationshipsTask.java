package org.bbop.termgenie.rules;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.rules.AbstractTermCreationTools.OWLChangeTracker;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.InferenceBuilder;
import owltools.graph.OWLGraphWrapper;


public class InferAllRelationshipsTask implements ReasonerTask {
	
	private final OWLGraphWrapper ontology;
	private final IRI iri;
	private final OWLChangeTracker changeTracker;
	
	private List<IRelation> relations = Collections.emptyList();
	private Set<OWLClass> equivalentClasses = null;
	private Set<OWLClass> subClasses = null;

	InferAllRelationshipsTask(OWLGraphWrapper ontology, IRI iri, OWLChangeTracker changeTracker) {
		super();
		this.ontology = ontology;
		this.iri = iri;
		this.changeTracker = changeTracker;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		InferenceBuilder inferenceBuilder = new InferenceBuilder(ontology, (OWLReasonerFactory) null, false);
		inferenceBuilder.setReasoner(reasoner);
		List<OWLAxiom> inferences = inferenceBuilder.buildInferences();
		for (OWLAxiom owlAxiom : inferences) {
			AddAxiom addAx = new AddAxiom(ontology.getSourceOntology(), owlAxiom);
			changeTracker.apply(addAx);
		}
		for(OWLAxiom redundant : inferenceBuilder.getRedundantAxioms()) {
			RemoveAxiom addAx = new RemoveAxiom(ontology.getSourceOntology(), redundant);
			changeTracker.apply(addAx);
		}
		OWLClass owlClass = ontology.getOWLClass(iri);
		Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass);
		if (equivalentClasses.getSize() > 1) {
			this.equivalentClasses = equivalentClasses.getEntities();
			this.equivalentClasses.remove(owlClass);
		} else {
			NodeSet<OWLClass> subClasses = reasoner.getSubClasses(owlClass, true);
			if (!subClasses.isEmpty()) {
				this.subClasses = subClasses.getFlattened();
			}
			relations = OwlTranslatorTools.extractRelations(owlClass, ontology);
				
		}
		return Modified.no;
	}

	public List<IRelation> getRelations() {
		return relations;
	}

	/**
	 * @return the equivalentClasses
	 */
	public Set<OWLClass> getEquivalentClasses() {
		return equivalentClasses;
	}
	
	public Set<OWLClass> getSubClasses() {
		return subClasses;
	}
}
