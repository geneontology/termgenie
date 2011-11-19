package org.bbop.termgenie.ontology.obo;

import java.io.File;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.obo.OboScmHelper.OboCommitData;
import org.bbop.termgenie.scm.VersionControlAdapter;

/**
 * Instead of committing to the SCM repository, write the changed ontology to a
 * local file. <br/>
 * WARNING: This adapter is NOT to be used in production. Any
 * commit overwrites the previous one. There is no merge support in this class.
 */
public class FileOnlyOboCommitPipeline extends OboCommitPipeline {

	private final File localFile;

	public FileOnlyOboCommitPipeline(final OntologyTaskManager source,
			final CommitHistoryStore store,
			final OboScmHelper helper,
			final String localFile)
	{
		super(source, store, helper);
		this.localFile = new File(localFile);
	}

	@Override
	protected void commitToRepository(String commitMessage,
			VersionControlAdapter scm,
			OboCommitData data,
			String diff) throws CommitException
	{
		Logger.getLogger(getClass()).info("Commit to file. Message:\n"+commitMessage);
		helper.copyFileForCommit(data.getModifiedSCMTargetFile(), localFile);
	}
}
