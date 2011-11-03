package org.bbop.termgenie.ontology.go;

import java.io.File;
import java.util.List;

import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.OntologyCommitPipeline;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.go.GoCvsHelper.OboCommitData;
import org.obolibrary.oboformat.model.OBODoc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GeneOntologyCommitAdapter extends OntologyCommitPipeline<CVSTools, OboCommitData, OBODoc>
{

	protected final GoCvsHelper helper;

	@Inject
	GeneOntologyCommitAdapter(@Named("GeneOntology") final OntologyTaskManager source,
			final CommitHistoryStore store,
			final GoCvsHelper helper)
	{
		super(source, store, helper.isSupportAnonymus());
		this.helper = helper;
	}

	@Override
	protected OboCommitData prepareWorkflow(File workFolder) throws CommitException {
		return helper.prepareWorkflow(workFolder);
	}

	@Override
	protected CVSTools prepareSCM(CommitInfo commitInfo, OboCommitData data) throws CommitException
	{
		return helper.prepareSCM(commitInfo, data);
	}

	@Override
	protected OBODoc retrieveTargetOntology(CVSTools cvs, OboCommitData data)
			throws CommitException
	{
		return helper.retrieveTargetOntology(cvs, data);
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
	protected void createModifiedTargetFile(OboCommitData data, OBODoc ontology)
			throws CommitException
	{
		helper.createModifiedTargetFile(data, ontology);
	}

	@Override
	protected void commitToRepository(String username, CVSTools scm, OboCommitData data, String diff)
			throws CommitException
	{
		helper.commitToRepository(username, scm, data, diff);
	}
}
