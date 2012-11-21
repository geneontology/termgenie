package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonExportResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.tools.OntologyTools;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NoCommitTermCommitServiceImpl implements TermCommitService {

	protected static final Logger logger = Logger.getLogger(TermCommitService.class);

	protected final InternalSessionHandler sessionHandler;
	protected final OntologyTools ontologyTools;

	/**
	 * @param ontologyTools
	 * @param sessionHandler
	 */
	@Inject
	protected NoCommitTermCommitServiceImpl(OntologyTools ontologyTools,
			InternalSessionHandler sessionHandler)
	{
		super();
		this.ontologyTools = ontologyTools;
		this.sessionHandler = sessionHandler;
	}

	@Override
	public JsonExportResult exportTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontologyName,
			HttpSession session)
	{
		JsonExportResult result = new JsonExportResult();

		OntologyTaskManager manager = getOntologyManager(ontologyName);
		if (manager == null) {
			result.setSuccess(false);
			result.setMessage("Unknown ontology: " + ontologyName);
			return result;
		}
		UserData userData = sessionHandler.getUserData(session);
		CreateExportDiffTask task = new CreateExportDiffTask(terms, userData);
		try {
			manager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Could not create OBO export due to an invalid ontology state", exception);
			result.setSuccess(false);
			result.setMessage("Could not create OBO export due to an invalid ontology state: " + task.getException().getMessage());
			return result;
		}
		if (task.getException() != null) {
			result.setSuccess(false);
			result.setMessage("Could not create OBO export: " + task.getException().getMessage());
			return result;
		}
		if (task.oboDiffAdd == null) {
			result.setSuccess(false);
			result.setMessage("Could not create OBO export: empty result");
			return result;
		}

		result.setSuccess(true);
		result.addExport("OBO Added Terms", task.oboDiffAdd);
		if (task.oboDiffModified != null) {
			result.addExport("OBO Modified Terms", task.oboDiffModified);
		}
		// TODO add OWL export support
		return result;
	}

	private static class CreateExportDiffTask extends OntologyTask {

		private final JsonOntologyTerm[] terms;
		private final UserData userData;
		
		private String oboDiffAdd = null;
		private String oboDiffModified = null;

		public CreateExportDiffTask(JsonOntologyTerm[] terms, UserData userData) {
			this.terms = terms;
			this.userData = userData;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {

			OBODoc oboDoc;
			Owl2Obo owl2Obo = new Owl2Obo();
			oboDoc = owl2Obo.convert(managed.getSourceOntology());

			List<String> addIds = new ArrayList<String>(terms.length);
			List<String> modIds = new ArrayList<String>();

			for (JsonOntologyTerm term : terms) {
				addIds.add(term.getTempId());

				final Frame frame = JsonOntologyTerm.createFrame(term);
				if (userData != null) {
					final String scmAlias = userData.getScmAlias();
					if (scmAlias != null) {
						OboTools.updateClauseValues(frame, OboFormatTag.TAG_CREATED_BY, scmAlias);
					}
				}
				oboDoc.addTermFrame(frame);

				// changed relations
				OboTools.fillChangedRelations(oboDoc,
						JsonOntologyTerm.createChangedFrames(term.getChanged()),
						modIds);
			}

			oboDiffAdd = OboWriterTools.writeTerms(addIds, oboDoc);
			if (!modIds.isEmpty()) {
				oboDiffModified = OboWriterTools.writeTerms(modIds, oboDoc);
			}
		}
	}

	protected OntologyTaskManager getOntologyManager(String ontologyName) {
		return ontologyTools.getManager(ontologyName);
	}

	@Override
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontology,
			boolean sendConfirmationEMail,
			HttpSession session,
			ProcessState processState)
	{
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not enabled.");
		return result;
	}

}
