package org.bbop.termgenie.tools;

import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Place holder for implementing the commit of new terms for the ontology.
 */
@Singleton
public class OntologyCommitTool {
	
	@Inject
	OntologyCommitTool() {
		super();
	}
	
	/**
	 * @param ontology
	 * @param candidates
	 * 
	 * TODO decide on return value, boolean is too simple.
	 * @return boolean
	 */
	public boolean commitCandidates(Ontology ontology, List<TermGenerationOutput> candidates) {
		throw new RuntimeException("Commit is currently not supported");
	}
}
