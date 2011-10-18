package org.bbop.termgenie.ontology.go;

import java.io.File;

import org.bbop.termgenie.cvs.CVSTools;
import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.go.GoCvsHelper.OboCommitData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Extend the {@link GeneOntologyReviewCommitAdapter} to write to a file instead
 * of a repository.<br/>
 * WARNING: This adapter is NOT to be used in production. Any
 * commit overwrites the previous one. There is no merge support in this class.
 */
@Singleton
public class FileOnlyGeneOntologyReviewCommitAdapter extends GeneOntologyReviewCommitAdapter {

	private final File localFile;

	@Inject
	FileOnlyGeneOntologyReviewCommitAdapter(@Named("GeneOntologyTaskManager") OntologyTaskManager source,
			CommitHistoryStore store,
			GoCvsHelper helper,
			@Named("FileOnlyGeneOntologyReviewCommitAdapterLocalFile") final String localFile)
	{
		super(source, store, helper);
		this.localFile = new File(localFile);
	}

	@Override
	protected void commitToRepository(String username, CVSTools scm, OboCommitData data, String diff)
			throws CommitException
	{
		helper.copyFileForCommit(data.getModifiedSCMTargetFile(), localFile);
	}
}
