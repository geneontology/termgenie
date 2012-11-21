package org.bbop.termgenie.services.freeform;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.OntologyTaskManager;


public interface InternalFreeFormCommitService {

	/**
	 * Commit free form terms to the ontology. Retrieve the username and
	 * password from the session
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param terms
	 * @param manager
	 * @param sendConfirmationEMail
	 * @param tempIdPrefix
	 * @param session 
	 * @param processState
	 * @return {@link JsonCommitResult}
	 */
	public JsonCommitResult commitFreeFormTerms(String sessionId,
			JsonOntologyTerm[] terms,
			OntologyTaskManager manager,
			boolean sendConfirmationEMail,
			String tempIdPrefix,
			HttpSession session,
			ProcessState processState);
}
