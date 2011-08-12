package org.bbop.termgenie.core.eventbus;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.ontology.OntologyTaskManager;

public class OntologyChangeEvent {

	static {
		/*
		 * Call the setup in the static block. Guarantees the proper setup of
		 * the event bus.
		 */
		TermGenieEventBus.setup();
	}

	private final OntologyTaskManager manager;
	private final Ontology ontology;

	/**
	 * @param manager
	 * @param ontology
	 */
	public OntologyChangeEvent(OntologyTaskManager manager, Ontology ontology) {
		super();
		this.manager = manager;
		this.ontology = ontology;
	}

	/**
	 * @return the manager
	 */
	public OntologyTaskManager getManager() {
		return manager;
	}

	/**
	 * @return the ontology
	 */
	public Ontology getOntology() {
		return ontology;
	}
}
