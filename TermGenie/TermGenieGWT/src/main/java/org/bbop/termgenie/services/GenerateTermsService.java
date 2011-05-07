package org.bbop.termgenie.services;

import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.Pair;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("generate")
public interface GenerateTermsService extends RemoteService {

	/**
	 * Retrieve the available term templates parameters for the given ontology;
	 * 
	 * @param ontology
	 * @return gwtTermTemplates
	 */
	public GWTTermTemplate[] getAvailableGWTTermTemplates(String ontology);
	
	/**
	 * Generate terms, and commit it to the ontology
	 * 
	 * @param ontology
	 * @param allParameters
	 * @param commit
	 * @param username
	 * @param password
	 * 
	 * @return if the operation succeeded
	 */
	public boolean generateTerms(String ontology,
			Pair<GWTTermTemplate,GWTTermGenerationParameter>[] allParameters,
			boolean commit, String username, String password);
}
