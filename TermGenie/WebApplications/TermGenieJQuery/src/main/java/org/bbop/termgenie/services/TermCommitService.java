package org.bbop.termgenie.services;


import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonExportResult;
import org.bbop.termgenie.data.JsonOntologyTerm;

public interface TermCommitService {

	/**
	 * Prepare the terms for export.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param terms
	 * @param ontology
	 * @return {@link JsonExportResult}
	 */
	public JsonExportResult exportTerms(String sessionId, JsonOntologyTerm[] terms, String ontology);

	/**
	 * Commit the terms to the ontology. Retrieve the username and password from
	 * the session
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param terms
	 * @param ontology
	 * @return {@link JsonCommitResult}
	 */
	public JsonCommitResult commitTerms(String sessionId, JsonOntologyTerm[] terms, String ontology);
}
