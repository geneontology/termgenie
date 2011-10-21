package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonExportResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermMetaData;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermRelation;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.ontology.obo.OBOWriterTools;
import org.bbop.termgenie.tools.OntologyTools;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NoCommitTermCommitServiceImpl implements TermCommitService {

	protected static final Logger logger = Logger.getLogger(TermCommitService.class);

	private final OntologyTools ontologyTools;

	/**
	 * @param ontologyTools
	 */
	@Inject
	protected NoCommitTermCommitServiceImpl(OntologyTools ontologyTools) {
		super();
		this.ontologyTools = ontologyTools;
	}

	@Override
	public JsonExportResult exportTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontologyName)
	{
		JsonExportResult result = new JsonExportResult();

		OntologyTaskManager manager = getOntologyManager(ontologyName);
		if (manager == null) {
			result.setSuccess(false);
			result.setMessage("Unknown ontology: " + ontologyName);
			return result;
		}
		CreateExportDiffTask task = new CreateExportDiffTask(terms);
		manager.runManagedTask(task);
		if (task.getException() != null) {
			result.setSuccess(false);
			result.setMessage("Could not create OBO export: " + task.getException().getMessage());
			return result;
		}
		if (task.oboDiff == null) {
			result.setSuccess(false);
			result.setMessage("Could not create OBO export: empty result");
			return result;
		}

		result.setSuccess(true);
		result.setFormats(new String[] { "OBO" });
		result.setContents(new String[] { task.oboDiff });
		// TODO add OWL export support
		return result;
	}

	
	private class CreateExportDiffTask extends OntologyTask {

		private final JsonOntologyTerm[] terms;

		private String oboDiff = null;

		public CreateExportDiffTask(JsonOntologyTerm[] terms) {
			this.terms = terms;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			
			OBODoc oboDoc;
			Owl2Obo owl2Obo = new Owl2Obo();
			oboDoc = owl2Obo.convert(managed.getSourceOntology());
			
			for (JsonOntologyTerm term : terms) {
				final Frame frame = new Frame(FrameType.TERM);

				// id
				frame.setId(term.getTempId());

				// name
				addClause(frame, OboFormatTag.TAG_NAME, term.getLabel());

				// definition
				Clause defClause = new Clause(OboFormatTag.TAG_DEF, term.getDefinition());
				frame.addClause(defClause);
				String[] defxRefs = term.getDefxRef();
				if (defxRefs != null && defxRefs.length > 0) {
					for (String defxRef : defxRefs) {
						Xref xref = new Xref(defxRef);
						defClause.addXref(xref);
					}
				}

				// synonyms
				JsonSynonym[] synonyms = term.getSynonyms();
				if (synonyms != null) {
					for (JsonSynonym jsonSynonym : synonyms) {
						Clause synClause = new Clause(OboFormatTag.TAG_SYNONYM, jsonSynonym.getLabel());
						String[] xrefs = jsonSynonym.getXrefs();
						if (xrefs != null) {
							for (String xref : xrefs) {
								synClause.addXref(new Xref(xref));
							}
						}
						String scope = jsonSynonym.getScope();
						if (scope != null) {
							synClause.addValue(scope);
						}
						frame.addClause(synClause);
					}
				}

				// relations
				JsonTermRelation[] relations = term.getRelations();
				if (relations != null) {
					for (JsonTermRelation relation : relations) {
						OBOConverterTools.fillRelation(frame, JsonTermRelation.convert(relation), null);
					}
				}

				// meta data
				JsonTermMetaData metaData = term.getMetaData();
				addClause(frame, OboFormatTag.TAG_COMMENT, metaData.getComment());
				addClause(frame, OboFormatTag.TAG_CREATED_BY, metaData.getCreated_by());
				addClause(frame, OboFormatTag.TAG_CREATION_DATE, metaData.getCreation_date());

				oboDoc.addTermFrame(frame);
			}
			
			List<String> ids = new ArrayList<String>(terms.length);
			for (JsonOntologyTerm term : terms) {
				ids.add(term.getTempId());
			}
			oboDiff = OBOWriterTools.writeTerms(ids, oboDoc);
		}

	}
	
	protected OntologyTaskManager getOntologyManager(String ontologyName) {
		return ontologyTools.getManager(ontologyName);
	}

	private void addClause(Frame frame, OboFormatTag tag, String value) {
		if (value != null && !value.isEmpty()) {
			frame.addClause(new Clause(tag, value));
		}
	}

	@Override
	public JsonCommitResult commitTerms(String sessionId,
			JsonOntologyTerm[] terms,
			String ontology,
			HttpSession session)
	{
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not enabled.");
		return result;
	}

}
