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
	private final boolean hasRealOntology;
	
	OntologyTaskManager(Ontology ontology, boolean hasRealOntology) {
		super("OntologyTaskManager-"+ontology.getUniqueName());
		this.ontology = ontology;
		this.hasRealOntology = hasRealOntology;
	}
	
	@Override
	protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) {
		return createManaged();
	}
	
	public Ontology getOntology() {
		return ontology;
	}
	
	@Override
	public void runManagedTask(ManagedTask<OWLGraphWrapper> task) {
		if (hasRealOntology) {
			super.runManagedTask(task);
		}
	}

	public boolean hasRealOntology() {
		return hasRealOntology;
	}
}
