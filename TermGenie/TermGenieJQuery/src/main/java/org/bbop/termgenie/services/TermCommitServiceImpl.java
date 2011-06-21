package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.tools.ImplementationFactory;

public class TermCommitServiceImpl implements TermCommitService {

	@Override
	public boolean isValidUser(String username, String password, String ontology) {
		return ImplementationFactory.getUserCredentialValidator().validate(username, password, ontology);
	}

	@Override
	public JsonExportResult exportTerms(JsonOntologyTerm[] terms, String ontology) {
		JsonExportResult result = new JsonExportResult();
		result.setSuccess(false);
		result.setMessage("The export operation is not yet implemented");
		return result;
	}

	@Override
	public JsonCommitResult commitTerms(JsonOntologyTerm[] terms, String ontology, String username,
			String password) {
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not yet implemented.");
		return result;
	}

}
