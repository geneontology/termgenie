package org.bbop.termgenie.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.data.JsonCommitResult;
import org.bbop.termgenie.data.JsonExportResult;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
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

		private static final Set<String> updateRelations = createUpdateRelations();
		
		private static Set<String> createUpdateRelations() {
			HashSet<String> set = new HashSet<String>();
			set.add(OboFormatTag.TAG_IS_A.getTag());
			set.add(OboFormatTag.TAG_INTERSECTION_OF.getTag());
			set.add(OboFormatTag.TAG_UNION_OF.getTag());
			// DO not add disjoint_from
			set.add(OboFormatTag.TAG_RELATIONSHIP.getTag());
			return Collections.unmodifiableSet(set);
		}
		
		private final JsonOntologyTerm[] terms;

		private String oboDiffAdd = null;
		private String oboDiffModified = null;

		public CreateExportDiffTask(JsonOntologyTerm[] terms) {
			this.terms = terms;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			
			OBODoc oboDoc;
			Owl2Obo owl2Obo = new Owl2Obo();
			oboDoc = owl2Obo.convert(managed.getSourceOntology());
			
			List<String> addIds = new ArrayList<String>(terms.length);
			List<String> modIds = new ArrayList<String>();
			
			for (JsonOntologyTerm term : terms) {
				final Frame frame = new Frame(FrameType.TERM);

				// id
				String tempId = term.getTempId();
				addIds.add(tempId);
				frame.setId(tempId);

				// name
				addClause(frame, OboFormatTag.TAG_NAME, term.getLabel());

				// definition
				Clause defClause = new Clause(OboFormatTag.TAG_DEF, term.getDefinition());
				frame.addClause(defClause);
				List<String> defxRefs = term.getDefXRef();
				if (defxRefs != null && !defxRefs.isEmpty()) {
					for (String defxRef : defxRefs) {
						Xref xref = new Xref(defxRef);
						defClause.addXref(xref);
					}
				}

				// synonyms
				List<JsonSynonym> synonyms = term.getSynonyms();
				if (synonyms != null) {
					for (JsonSynonym jsonSynonym : synonyms) {
						Clause synClause = new Clause(OboFormatTag.TAG_SYNONYM, jsonSynonym.getLabel());
						Set<String> xrefs = jsonSynonym.getXrefs();
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
				List<JsonTermRelation> relations = term.getRelations();
				if (relations != null) {
					OBOConverterTools.fillRelations(frame, relations, null);
				}

				// meta data
				Map<String, String> metaData = term.getMetaData();
				addClause(frame, OboFormatTag.TAG_COMMENT, metaData);
				addClause(frame, OboFormatTag.TAG_CREATED_BY, metaData);
				addClause(frame, OboFormatTag.TAG_CREATION_DATE, metaData);

				oboDoc.addTermFrame(frame);
				
				// changed relations
				handleChangedRelations(term, modIds, oboDoc);
			}
			
			oboDiffAdd = OBOWriterTools.writeTerms(addIds, oboDoc);
			if (!modIds.isEmpty()) {
				oboDiffModified = OBOWriterTools.writeTerms(modIds, oboDoc);
			}
		}

		private void handleChangedRelations(JsonOntologyTerm term,
				List<String> modIds,
				OBODoc oboDoc)
		{
			List<JsonTermRelation> changed = term.getChanged();
			if (changed != null && !changed.isEmpty()) {
				Map<String, List<JsonTermRelation>> groups = new HashMap<String, List<JsonTermRelation>>();
				for (JsonTermRelation change : changed) {
					List<JsonTermRelation> list = groups.get(change.getSource());
					if (list == null) {
						list = new ArrayList<JsonTermRelation>();
						groups.put(change.getSource(), list);
					}
					list.add(change);
				}
				
				for(String modId : groups.keySet()) {
					modIds.add(modId);
					Frame frame = oboDoc.getTermFrame(modId);
					Collection<Clause> clauses = frame.getClauses();
					
					// remove old relations, except disjoint_from
					Iterator<Clause> iterator = clauses.iterator();
					while (iterator.hasNext()) {
						Clause clause = iterator.next();
						String tag = clause.getTag();
						if (updateRelations.contains(tag)) {
							iterator.remove();
						}
					}
					
					List<JsonTermRelation> relations = groups.get(modId);
					List<Clause> newRelations = OBOConverterTools.translateRelations(relations, null);
					for (Clause newRelation : newRelations) {
						String tag = newRelation.getTag();
						if (updateRelations.contains(tag)) {
							clauses.add(newRelation);
						}
					}
				}
			}
		}

	}
	
	protected OntologyTaskManager getOntologyManager(String ontologyName) {
		return ontologyTools.getManager(ontologyName);
	}

	private static void addClause(Frame frame, OboFormatTag tag, Map<String, String> map) {
		addClause(frame, tag, map.get(tag.getTag()));
	}
	
	private static void addClause(Frame frame, OboFormatTag tag, String value) {
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
