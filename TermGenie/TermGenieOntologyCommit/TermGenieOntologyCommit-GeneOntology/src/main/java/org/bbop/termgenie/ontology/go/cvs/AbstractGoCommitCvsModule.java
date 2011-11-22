package org.bbop.termgenie.ontology.go.cvs;

import java.util.Properties;

import org.bbop.termgenie.ontology.go.AbstractGoCommitModule;
import org.bbop.termgenie.ontology.obo.OboScmHelper;

public class AbstractGoCommitCvsModule extends AbstractGoCommitModule {

	private final String cvsOntologyFileName;
	private final String cvsRoot;

	/**
	 * @param cvsOntologyFileName
	 * @param cvsRoot
	 * @param applicationProperties 
	 */
	public AbstractGoCommitCvsModule(String cvsOntologyFileName, String cvsRoot, Properties applicationProperties) {
		super(applicationProperties);
		this.cvsOntologyFileName = cvsOntologyFileName;
		this.cvsRoot = cvsRoot;
	}

	@Override
	protected void configure() {
		super.configure();
		bind("GeneOntologyCommitAdapterCVSOntologyFileName", cvsOntologyFileName);
		bind("GeneOntologyCommitAdapterCVSRoot", cvsRoot);
		bindCVSPassword();
		bindOBOSCMHelper();
	}
	
	protected void bindOBOSCMHelper() {
		bind(OboScmHelper.class, GoCvsHelperPassword.class);
	}

	protected void bindCVSPassword() {
		// bind the password only via a system parameter !
		// Reason: Do not accidently commit a secret password
		bindSecret("GeneOntologyCommitAdapterCVSPassword");
	}
}
