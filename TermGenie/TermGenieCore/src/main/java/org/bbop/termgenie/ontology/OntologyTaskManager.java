package org.bbop.termgenie.ontology;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bushe.swing.event.EventBus;

import owltools.graph.OWLGraphWrapper;

public abstract class OntologyTaskManager extends GenericTaskManager<OWLGraphWrapper> {

	/**
	 * A task which requires an ontology.
	 */
	public static interface OntologyTask extends ManagedTask<OWLGraphWrapper> {/*
																				 * intentionally
																				 * empty
																				 */}

	protected final Ontology ontology;
	private String ontologyId = null;

	public OntologyTaskManager(Ontology ontology) {
		super("OntologyTaskManager-" + ontology.getUniqueName());
		this.ontology = ontology;
		runManagedTask(new OntologyTask() {

			@Override
			public Modified run(OWLGraphWrapper managed) {
				ontologyId = managed.getOntologyId();
				return Modified.no;
			}
		});
	}

	public String getOntologyId() {
		return ontologyId;
	}

	@Override
	protected void setChanged(boolean reset) {
		EventBus.publish(new OntologyChangeEvent(this, ontology, reset));
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
