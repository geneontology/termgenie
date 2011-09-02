package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.tools.OntologyTools;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GoTermCommitServiceImpl extends TermCommitServiceImpl {

	private final Committer committer;

	/**
	 * @param ontologyTools
	 * @param committer
	 */
	@Inject
	protected GoTermCommitServiceImpl(OntologyTools ontologyTools, Committer committer) {
		super(ontologyTools);
		this.committer = committer;
	}

	@Override
	public JsonCommitResult commitTerms(String sessionId, JsonOntologyTerm[] terms, String ontology)
	{
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not enabled.");
		return result;
	}

}
