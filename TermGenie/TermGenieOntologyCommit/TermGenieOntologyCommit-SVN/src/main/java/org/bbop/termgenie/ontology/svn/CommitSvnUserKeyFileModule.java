package org.bbop.termgenie.ontology.svn;

import java.io.File;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.ScmHelper;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.inject.TypeLiteral;

public class CommitSvnUserKeyFileModule {

	private static class CommitOboSvnUserKeyFileModule extends AbstractOboCommitSvnModule {
		private final String svnUsername;
		private final File svnKeyFile;
		private final boolean usePassphrase;
	
		private CommitOboSvnUserKeyFileModule(String svnRepository,
				String svnOntologyFileName,
				String svnUsername,
				File svnKeyFile,
				Properties applicationProperties,
				boolean svnLoadExternals,
				boolean usePassphrase)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
			this.usePassphrase = usePassphrase;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterSVNUsername", svnUsername);
			bind("CommitAdapterSVNKeyFile", svnKeyFile);
			if (usePassphrase) {
				bindSecret("CommitAdapterSVNPassword");
			}
			else {
				bindNull("CommitAdapterSVNPassword");
			}
		}
	
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OBODoc>>() { /* empty */ }, 
					SvnHelper.OboSvnHelperKeyFile.class);
		}
	}
	
	private static class CommitOwlSvnUserKeyFileModule extends AbstractOwlCommitSvnModule {
		private final String svnUsername;
		private final File svnKeyFile;
		private final boolean usePassphrase;
		private final String catalogXml;
	
		private CommitOwlSvnUserKeyFileModule(String svnRepository,
				String svnOntologyFileName,
				String catalogXml,
				String svnUsername,
				File svnKeyFile,
				Properties applicationProperties,
				boolean svnLoadExternals,
				boolean usePassphrase)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
			this.catalogXml = catalogXml;
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
			this.usePassphrase = usePassphrase;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterSVNUsername", svnUsername);
			bind("CommitAdapterSVNKeyFile", svnKeyFile);
			if (usePassphrase) {
				bindSecret("CommitAdapterSVNPassword");	
			}
			else {
				bindNull("CommitAdapterSVNPassword");
			}
			bind("CommitAdapterSVNCatalogXml", catalogXml, true);
		}
	
		@Override
		protected void bindScmHelper() {
			bind(new TypeLiteral<ScmHelper<OWLOntology>>() { /* empty */ }, 
					SvnHelper.OwlSvnHelperKeyFile.class);
		}
	}
	
	/**
	 * Create an commit module for OBO and SVN with public/private key authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param svnKeyFile
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @param usePassphrase
	 * @return module
	 */
	public static IOCModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			boolean svnLoadExternals,
			boolean usePassphrase) {
		return new CommitOboSvnUserKeyFileModule(svnRepository, svnOntologyFileName, svnUsername, svnKeyFile, applicationProperties, svnLoadExternals, usePassphrase);
	}
	
	/**
	 * Create an commit module for OWL and SVN with public/private key authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param catalogXml
	 * @param svnUsername
	 * @param svnKeyFile
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @param usePassphrase
	 * @return module
	 */
	public static IOCModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			String catalogXml,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			boolean svnLoadExternals,
			boolean usePassphrase) {
		return new CommitOwlSvnUserKeyFileModule(svnRepository, svnOntologyFileName, catalogXml, svnUsername, svnKeyFile, applicationProperties, svnLoadExternals, usePassphrase);
	}
}
