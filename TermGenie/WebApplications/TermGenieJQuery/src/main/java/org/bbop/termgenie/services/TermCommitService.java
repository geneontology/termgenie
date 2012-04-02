package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonExportResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.json.rpc.server.ProcessStateAware;
import org.json.rpc.server.SessionAware;

public interface TermCommitService {

	/**
	 * Prepare the terms for export.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param terms
	 * @param ontology
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @return {@link JsonExportResult}
	 */
	@SessionAware
	public JsonExportResult exportTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontology,
			HttpSession session);

	/**
	 * Commit the terms to the ontology. Retrieve the username and password from
	 * the session
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param terms
	 * @param ontology
	 * @param session introduced via {@link SessionAware}, do not send in rpc
	 *            call.
	 * @param processState introduced via {@link ProcessStateAware}, do not send in rpc
	 *            call.
	 * @return {@link JsonCommitResult}
	 */
	@SessionAware
	@ProcessStateAware
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontology,
			HttpSession session,
			ProcessState processState);
}
