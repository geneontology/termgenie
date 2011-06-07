package org.bbop.termgenie.tools;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

/**
 * Place holder for implementing the commit of new terms for the ontology.
 */
public class OntologyCommitTool {
	
	private static volatile OntologyCommitTool instance = null;
	
	private OntologyCommitTool() {
		super();
	}
	
	public static synchronized OntologyCommitTool getInstance() {
		if (instance == null) {
			instance = new OntologyCommitTool();
		}
		return instance;
	}

	/**
	 * @param ontology
	 * @param candidates
	 * 
	 * TODO decide on return value, boolean is too simple.
	 */
	public boolean commitCandidates(Ontology ontology, List<TermGenerationOutput> candidates) {
		throw new RuntimeException("Commit is currently not supported");
	}
}
