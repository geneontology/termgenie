package org.bbop.termgenie.owl;

import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.SharedReasoner.ReasonerTask;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

/**
 * {@link ReasonerTask} which uses a reasoner to check for equivalent classes.
 * This job does not assert subClasses or checks for redundant/new relations.
 * This is useful for ontologies which do not rely on asserted links.
 */
public class SimpleRelationHandlingTask implements RelationshipTask {
	
	private final OWLOntology ontology;
	private final OWLGraphWrapper reference;
	private final IRI iri;
	private final ProcessState state;
	
	private InferredRelations result;
	

	public SimpleRelationHandlingTask(OWLOntology ontology, OWLGraphWrapper reference, IRI iri, ProcessState state) {
		super();
		this.ontology = ontology;
		this.reference = reference;
		this.iri = iri;
		this.state = state;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		// First check for equivalent classes
		ProcessState.addMessage(state, "Check for equivalent classes of new term");
		OWLClass owlClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
		Set<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass).getEntitiesMinus(owlClass);
		if (!equivalentClasses.isEmpty()) {
			result = new InferredRelations(equivalentClasses);
			return Modified.no;
		}
		Pair<List<Clause>,Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(owlClass, ontology, reference);
		result = new InferredRelations(pair.getOne(), pair.getTwo(), null);
		return Modified.no;
	}
	
	/**
	 * @return the {@link InferredRelations}
	 */
	@Override
	public final InferredRelations getInferredRelations() {
		return result;
	}
}
