package org.bbop.termgenie.services;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermMetaData;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermRelation;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.tools.OntologyTools;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NoCommitTermCommitServiceImpl implements TermCommitService {

	private static final Logger logger = Logger.getLogger(NoCommitTermCommitServiceImpl.class);

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
		String ontologyIdPrefix = getOntologyIdPrefix(manager);
		int count = 0;
		OBODoc doc = new OBODoc();
		doc.setHeaderFrame(new Frame(FrameType.HEADER));
		for (JsonOntologyTerm term : terms) {
			final Frame frame = new Frame(FrameType.TERM);

			// id
			frame.setId(createFakeId(ontologyIdPrefix, count));

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
				doc.addTermFrame(frame);
			} catch (FrameMergeException exception) {
				result.setSuccess(false);
				result.setMessage("Could not create OBO export: " + exception.getMessage());
				return result;
			}
			count += 1;
		}

		OBOFormatWriter writer = new OBOFormatWriter();
		try {
			StringWriter stringWriter = new StringWriter();
			BufferedWriter bwriter = new BufferedWriter(stringWriter);
			writer.write(doc, bwriter);
			bwriter.close();
			result.setSuccess(true);
			result.setFormats(new String[] { "OBO" });
			result.setContents(new String[] { stringWriter.getBuffer().toString() });
			// TODO add OWL export support
			return result;
		} catch (IOException exception) {
			logger.error("Could not create export results.", exception);
			result.setSuccess(false);
			result.setMessage("Could not write OBO export: " + exception.getMessage());
			return result;
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

	private String createFakeId(String prefix, int count) {
		StringBuilder sb = new StringBuilder(prefix);
		sb.append(":fake");
		if (count < 100) {
			sb.append('0');
		}
		if (count < 10) {
			sb.append('0');
		}
		sb.append(count);
		return sb.toString();
	}

	private String getOntologyIdPrefix(OntologyTaskManager manager) {
		LocalTask task = new LocalTask();
		manager.runManagedTask(task);
		return task.id;
	}

	private final class LocalTask implements OntologyTask {

		String id = null;

		@Override
		public Modified run(OWLGraphWrapper managed) {
			id = managed.getOntologyId();
			return Modified.no;
		}
	}

	@Override
	public JsonCommitResult commitTerms(String sessionId, JsonOntologyTerm[] terms, String ontology)
	{
		JsonCommitResult result = new JsonCommitResult();
		result.setSuccess(false);
		result.setMessage("The commit operation is not enabled.");
		return result;
	}

}
