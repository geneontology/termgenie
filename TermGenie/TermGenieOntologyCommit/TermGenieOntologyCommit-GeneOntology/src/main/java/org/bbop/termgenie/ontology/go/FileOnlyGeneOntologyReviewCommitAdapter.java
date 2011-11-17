package org.bbop.termgenie.ontology.go;

import java.io.File;

import org.bbop.termgenie.ontology.CommitException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.OBOSCMHelper;
import org.bbop.termgenie.ontology.OBOSCMHelper.OboCommitData;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.scm.VersionControlAdapter;

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
	FileOnlyGeneOntologyReviewCommitAdapter(@Named("GeneOntology") OntologyTaskManager source,
			CommitHistoryStore store,
			OBOSCMHelper helper,
			@Named("FileOnlyGeneOntologyReviewCommitAdapterLocalFile") final String localFile)
	{
		super(source, store, helper);
		this.localFile = new File(localFile);
	}

	@Override
	protected void commitToRepository(String username, VersionControlAdapter scm, OboCommitData data, String diff)
			throws CommitException
	{
		helper.copyFileForCommit(data.getModifiedSCMTargetFile(), localFile);
	}
}
