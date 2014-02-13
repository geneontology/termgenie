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
	
		private CommitOboSvnUserKeyFileModule(String svnRepository,
				String svnOntologyFileName,
				String svnUsername,
				File svnKeyFile,
				Properties applicationProperties,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterSVNUsername", svnUsername);
			bind("CommitAdapterSVNKeyFile", svnKeyFile);
			bindSecret("CommitAdapterSVNPassword");
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
	
		private CommitOwlSvnUserKeyFileModule(String svnRepository,
				String svnOntologyFileName,
				String svnUsername,
				File svnKeyFile,
				Properties applicationProperties,
				boolean svnLoadExternals)
		{
			super(svnRepository, svnOntologyFileName, applicationProperties, svnLoadExternals);
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
		}
	
		@Override
		protected void configure() {
			super.configure();
			bind("CommitAdapterSVNUsername", svnUsername);
			bind("CommitAdapterSVNKeyFile", svnKeyFile);
			bindSecret("CommitAdapterSVNPassword");
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
	 * @return module
	 */
	public static IOCModule createOboModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			boolean svnLoadExternals) {
		return new CommitOboSvnUserKeyFileModule(svnRepository, svnOntologyFileName, svnUsername, svnKeyFile, applicationProperties, svnLoadExternals);
	}
	
	/**
	 * Create an commit module for OWL and SVN with public/private key authentication.
	 * 
	 * @param svnRepository
	 * @param svnOntologyFileName
	 * @param svnUsername
	 * @param svnKeyFile
	 * @param applicationProperties
	 * @param svnLoadExternals
	 * @return module
	 */
	public static IOCModule createOwlModule(String svnRepository,
			String svnOntologyFileName,
			String svnUsername,
			File svnKeyFile,
			Properties applicationProperties,
			boolean svnLoadExternals) {
		return new CommitOwlSvnUserKeyFileModule(svnRepository, svnOntologyFileName, svnUsername, svnKeyFile, applicationProperties, svnLoadExternals);
	}
}
