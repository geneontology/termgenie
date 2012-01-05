package org.bbop.termgenie.ontology.obo;

import java.io.File;
import java.util.List;

import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.obo.OboScmHelper.OboCommitData;
import org.bbop.termgenie.ontology.OntologyCommitPipeline;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.obolibrary.oboformat.model.OBODoc;

public class OboCommitPipeline extends OntologyCommitPipeline<OboCommitData, OBODoc>
{
	protected final OboScmHelper helper;

	public OboCommitPipeline(final OntologyTaskManager source,
			final CommitHistoryStore store,
			final OboScmHelper helper)
	{
		super(source, store, helper.isSupportAnonymus());
		this.helper = helper;
	}

	@Override
	protected OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		return helper.prepareWorkflow(workFolder);
	}

	@Override
	protected VersionControlAdapter prepareSCM(CommitInfo commitInfo, OboCommitData data) throws CommitException
	{
		return helper.prepareSCM(commitInfo, data);
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
	protected boolean applyChanges(List<CommitObject<TermCommit>> terms, final OBODoc oboDoc) {
		return helper.applyChanges(terms, oboDoc);
	}

	@Override
	protected void createModifiedTargetFile(OboCommitData data, OBODoc ontology, String savedBy)
			throws CommitException
	{
		helper.createModifiedTargetFile(data, ontology, savedBy);
	}

	@Override
	protected void commitToRepository(String commitMessage, VersionControlAdapter scm, OboCommitData data, String diff)
			throws CommitException
	{
		helper.commitToRepository(commitMessage, scm, data, diff);
	}
}
