package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;

import org.bbop.termgenie.svn.SvnTool;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public abstract class SvnAwareOntologyModule extends OntologyModule {

	public static SvnAwareOntologyModule createAnonymousSvnModule(String configFile, Properties applicationProperties)
	{
		return new AnonymousSvnOntologyModule(configFile, applicationProperties);
	}
	
	public static SvnAwareOntologyModule createUsernamePasswordSvnModule(String configFile,
			Properties applicationProperties,
			String svnUsername)
	{
		PasswordSvnOntologyModule m = new PasswordSvnOntologyModule(configFile, applicationProperties);
		m.setSvnUsername(svnUsername);
		return m;
	}
	
	public static SvnAwareOntologyModule createSshKeySvnModule(String configFile,
			Properties applicationProperties,
			String svnUsername,
			String keyFile,
			boolean usePassphrase)
	{
		KeySvnOntologyModule m = new KeySvnOntologyModule(configFile, applicationProperties);
		m.setSvnUsername(svnUsername);
		m.setSvnKeyFile(keyFile);
		m.setUsePassphrase(usePassphrase);
		return m;
	}

	private String svnAwareRepositoryURL = null;
	private Map<IRI, String> svnAwareMappedIRIs = Collections.emptyMap();
	private String svnAwareCatalogXML = null;
	private String svnAwareWorkFolder = null;
	private boolean svnAwareLoadExternal = true;
	
	private SvnAwareOntologyModule(String configFile, Properties applicationProperties) {
		super(applicationProperties, configFile);
	}
	
	public void setSvnAwareRepositoryURL(String repositoryURL) {
		this.svnAwareRepositoryURL = repositoryURL;
	}

	public void setSvnAwareMappedIRIs(Map<IRI, String> mappedIRIs) {
		this.svnAwareMappedIRIs = mappedIRIs;
	}
	
	public void setSvnAwareCatalogXML(String catalogXML) {
		this.svnAwareCatalogXML = catalogXML;
	}
	
	public void setSvnAwareWorkFolder(String workFolder) {
		this.svnAwareWorkFolder = workFolder;
	}
	
	public void setSvnAwareLoadExternal(boolean loadExternal) {
		this.svnAwareLoadExternal = loadExternal;
	}

	@Override
	protected void bindIRIMappers() {
		super.bindIRIMappers();
		bind("SVNAwareIRIMapperRepositoryURL", svnAwareRepositoryURL);
		bind("SVNAwareIRIMapperWorkFolder", svnAwareWorkFolder);
		bindIRIMap("SVNAwareIRIMapperMappedIRIs", svnAwareMappedIRIs);
		bind("SVNAwareIRIMapperCatalogXML", svnAwareCatalogXML, true);
		bind("SVNAwareIRIMapperSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bind("SVNAwareIRIMapperSVNLoadExternal", svnAwareLoadExternal);
		bindAdditional();
	}
	
	protected void bindAdditional() {
		// default empty;
	}

	static class AnonymousSvnOntologyModule extends SvnAwareOntologyModule {

		AnonymousSvnOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}

		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
				@Named("SVNAwareIRIMapperCatalogXML") String catalogXML,
				@Named("SVNAwareIRIMapperMappedIRIs") Map<IRI, String> mappedIRIs,
				@Named("SVNAwareIRIMapperWorkFolder") @Nullable String svnWorkFolder,
				@Named("SVNAwareIRIMapperSVNConfigDir") File svnConfigDir,
				@Named("SVNAwareIRIMapperSVNLoadExternal") Boolean svnLoadExternal)
		{
			final File workFolderFile = new File(svnWorkFolder);
			final SvnTool svn = SvnTool.createAnonymousSVN(workFolderFile, repositoryURL, svnConfigDir, svnLoadExternal);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			SvnIRIMapper svnMapper = new SvnIRIMapper(svn, checkout, mappedIRIs, catalogXML);
			return svnMapper;
		}
	}
	
	static class PasswordSvnOntologyModule extends SvnAwareOntologyModule {

		private String svnUsername = null;

		PasswordSvnOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}
		
		public void setSvnUsername(String svnUsername) {
			this.svnUsername = svnUsername;
		}

		@Override
		protected void bindAdditional() {
			bind("SVNAwareIRIMapperUsername", svnUsername);
			bindSecret("SVNAwareIRIMapperPassword");
		}


		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
				@Named("SVNAwareIRIMapperCatalogXML") String catalogXML,
				@Named("SVNAwareIRIMapperMappedIRIs") Map<IRI, String> mappedIRIs,
				@Named("SVNAwareIRIMapperWorkFolder") @Nullable String workFolder,
				@Named("SVNAwareIRIMapperUsername") String svnUsername,
				@Named("SVNAwareIRIMapperPassword") String svnPassword,
				@Named("SVNAwareIRIMapperSVNConfigDir") File svnConfigDir,
				@Named("SVNAwareIRIMapperSVNLoadExternal") Boolean svnLoadExternal)
		{
			final File workFolderFile = new File(workFolder);
			final SvnTool svn = SvnTool.createUsernamePasswordSVN(workFolderFile, repositoryURL, svnUsername, svnPassword, svnConfigDir, svnLoadExternal);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			SvnIRIMapper svnMapper = new SvnIRIMapper(svn, checkout, mappedIRIs, catalogXML);
			return svnMapper;
		}
	}
	
	static class KeySvnOntologyModule extends SvnAwareOntologyModule {

		private String svnUsername = null;
		private String svnKeyFile = null;
		private boolean usePassphrase = true;

		KeySvnOntologyModule(String configFile, Properties applicationProperties) {
			super(configFile, applicationProperties);
		}
		
		public void setSvnUsername(String svnUsername) {
			this.svnUsername = svnUsername;
		}
		
		public void setSvnKeyFile(String svnKeyFile) {
			this.svnKeyFile = svnKeyFile;
		}
		
		public void setUsePassphrase(boolean usePassphrase) {
			this.usePassphrase = usePassphrase;
		}

		@Override
		protected void bindAdditional() {
			bind("SVNAwareIRIMapperUsername", svnUsername);
			bind("SVNAwareIRIMapperKeyFile", svnKeyFile);
			if (usePassphrase) {
				bindSecret("SVNAwareIRIMapperPassword");	
			}
			else {
				bindNull("SVNAwareIRIMapperPassword");
			}
		}

		@Provides
		@Singleton
		@Named("PrimaryIRIMapper")
		protected OWLOntologyIRIMapper getIRIMapper(
				@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
				@Named("SVNAwareIRIMapperCatalogXML") String catalogXML,
				@Named("SVNAwareIRIMapperMappedIRIs") Map<IRI, String> mappedIRIs,
				@Named("SVNAwareIRIMapperWorkFolder") @Nullable String workFolder,
				@Named("SVNAwareIRIMapperUsername") String svnUsername,
				@Named("SVNAwareIRIMapperKeyFile") String svnKeyFile,
				@Named("SVNAwareIRIMapperPassword") @Nullable String svnPassword,
				@Named("SVNAwareIRIMapperSVNConfigDir") File svnConfigDir,
				@Named("SVNAwareIRIMapperSVNLoadExternal") Boolean svnLoadExternal)
		{
			final File workFolderFile = new File(workFolder);
			final File keyFile = new File(svnKeyFile);
			final SvnTool svn = SvnTool.createSSHKeySVN(workFolderFile, repositoryURL, svnUsername, keyFile, svnPassword, svnConfigDir, svnLoadExternal);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			SvnIRIMapper svnMapper = new SvnIRIMapper(svn, checkout, mappedIRIs, catalogXML);
			return svnMapper;
		}
	}
}
