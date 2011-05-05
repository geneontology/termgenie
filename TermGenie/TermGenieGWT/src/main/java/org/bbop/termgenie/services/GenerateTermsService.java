package org.bbop.termgenie.services;

import org.bbop.termgenie.shared.GWTTermTemplate;

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
	 * Check if the input data would generate valid terms.
	 * 
	 * @return true if the submitted data is valid
	 */
	public boolean checkTerms();
	
	/**
	 * Generate terms, and commit it to the ontology
	 * 
	 * @return if the operation succeeded
	 */
	public boolean generateTerms();
}
