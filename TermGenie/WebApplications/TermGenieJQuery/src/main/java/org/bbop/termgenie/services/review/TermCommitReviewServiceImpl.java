package org.bbop.termgenie.services.review;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryTools;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.Committer.CommitResult;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.AfterReview;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages.BeforeReview;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.ComitAwareOboTools;
import org.bbop.termgenie.ontology.obo.OboParserTools;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.ontology.obo.OwlTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.permissions.UserPermissions;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import owltools.graph.OWLGraphWrapper;
import owltools.io.ParserWrapper.OboAndOwlNameProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class TermCommitReviewServiceImpl implements TermCommitReviewService {

	private static final Logger logger = Logger.getLogger(TermCommitReviewServiceImpl.class);
	
	private final InternalSessionHandler sessionHandler;
	private final UserPermissions permissions;
	private final OntologyCommitReviewPipelineStages stages;
	private final OntologyTaskManager manager;
	private final Ontology ontology;
	private boolean useOboDiff = true;
	private boolean doAsciiCheck = false;
	
	@Inject
	TermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			OntologyLoader loader,
			OntologyCommitReviewPipelineStages stages)
	{
		super();
		this.sessionHandler = sessionHandler;
		this.permissions = permissions;
		this.manager = loader.getOntologyManager();
		this.ontology = manager.getOntology();
		this.stages = stages;
	}

	@Inject(optional=true)
	public void setUseOboDiff(@Named("TermCommitReviewServiceImpl.useOboDiff") @Nullable Boolean useOboDiff) {
		if (useOboDiff != null) {
			this.useOboDiff = useOboDiff.booleanValue();
		}
	}
	
	@Inject(optional=true)
	public void setDoAsciiCheck(@Named("TermCommitReviewServiceImpl.doAsciiCheck") @Nullable Boolean doAsciiCheck) {
		this.doAsciiCheck = doAsciiCheck;
	}

	@Override
	public JsonReviewConfig getConfig() {
		JsonReviewConfig config = new JsonReviewConfig();
		Committer reviewCommitter = stages.getReviewCommitter();
		config.isEnabled = reviewCommitter != null;
		config.useAsciiCheck = doAsciiCheck;
		return config;
	}

	@Override
	public boolean isAuthorized(String sessionId, HttpSession session) {
		String screenname = sessionHandler.isAuthenticated(sessionId, session);
		if (screenname != null) {
			UserData userData = sessionHandler.getUserData(session);
			if (userData != null) {
				boolean allowCommitReview = permissions.allowCommitReview(userData, ontology);
				return allowCommitReview;
			}
		}
		return false;
	}

	@Override
	public JsonCommitReviewEntry[] getPendingCommits(String sessionId, HttpSession session) {
		if (isAuthorized(sessionId, session)) {
			BeforeReview beforeReview = stages.getBeforeReview();
			try {
				List<CommitHistoryItem> items = beforeReview.getItemsForReview();
				if (items != null && !items.isEmpty()) {
					List<JsonCommitReviewEntry> result = createEntries(items);
					
					if (!result.isEmpty()) {
						// update commit message to include the user doing the review
						UserData userData = sessionHandler.getUserData(session);
						if (userData == null) {
							logger.warn("Could not retrieve user data for session: "+sessionId);
							return null;
						}
						String scmAlias = userData.getScmAlias();
						for (JsonCommitReviewEntry entry : result) {
							StringBuilder sb = new StringBuilder(entry.getCommitMessage());
							sb.append(" reviewed by ").append(scmAlias);
							entry.setCommitMessage(sb.toString());
							entry.setCommitMessageChanged(true);
						}
						return result.toArray(new JsonCommitReviewEntry[result.size()]);
					}
				}
			} catch (CommitException exception) {
				logger.error("Could not retrieve pending commits for db", exception);
			} catch (OWLOntologyCreationException exception) {
				logger.error("Could not create entries for items", exception);
			} catch (InvalidManagedInstanceException exception) {
				logger.error("Could not create entries for items due to invalid ontology", exception);
			} catch (OBOFormatParserException exception) {
				logger.error("Could not create entries for items due to an obo format exception.", exception);
			}
		}
		return null;
	}

	protected List<JsonCommitReviewEntry> createEntries(List<CommitHistoryItem> items) throws OWLOntologyCreationException, InvalidManagedInstanceException, OBOFormatParserException {
		CreateReviewEntriesTask task = new CreateReviewEntriesTask(items);
		manager.runManagedTask(task);
		if (task.owlException != null) {
			throw task.owlException;
		}
		if (task.oboException != null) {
			throw task.oboException;
		}
		return task.result;
	}
	
	private class CreateReviewEntriesTask extends OntologyTask {

		private final List<CommitHistoryItem> items;
		
		List<JsonCommitReviewEntry> result = null;
		OWLOntologyCreationException owlException = null;
		OBOFormatParserException oboException = null;

		/**
		 * @param items
		 */
		CreateReviewEntriesTask(List<CommitHistoryItem> items) {
			super();
			this.items = items;
		}

		@Override
		public void runCatching(OWLGraphWrapper graph) {
			try {
				NameProvider provider = null;
				OBODoc oboDoc = null;
				if (useOboDiff) {
					Owl2Obo owl2Obo = new Owl2Obo();
					oboDoc = owl2Obo.convert(graph.getSourceOntology());
					provider = new OboAndOwlNameProvider(oboDoc, graph);
				}
				result = new ArrayList<JsonCommitReviewEntry>(items.size());
				for (CommitHistoryItem item : items) {
					JsonCommitReviewEntry entry = new JsonCommitReviewEntry();
					entry.setHistoryId(item.getId());
					entry.setVersion(item.getVersion());
					entry.setDate(formatDate(item.getDate()));
					entry.setCommitMessage(item.getCommitMessage());
					entry.setEmail(item.getEmail());
					if (useOboDiff) {
						entry.setDiffs(createJsonDiffs(item, oboDoc, provider));
					}
					else {
						entry.setDiffs(createJsonOwlDiffs(item));
					}
					
					result.add(entry);
				}
			} catch (OWLOntologyCreationException exception) {
				this.owlException = exception;
			} catch (OBOFormatParserException exception) {
				this.oboException = exception;
			}
		}
	}

	private List<JsonDiff> createJsonDiffs(CommitHistoryItem item, OBODoc oboDoc, NameProvider nameProvider) throws OBOFormatParserException {
		List<JsonDiff> result = new ArrayList<JsonDiff>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			Frame frame = CommitHistoryTools.translate(term.getId(), term.getObo());
			JsonDiff jsonDiff = new JsonDiff();
			jsonDiff.setOperation(term.getOperation());
			jsonDiff.setId(term.getId());
			jsonDiff.setUuid(term.getUuid());
			jsonDiff.setObsolete(OboTools.isObsolete(frame));
			jsonDiff.setPattern(term.getPattern());
			
			List<Pair<Frame, Set<OWLAxiom>>> changed = CommitHistoryTools.translateSimple(term.getChanged());
			boolean success = ComitAwareOboTools.handleTerm(frame, changed, term.getOperation(), oboDoc);
			if (success) {
				try {
					jsonDiff.setDiff(OboWriterTools.writeTerm(term.getId(), oboDoc, nameProvider));
					jsonDiff.setOwlAxioms(term.getAxioms());
					result.add(jsonDiff);
				} catch (IOException exception) {
					logger.error("Could not create diff for pending commit", exception);
				}
			}
			if (changed != null && !changed.isEmpty()) {
				jsonDiff.setRelations(JsonOntologyTerm.createJson(changed, nameProvider));
			}
		}
		if (!result.isEmpty()) {
			return result;
		}
		return null;
	}
	
	private List<JsonDiff> createJsonOwlDiffs(CommitHistoryItem item) {
		List<JsonDiff> result = new ArrayList<JsonDiff>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(term.getAxioms());
			JsonDiff jsonDiff = new JsonDiff();
			jsonDiff.setOperation(term.getOperation());
			jsonDiff.setId(term.getId());
			jsonDiff.setUuid(term.getUuid());
			jsonDiff.setObsolete(OwlTools.isObsolete(axioms));
			jsonDiff.setPattern(term.getPattern());
			jsonDiff.setOwlAxioms(term.getAxioms());
			result.add(jsonDiff);
		}
		if (!result.isEmpty()) {
			return result;
		}
		return null;
	}
	
	private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat();
		}
		
	};
	
	private String formatDate(Date date) {
		return dateFormat.get().format(date);
	}

	@Override
	public JsonCommitReviewCommitResult commit(String sessionId,
			JsonCommitReviewEntry[] entries,
			HttpSession session,
			ProcessState state)
	{
		if (!isAuthorized(sessionId, session)) {
			return JsonCommitReviewCommitResult.error("Error: This commit is not authorized.");
		}
		if (entries == null || entries.length == 0) {
			return JsonCommitReviewCommitResult.error("Error: No entires to commit in the request.");
		}
		
		// commit changes to repository
		GenericTaskManager<AfterReview> afterReviewTaskManager = stages.getAfterReview();
		
		CommitTask task = new CommitTask(entries, this, state);
		try {
			afterReviewTaskManager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			logger.error("Error during commit", exception);
			return JsonCommitReviewCommitResult.error("Error during commit: "+exception.getMessage());
		}
		if (task.exception != null) {
			logger.error("Error during commit", task.exception);
			return JsonCommitReviewCommitResult.error("Error during commit: "+task.exception.getMessage());
		}
		return JsonCommitReviewCommitResult.success(task.historyIds, task.commits);
	}

	private static final class CommitTask implements ManagedTask<AfterReview> {
	
		private final JsonCommitReviewEntry[] entries;
		private List<Integer> historyIds;
		private List<CommitResult> commits;
		private CommitException exception;
		private final TermCommitReviewServiceImpl instance;
		private final ProcessState state;
	
		private CommitTask(JsonCommitReviewEntry[] entries,
				TermCommitReviewServiceImpl instance,
				ProcessState state)
		{
			this.entries = entries;
			this.instance = instance;
			this.state = state;
		}
	
		@Override
		public Modified run(AfterReview afterReview)
		{
			try {
				ProcessState.addMessage(state, "Start updating internal database");
				historyIds = new ArrayList<Integer>(entries.length);
				for (JsonCommitReviewEntry entry : entries) {
					if (entry.isCommitMessageChanged()) {
						updateHistoryItem(entry, afterReview);
					}
					else {
						List<JsonDiff> jsonDiffs = entry.getDiffs();
						for (JsonDiff jsonDiff : jsonDiffs) {
							if (jsonDiff.isModified()) {
								updateHistoryItem(entry, afterReview);
								break;
							}
						}
					}
					historyIds.add(entry.getHistoryId());
				}
				ProcessState.addMessage(state, "Finished updating internal database");
				
				commits = afterReview.commit(historyIds, state);
				
				if (logger.isInfoEnabled()) {
					StringBuilder sb = new StringBuilder();
					for (CommitResult r : commits) {
						List<CommitObject<TermCommit>> terms = r.getTerms();
						for (CommitObject<TermCommit> commitObject : terms) {
							Frame frame = commitObject.getObject().getTerm();
							String id = frame.getId();
							String lbl = frame.getTagValue(OboFormatTag.TAG_NAME, String.class);
							sb.append("\n");
							sb.append(id).append(" '").append(lbl).append('\'');
						}
					}
					logger.info("Finished commit after review for ids:"+sb);
				}
				
				return Modified.no;
			} catch (CommitException exception) {
				this.exception = exception;
				return Modified.no;
			}
		}
		
		private void updateHistoryItem(JsonCommitReviewEntry entry, AfterReview afterReview) throws CommitException {
			int historyId = entry.getHistoryId();
			CommitHistoryItem historyItem = afterReview.getItem(historyId);
			if (entry.getVersion() != historyItem.getVersion()) {
				throw new CommitException("Could not change item, due to conflicting updates.", false);
			}
			if (historyItem.isCommitted()) {
				throw new CommitException("Trying to change an already committed item.", false);
			}
			if (entry.isCommitMessageChanged()) {
				historyItem.setCommitMessage(entry.getCommitMessage());
			}
			StringBuilder sb = new StringBuilder(historyItem.getCommitMessage());
			List<JsonDiff> diffs = entry.getDiffs();
			for (JsonDiff jsonDiff : diffs) {
				final CommitedOntologyTerm term = getMatchingTerm(historyItem, jsonDiff.getUuid());
				if (instance.useOboDiff) {
					Frame termFrame = parseOboDiff(jsonDiff);
					if (jsonDiff.isModified()) {
						CommitHistoryTools.update(term, termFrame, jsonDiff.getOwlAxioms(), JsonDiff.getModification(jsonDiff));
					}
					sb.append('\n');
					if(OboTools.isObsolete(termFrame)) {
						term.setChanged(null); // do not change any relations for other terms
						sb.append("OBSOLETE ");
					}
					else {
						sb.append("Added ");
					}
					sb.append(termFrame.getId());
					Object label = termFrame.getTagValue(OboFormatTag.TAG_NAME);
					if (label != null) {
						sb.append(" ");
						sb.append(label);
					}
				}
				else {
					try {
						Set<OWLAxiom> axioms = parseOwlDiff(jsonDiff);
						if (jsonDiff.isModified()) {
							Frame termFrame = OwlTranslatorTools.generateFrame(axioms, jsonDiff.getId());
							CommitHistoryTools.update(term, termFrame, OwlStringTools.translateAxiomsToString(axioms), JsonDiff.getModification(jsonDiff));
						}
					} catch (OWLOntologyCreationException exception) {
						throw new CommitException("", exception, false);
					}
				}
			}
			
			historyItem.setCommitMessage(sb.toString());
			afterReview.updateItem(historyItem);
		}

		private Frame parseOboDiff(JsonDiff jsonDiff) {
			Frame frame = OboParserTools.parseFrame(jsonDiff.getId(), jsonDiff.getDiff());
			if (frame == null) {
				return null;
			}
			Collection<Clause> clauses = frame.getClauses();
			Iterator<Clause> iterator = clauses.iterator();
			String tagName = OboFormatTag.TAG_IS_OBSELETE.getTag();
			while (iterator.hasNext()) {
				Clause clause = iterator.next();
				if (tagName.equals(clause.getTag())) {
					iterator.remove();
				}
			}
			if (jsonDiff.isObsolete()) {
				Clause obsoleteClause = new Clause(OboFormatTag.TAG_IS_OBSELETE);
				obsoleteClause.addValue(Boolean.TRUE);
				frame.addClause(obsoleteClause);
				
				instance.handleObsoleteOboFrame(jsonDiff, frame);
			}
			return frame;
		}
		
		private Set<OWLAxiom> parseOwlDiff(JsonDiff jsonDiff) {
			Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(jsonDiff.getOwlAxioms());
			if (jsonDiff.isObsolete()) {
				IRI classIRI = OwlTools.translateFrameIdToClassIRI(jsonDiff.getId());
				// remove all relation and equivalent class axioms
				Iterator<OWLAxiom> it = axioms.iterator();
				while (it.hasNext()) {
					OWLAxiom ax = it.next();
					Boolean remove = ax.accept(new OWLAxiomVisitorEx<Boolean>() {

						@Override
						public Boolean visit(OWLSubAnnotationPropertyOfAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLAnnotationPropertyDomainAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLAnnotationPropertyRangeAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLSubClassOfAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLAsymmetricObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLReflexiveObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDisjointClassesAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLDataPropertyDomainAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLObjectPropertyDomainAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLEquivalentObjectPropertiesAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDifferentIndividualsAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDisjointDataPropertiesAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLObjectPropertyRangeAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLFunctionalObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLDisjointUnionAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLDeclarationAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLAnnotationAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLSymmetricObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDataPropertyRangeAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLFunctionalDataPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLEquivalentDataPropertiesAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLClassAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLEquivalentClassesAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLDataPropertyAssertionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLSubDataPropertyOfAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLSameIndividualAxiom axiom) {
							return Boolean.TRUE;
						}

						@Override
						public Boolean visit(OWLSubPropertyChainOfAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLInverseObjectPropertiesAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLHasKeyAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(OWLDatatypeDefinitionAxiom axiom) {
							return null;
						}

						@Override
						public Boolean visit(SWRLRule rule) {
							return null;
						}
						
					});
					if (remove != null && remove.booleanValue() == true) {
						it.remove();
					}
					
				}
				// add obsolete annotation
				OwlTools.addObsoleteAxiom(axioms, classIRI);
				
			}
			return axioms;
		}

		private CommitedOntologyTerm getMatchingTerm(CommitHistoryItem historyItem,
				int uuid) throws CommitException
		{
			List<CommitedOntologyTerm> terms = historyItem.getTerms();
			for (int i = 0; i < terms.size(); i++) {
				CommitedOntologyTerm original = terms.get(i);
				if (uuid == original.getUuid()) {
					return terms.get(i);
				}
			}
			throw new CommitException("Could not find the modified term in the history", false);
		}
	}

	/**
	 * Override this method to change the implement additional modifications for obsolete frames.
	 * 
	 * @param jsonDiff
	 * @param frame
	 */
	protected void handleObsoleteOboFrame(JsonDiff jsonDiff, Frame frame) {
		// remove all relations
		OboTools.removeAllRelations(frame);
		
		// remove all references to this term
	}
	
}
