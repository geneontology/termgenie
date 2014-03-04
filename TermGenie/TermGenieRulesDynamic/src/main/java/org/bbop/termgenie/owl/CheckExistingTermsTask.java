package org.bbop.termgenie.owl;

import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.SharedReasoner.ReasonerTask;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;


public class CheckExistingTermsTask implements ReasonerTask {
	
	private final OWLGraphWrapper ontology;
	private final IRI iri;
	private final ProcessState state;
	
	private Set<OWLClass> equivalentClasses = null;

	public CheckExistingTermsTask(OWLGraphWrapper ontology, IRI iri, ProcessState state) {
		super();
		this.ontology = ontology;
		this.iri = iri;
		this.state = state;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		// First check for equivalent classes
		ProcessState.addMessage(state, "Check for existing classes for a given logical definition.");
		OWLClass owlClass = ontology.getOWLClass(iri);
		Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass);
		if (equivalentClasses != null && equivalentClasses.getSize() > 1) {
			this.equivalentClasses = equivalentClasses.getEntities();
			this.equivalentClasses.remove(owlClass);
		}
		
		return Modified.no;
	}

	
	/**
	 * @return the {@link InferredRelations}
	 */
	public final Set<OWLClass> getExisting() {
		return equivalentClasses;
	}

}
