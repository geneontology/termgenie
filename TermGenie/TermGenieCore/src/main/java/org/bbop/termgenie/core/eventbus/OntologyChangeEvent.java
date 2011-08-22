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
	private final boolean reset;

	/**
	 * @param manager
	 * @param ontology
	 * @param reset
	 */
	public OntologyChangeEvent(OntologyTaskManager manager, Ontology ontology, boolean reset) {
		super();
		this.manager = manager;
		this.ontology = ontology;
		this.reset = reset;
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
	
	/**
	 * @return the reset
	 */
	public boolean isReset() {
		return reset;
	}
}
