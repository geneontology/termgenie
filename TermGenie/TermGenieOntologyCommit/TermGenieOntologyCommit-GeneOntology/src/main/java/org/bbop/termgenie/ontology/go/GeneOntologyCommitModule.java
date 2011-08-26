package org.bbop.termgenie.ontology.go;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.Committer;

public class GeneOntologyCommitModule extends IOCModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;
	
	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 */
	public GeneOntologyCommitModule(String cvsOntologyFileName, String cvsRoot)
	{
		super();
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	public GeneOntologyCommitModule() {
		this(null, null);
	}

	@Override
	protected void configure() {
		bind(Committer.class).to(GeneOntologyCommitAdapter.class);
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bind("GeneOntologyCommitAdapterCVSPassword"); 
		
	}

}
