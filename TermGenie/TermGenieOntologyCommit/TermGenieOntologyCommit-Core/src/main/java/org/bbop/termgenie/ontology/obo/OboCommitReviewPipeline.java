package org.bbop.termgenie.ontology.obo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo.CommitMode;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipeline;
import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OBOWriterTools;
import org.bbop.termgenie.ontology.obo.OboScmHelper.OboCommitData;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.OBODoc;

import owltools.graph.OWLGraphWrapper;

public class OboCommitReviewPipeline extends OntologyCommitReviewPipeline<OboCommitData, OBODoc> implements
		OntologyCommitReviewPipelineStages
{

	protected final OboScmHelper helper;
	private final AfterReviewTaskManager afterReviewTaskManager;

	public OboCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			OboScmHelper helper)
	{
		super(source, store, helper.isSupportAnonymus());
		this.helper = helper;
		this.afterReviewTaskManager = new AfterReviewTaskManager("AfterReviewTaskManager", this);
	}

	@Override
	protected String createDiff(CommitHistoryItem historyItem, OntologyTaskManager source)
			throws CommitException
	{

		CreateDiffTask task = new CreateDiffTask(historyItem);
		source.runManagedTask(task);
		if (task.getException() != null) {
			throw error("Could not create diff", task.getException());
		}
		if (task.diff == null) {
			throw error("Could not create diff: empty result");
		}
		return task.diff;
	}

	private class CreateDiffTask extends OntologyTask {

		private final CommitHistoryItem historyItem;

		private String diff = null;

		public CreateDiffTask(CommitHistoryItem historyItem) {
			this.historyItem = historyItem;
		}

		@Override
		protected void runCatching(OWLGraphWrapper managed) throws Exception {
			Owl2Obo owl2Obo = new Owl2Obo();
			OBODoc oboDoc = owl2Obo.convert(managed.getSourceOntology());

			List<CommitedOntologyTerm> terms = historyItem.getTerms();
			boolean succcess = applyChanges(terms, oboDoc);
			if (succcess) {
				List<String> ids = new ArrayList<String>(terms.size());
				for (CommitedOntologyTerm term : terms) {
					ids.add(term.getId());
				}
				diff = OBOWriterTools.writeTerms(ids, oboDoc);
			}
		}
	}

	@Override
	protected OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		return helper.prepareWorkflow(workFolder);
	}

	@Override
	protected VersionControlAdapter prepareSCM(CommitMode mode,
			String username,
			String password,
			OboCommitData data) throws CommitException
	{
		return helper.createSCM(mode, username, password, data.getScmFolder());
	}

	@Override
	protected void updateSCM(VersionControlAdapter scm, OBODoc targetOntology, OboCommitData data)
			throws CommitException
	{
		try {
			scm.connect();
			scm.update();
		} catch (IOException exception) {
			throw error("Could not update scm repository", exception);
		} finally {
			try {
				scm.close();
			} catch (IOException exception) {
				Logger.getLogger(getClass()).error("Could not close SCM tool.", exception);
			}
		}
	}

	@Override
	protected OBODoc retrieveTargetOntology(VersionControlAdapter scm, OboCommitData data)
			throws CommitException
	{
		return helper.retrieveTargetOntology(scm, data);
	}

	@Override
	protected void checkTargetOntology(OboCommitData data, OBODoc targetOntology)
			throws CommitException
	{
		helper.checkTargetOntology(data, targetOntology);
	}

	@Override
	protected boolean applyChanges(List<CommitedOntologyTerm> terms, OBODoc ontology)
			throws CommitException
	{
		return helper.applyHistoryChanges(terms, ontology);
	}

	@Override
	protected void createModifiedTargetFile(OboCommitData data, OBODoc ontology)
			throws CommitException
	{
		helper.createModifiedTargetFile(data, ontology);
	}

	@Override
	protected void commitToRepository(String username, VersionControlAdapter scm, OboCommitData data, String diff)
			throws CommitException
	{
		helper.commitToRepository(username, scm, data, diff);
	}

	@Override
	protected CommitMode getCommitMode() {
		return helper.getCommitMode();
	}

	@Override
	protected String getCommitUserName() {
		return helper.getCommitUserName();
	}

	@Override
	protected String getCommitPassword() {
		return helper.getCommitPassword();
	}

	@Override
	public Committer getReviewCommitter() {
		return this;
	}

	@Override
	public BeforeReview getBeforeReview() {
		return this;
	}

	@Override
	public GenericTaskManager<AfterReview> getAfterReview() {
		return afterReviewTaskManager;
	}

	
	private static class AfterReviewTaskManager extends GenericTaskManager<AfterReview> {

		private final AfterReview instance;
		
		/**
		 * @param name
		 * @param instance
		 */
		private AfterReviewTaskManager(String name, AfterReview instance) {
			super(name);
			this.instance = instance;
		}

		@Override
		protected AfterReview createManaged() {
			return instance;
		}

		@Override
		protected AfterReview updateManaged(AfterReview managed) {
			return instance;
		}

		@Override
		protected AfterReview resetManaged(AfterReview managed) {
			return instance;
		}

		@Override
		protected void setChanged(boolean reset) {
			// do nothing
		}
	}
}
