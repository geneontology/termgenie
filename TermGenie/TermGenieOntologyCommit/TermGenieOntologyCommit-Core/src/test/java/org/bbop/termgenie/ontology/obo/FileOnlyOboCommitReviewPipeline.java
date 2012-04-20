package org.bbop.termgenie.ontology.obo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.obo.OboScmHelper.OboCommitData;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.obolibrary.oboformat.model.OBODoc;

/**
 * Extend the {@link OboCommitReviewPipeline} to write to a file instead of a
 * repository.<br/>
 * WARNING: This adapter is NOT to be used in production. Any commit overwrites
 * the previous one. There is no merge support in this class.
 */
public class FileOnlyOboCommitReviewPipeline extends OboCommitReviewPipeline {

	private final File localFolder;

	public FileOnlyOboCommitReviewPipeline(OntologyTaskManager source,
			CommitHistoryStore store,
			OboScmHelper helper,
			TermFilter<OBODoc> termFilter,
			final String localFolder) throws IOException
	{
		super(source, store, termFilter, helper);
		this.localFolder = new File(localFolder);
		FileUtils.forceMkdir(this.localFolder);
	}

	@Override
	protected void commitToRepository(String commitMessage,
			VersionControlAdapter scm,
			OboCommitData data,
			String diff) throws CommitException
	{
		Logger.getLogger(getClass()).info("Commit to file. Message:\n" + commitMessage);
		for(File modifiedSCMTargetFile : data.getModifiedSCMTargetFiles()) {
			File localFile = new File(localFolder, modifiedSCMTargetFile.getName());
			helper.copyFileForCommit(modifiedSCMTargetFile, localFile );
		}
	}
}
