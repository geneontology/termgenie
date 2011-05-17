package org.bbop.termgenie.server;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.rules.HardCodedTermGenerationEngine;

/**
 * Wrapper for the implementation of the term generation and its configuration.
 */
public class TermGenerationTool implements TermGenerationEngine {

	private final static TermGenerationTool instance = new TermGenerationTool();
	
	private final TermGenerationEngine engine;

	private TermGenerationTool() {
		super();
		List<Ontology> ontologies = OntologyTools.instance.getAvailableOntologies();
		this.engine = new HardCodedTermGenerationEngine(ontologies);
	}

	public static synchronized TermGenerationTool getInstance() {
		return instance;
	}

	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		return engine.generateTerms(ontology, generationTasks);
	}
}
