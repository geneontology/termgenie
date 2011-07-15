package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager;

import owltools.graph.OWLGraphWrapper;

public abstract class OntologyTaskManager extends GenericTaskManager<OWLGraphWrapper> {

	/**
	 * A task which requires an ontology. 
	 */
	public static interface OntologyTask extends ManagedTask<OWLGraphWrapper>{}

	protected final Ontology ontology;
	
	OntologyTaskManager(Ontology ontology) {
		super("OntologyTaskManager-"+ontology.getUniqueName());
		this.ontology = ontology;
	}
	
	@Override
	protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) {
		return createManaged();
	}
	
	public Ontology getOntology() {
		return ontology;
	}
	
}
