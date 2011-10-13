package org.bbop.termgenie.services;

import java.io.BufferedWriter;
import java.io.StringWriter;

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
import org.bbop.termgenie.tools.OntologyTools;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
		if (task.exception != null) {
			result.setSuccess(false);
			result.setMessage("Could not create OBO export: " + task.exception.getMessage());
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

	
	private class CreateExportDiffTask implements OntologyTask {

		private final JsonOntologyTerm[] terms;

		private Throwable exception = null;
		private String oboDiff = null;

		public CreateExportDiffTask(JsonOntologyTerm[] terms) {
			this.terms = terms;
		}

		@Override
		public Modified run(OWLGraphWrapper managed) {
			
			OBODoc oboDoc;
			try {
				Owl2Obo owl2Obo = new Owl2Obo();
				oboDoc = owl2Obo.convert(managed.getSourceOntology());
			} catch (OWLOntologyCreationException exception) {
				this.exception = exception;
				return Modified.no;
			}
			
			for (JsonOntologyTerm term : terms) {
				final Frame frame = new Frame(FrameType.TERM);

				// id
				frame.setId(term.getTempId());

				// name
				addClause(frame, OboFormatTag.TAG_NAME, term.getLabel());

				// definition
				Clause defClause = create(OboFormatTag.TAG_DEF, term.getDefinition());
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
						Clause synClause = create(OboFormatTag.TAG_SYNONYM, jsonSynonym.getLabel());
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

				try {
					oboDoc.addTermFrame(frame);
				} catch (FrameMergeException exception) {
					this.exception = exception;
					return Modified.no;
				}
			}
			
			try {
				OBOFormatWriter oboWriter = new OBOFormatWriter();
				StringWriter stringWriter = new StringWriter();
				BufferedWriter writer = new BufferedWriter(stringWriter);
				for (JsonOntologyTerm term : terms) {
					Frame termFrame = oboDoc.getTermFrame(term.getTempId());
					oboWriter.write(termFrame, writer, oboDoc);
					writer.append('\n');
				}
				writer.close();
				oboDiff = stringWriter.getBuffer().toString();
			} catch (Exception exception) {
				this.exception = exception;
			}
			return Modified.no;
		}

	}
	
	protected OntologyTaskManager getOntologyManager(String ontologyName) {
		return ontologyTools.getManager(ontologyName);
	}

	private void addClause(Frame frame, OboFormatTag tag, String value) {
		if (value != null && !value.isEmpty()) {
			frame.addClause(create(tag, value));
		}
	}

	private Clause create(OboFormatTag tag, String value) {
		return new Clause(tag.toString(), value);
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
