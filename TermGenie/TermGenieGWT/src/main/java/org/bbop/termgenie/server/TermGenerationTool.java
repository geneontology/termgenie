package org.bbop.termgenie.server;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

/**
 * Wrapper for the implementation of the term generation and its configuration.
 */
public class TermGenerationTool implements TermGenerationEngine {

	private static volatile TermGenerationTool instance = null;
	
	private final TermGenerationEngine engine;

	private TermGenerationTool() {
		super();
		this.engine = new TermGenerationEngine() {
			
			@Override
			public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public static synchronized TermGenerationTool getInstance() {
		if (instance == null) {
			instance = new TermGenerationTool();
		}
		return instance;
	}

	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		return engine.generateTerms(ontology, generationTasks);
	}
}
