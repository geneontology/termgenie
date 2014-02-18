package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;
import org.semanticweb.owlapi.model.IRI;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


public abstract class SvnAwareXMLReloadingOntologyModule extends XMLReloadingOntologyModule {

	public static SvnAwareXMLReloadingOntologyModule createAnonymousSvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String fileCachingFolder,
			boolean loadExternal,
			List<String> ignoreMappings)
	{
		return new AnonymousSvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, fileCachingFolder, loadExternal, ignoreMappings);
	}
	
	public static SvnAwareXMLReloadingOntologyModule createAnonymousSvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String fileCachingFolder,
			boolean loadExternal,
			List<String> ignoreMappings,
			final Map<IRI, File> localMappings)
	{
		if (localMappings == null || localMappings.isEmpty()) {
			return createAnonymousSvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, fileCachingFolder, loadExternal, ignoreMappings);
		}
		return new AnonymousSvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, fileCachingFolder, loadExternal, ignoreMappings) {

			@Override
			protected IRIMapper getLocalIRIMapper() {
				return new LocalIRIMapper(localMappings);
			}
			
		};
	}
	
	public static SvnAwareXMLReloadingOntologyModule createUsernamePasswordSvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String svnUsername,
			boolean loadExternal,
			List<String> ignoreMappings)
	{
		return new PasswordSvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, loadExternal, ignoreMappings);
	}
	
	public static SvnAwareXMLReloadingOntologyModule createUsernamePasswordSvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String svnUsername,
			boolean loadExternal,
			List<String> ignoreMappings,
			final Map<IRI, File> localMappings)
	{
		if (localMappings == null || localMappings.isEmpty()) {
			return createUsernamePasswordSvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, loadExternal, ignoreMappings);
		}
		return new PasswordSvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, loadExternal, ignoreMappings) {

			@Override
			protected IRIMapper getLocalIRIMapper() {
				return new LocalIRIMapper(localMappings);
			}
			
		};
	}
	
	public static SvnAwareXMLReloadingOntologyModule createSshKeySvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String svnUsername,
			String keyFile,
			boolean loadExternal,
			boolean usePassphrase,
			List<String> ignoreMappings)
	{
		return new KeySvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, keyFile, loadExternal, usePassphrase, ignoreMappings);
	}
	
	public static SvnAwareXMLReloadingOntologyModule createSshKeySvnModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String svnUsername,
			String keyFile,
			boolean loadExternal,
			boolean usePassphrase,
			List<String> ignoreMappings,
			final Map<IRI, File> localMappings)
	{
		if (localMappings == null || localMappings.isEmpty()) {
			return createSshKeySvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, keyFile, loadExternal, usePassphrase, ignoreMappings);
		}
		return new KeySvnAwareXMLReloadingOntologyModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUsername, keyFile, loadExternal, usePassphrase, ignoreMappings) {

			@Override
			protected IRIMapper getLocalIRIMapper() {
				return new LocalIRIMapper(localMappings);
			}
			
		};
	}
	

	private final String repositoryURLDefault;
	private final Map<IRI, String> mappedIRIs;
	private final String catalogXML;
	private final String workFolderDefault;
	private final String fileCachingFolder;
	private final boolean loadExternal;
	
	private SvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			Map<IRI, String> mappedIRIs,
			String catalogXML,
			String workFolder,
			String fileCachingFolder,
			boolean loadExternal,
			List<String> ignoreMappings)
	{
		super(configFile, ignoreMappings, applicationProperties);
		this.repositoryURLDefault = repositoryURL;
		this.mappedIRIs = mappedIRIs;
		this.catalogXML = catalogXML;
		this.workFolderDefault = workFolder;
		if(fileCachingFolder == null) {
			this.fileCachingFolder = new File(FileUtils.getTempDirectory(), "termgenie-download-cache").getAbsolutePath();
		}
		else {
			this.fileCachingFolder = fileCachingFolder;
		}
		this.loadExternal = loadExternal;

	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, "FallbackIRIMapper", FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", fileCachingFolder);
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
		bindList("FileCachingIRIMapperIgnoreMappings", ignoreMappings, true);

		bind("SVNAwareIRIMapperRepositoryURL", repositoryURLDefault);
		bind("SVNAwareIRIMapperWorkFolder", workFolderDefault);
		bindIRIMap("SVNAwareIRIMapperMappedIRIs", mappedIRIs);
		bind("SVNAwareIRIMapperCatalogXML", catalogXML, true);
		bind("SVNAwareIRIMapperSVNConfigDir", SvnTool.getDefaultSvnConfigDir());
		bind("SVNAwareIRIMapperSVNLoadExternal", loadExternal);
		bindAdditional();
	}
	
	protected void bindAdditional() {
		// default empty;
	}

	@Provides
	@Singleton
	@Named("LocalIRIMapper")
	@Nullable
	protected IRIMapper getLocalIRIMapper() {
		return null;
	}
	
	static class AnonymousSvnAwareXMLReloadingOntologyModule extends SvnAwareXMLReloadingOntologyModule {

		AnonymousSvnAwareXMLReloadingOntologyModule(String configFile,
				Properties applicationProperties,
				String repositoryURL,
				Map<IRI, String> mappedIRIs,
				String catalogXML,
				String workFolder,
				String fileCachingFolder,
				boolean svnLoadExternal,
				List<String> ignoreMappings)
		{
			super(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, fileCachingFolder, svnLoadExternal, ignoreMappings);
			
		}
		

		@Provides
		@Singleton
		protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
				@Named("LocalIRIMapper") @Nullable IRIMapper localIRIMapper,
				@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
				@Named("SVNAwareIRIMapperCatalogXML") String catalogXML,
				@Named("SVNAwareIRIMapperMappedIRIs") Map<IRI, String> mappedIRIs,
				@Named("SVNAwareIRIMapperWorkFolder") @Nullable String workFolder,
				@Named("SVNAwareIRIMapperSVNConfigDir") File svnConfigDir,
				@Named("SVNAwareIRIMapperSVNLoadExternal") Boolean svnLoadExternal)
		{
			final File workFolderFile = new File(workFolder);
			final SvnTool svn = SvnTool.createAnonymousSVN(workFolderFile, repositoryURL, svnConfigDir, svnLoadExternal);
			List<String> checkout = new ArrayList<String>(mappedIRIs.values());
			SvnIRIMapper svnMapper = new SvnIRIMapper(fallbackIRIMapper, svn, checkout, mappedIRIs, catalogXML);
			if (localIRIMapper == null) {
				return svnMapper;
			}
			return new IRIMapperSwitch(localIRIMapper, svnMapper);
		}
	}
	
	static class PasswordSvnAwareXMLReloadingOntologyModule extends SvnAwareXMLReloadingOntologyModule {

		private final String svnUsername;

		PasswordSvnAwareXMLReloadingOntologyModule(String configFile,
				Properties applicationProperties,
				String repositoryURL,
				Map<IRI, String> mappedIRIs,
				String catalogXML,
				String workFolder,
				String svnUsername,
				boolean svnLoadExternal,
				List<String> ignoreMappings)
		{
			super(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, null, svnLoadExternal, ignoreMappings);
			this.svnUsername = svnUsername;
		}
		

		@Override
		protected void bindAdditional() {
			bind("SVNAwareIRIMapperUsername", svnUsername);
			bindSecret("SVNAwareIRIMapperPassword");
		}


		@Provides
		@Singleton
		protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
				@Named("LocalIRIMapper") @Nullable IRIMapper localIRIMapper,
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
			SvnIRIMapper svnMapper = new SvnIRIMapper(fallbackIRIMapper, svn, checkout, mappedIRIs, catalogXML);
			if (localIRIMapper == null) {
				return svnMapper;
			}
			return new IRIMapperSwitch(localIRIMapper, svnMapper);
		}
	}
	
	static class KeySvnAwareXMLReloadingOntologyModule extends SvnAwareXMLReloadingOntologyModule {

		private final String svnUsername;
		private final String svnKeyFile;
		private final boolean usePassphrase;

		KeySvnAwareXMLReloadingOntologyModule(String configFile,
				Properties applicationProperties,
				String repositoryURL,
				Map<IRI, String> mappedIRIs,
				String catalogXML,
				String workFolder,
				String svnUsername,
				String svnKeyFile,
				boolean svnLoadExternal,
				boolean usePassphrase,
				List<String> ignoreMappings)
		{
			super(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, null, svnLoadExternal, ignoreMappings);
			this.svnUsername = svnUsername;
			this.svnKeyFile = svnKeyFile;
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
		protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
				@Named("LocalIRIMapper") @Nullable IRIMapper localIRIMapper,
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
			SvnIRIMapper svnMapper = new SvnIRIMapper(fallbackIRIMapper, svn, checkout, mappedIRIs, catalogXML);
			if (localIRIMapper == null) {
				return svnMapper;
			}
			return new IRIMapperSwitch(localIRIMapper, svnMapper);
		}
	}
	
	private static class IRIMapperSwitch implements IRIMapper {
		
		private final IRIMapper first;
		private final IRIMapper second;

		/**
		 * @param first
		 * @param second
		 */
		IRIMapperSwitch(IRIMapper first, IRIMapper second) {
			super();
			this.first = first;
			this.second = second;
		}

		@Override
		public IRI getDocumentIRI(IRI ontologyIRI) {
			IRI iri = first.getDocumentIRI(ontologyIRI);
			if (iri != null) {
				return iri;
			}
			return second.getDocumentIRI(ontologyIRI);
		}

		@Override
		public URL mapUrl(String url) {
			URL realUrl = first.mapUrl(url);
			if (realUrl != null) {
				return realUrl;
			}
			return second.mapUrl(url);
		}
		
	}
	
	private static class LocalIRIMapper implements IRIMapper {
		
		private final Map<IRI, File> localMappings;

		LocalIRIMapper(Map<IRI, File> localMappings) {
			this.localMappings = localMappings;
		}

		@Override
		public IRI getDocumentIRI(IRI ontologyIRI) {
			File file = localMappings.get(ontologyIRI);
			if (file != null) {
				return IRI.create(file);
			}
			return null;
		}

		@Override
		public URL mapUrl(String url) {
			return null;
		}
		
	}
}
