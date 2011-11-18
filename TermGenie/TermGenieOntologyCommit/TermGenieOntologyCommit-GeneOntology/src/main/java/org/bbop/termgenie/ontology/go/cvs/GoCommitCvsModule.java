package org.bbop.termgenie.ontology.go.cvs;

import java.util.Properties;

import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.Committer;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.obo.OboCommitPipeline;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class GoCommitCvsModule extends AbstractGoCommitCvsModule {

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties
	 */
	public GoCommitCvsModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties)
	{
		super(cvsOntologyFileName, cvsRoot, applicationProperties);
	}

	@Singleton
	@Provides
	protected Committer provideCommitter(@Named("GeneOntology") OntologyTaskManager source,
			CommitHistoryStore store,
			OboScmHelper helper)
	{
		return new OboCommitPipeline(source, store, helper);
	}
	
}
