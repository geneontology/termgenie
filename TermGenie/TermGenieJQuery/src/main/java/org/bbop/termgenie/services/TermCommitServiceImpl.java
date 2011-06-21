package org.bbop.termgenie.services;

import org.bbop.termgenie.tools.ImplementationFactory;

public class TermCommitServiceImpl implements TermCommitService {

	@Override
	public boolean isValidUser(String username, String password, String ontology) {
		return ImplementationFactory.getUserCredentialValidator().validate(username, password, ontology);
	}
}
