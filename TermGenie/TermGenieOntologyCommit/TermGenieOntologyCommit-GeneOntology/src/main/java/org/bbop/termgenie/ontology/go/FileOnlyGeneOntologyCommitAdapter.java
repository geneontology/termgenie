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
 * Instead of committing to the CVS repository, write the changed ontology to a
 * local file. <br/>
 * WARNING: This adapter is NOT to be used in production. Any
 * commit overwrites the previous one. There is no merge support in this class.
 */
@Singleton
public class FileOnlyGeneOntologyCommitAdapter extends GeneOntologyCommitAdapter {

	private final File localFile;

	@Inject
	FileOnlyGeneOntologyCommitAdapter(@Named("GeneOntology") final OntologyTaskManager source,
			final CommitHistoryStore store,
			final GoCvsHelper helper,
			@Named("FileOnlyGeneOntologyCommitAdapterLocalFile") final String localFile)
	{
		super(source, store, helper);
		this.localFile = new File(localFile);
	}

	@Override
	protected void commitToRepository(String username,
			CVSTools scm,
			OboCommitData data,
			String diff) throws CommitException
	{
		helper.copyFileForCommit(data.getModifiedSCMTargetFile(), localFile);
	}
}
