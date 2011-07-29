package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager;

import owltools.graph.OWLGraphWrapper;

public abstract class OntologyTaskManager extends GenericTaskManager<OWLGraphWrapper> {

	/**
	 * A task which requires an ontology. 
	 */
	public static interface OntologyTask extends ManagedTask<OWLGraphWrapper>{/* intentionally empty */}

	protected final Ontology ontology;
	
	public OntologyTaskManager(Ontology ontology) {
		super("OntologyTaskManager-"+ontology.getUniqueName());
		this.ontology = ontology;
	}
	
	@Override
	protected OWLGraphWrapper updateManaged(OWLGraphWrapper managed) {
		return createManaged();
	}
	
	@Override
	protected OWLGraphWrapper resetManaged(OWLGraphWrapper managed) {
		return createManaged();
	}

	public Ontology getOntology() {
		return ontology;
	}
	
}
