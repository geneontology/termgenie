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
	private final boolean reset;

	/**
	 * @param manager
	 * @param reset
	 */
	public OntologyChangeEvent(OntologyTaskManager manager, boolean reset) {
		super();
		this.manager = manager;
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
		return manager.getOntology();
	}
	
	/**
	 * @return the reset
	 */
	public boolean isReset() {
		return reset;
	}
}
