package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class PasswordSvnAwareXMLReloadingOntologyModule extends AbstractSvnAwareXMLReloadingOntologyModule {

	private final String svnUserName;

	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param repositoryURL
	 * @param remoteTargetFile
	 * @param mappedIRI
	 * @param workFolder
	 * @param svnUserName
	 */
	public PasswordSvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			String remoteTargetFile,
			String mappedIRI,
			String workFolder,
			String svnUserName)
	{
		super(configFile, applicationProperties, repositoryURL, remoteTargetFile, mappedIRI, workFolder);
		this.svnUserName = svnUserName;

	}
	
	@Override
	protected void bindIRIMapper() {
		super.bindIRIMapper();
		bind("SVNAwareIRIMapperUsername", svnUserName);
		bindSecret("SVNAwareIRIMapperPassword");
	}

	@Provides
	@Singleton
	protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
			@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
			@Named("SVNAwareIRIMapperRemoteTargetFile") String remoteTargetFile,
			@Named("SVNAwareIRIMapperMappedIRI") String mappedIRI,
			@Named("SVNAwareIRIMapperWorkFolder") String workFolder,
			@Named("SVNAwareIRIMapperUsername") String svnUsername,
			@Named("SVNAwareIRIMapperPassword") String svnPassword,
			@Named("SVNAwareIRIMapperSVNConfigDir") File svnConfigDir)
	{
		Map<String, String> mappedCVSFiles = Collections.singletonMap(mappedIRI, remoteTargetFile);
		final File workFolderFile = new File(workFolder);
		final SvnTool svn = SvnTool.createUsernamePasswordSVN(workFolderFile, repositoryURL, svnUsername, svnPassword, svnConfigDir);
		return new SvnAwareIRIMapper(fallbackIRIMapper, svn, workFolderFile, mappedCVSFiles, remoteTargetFile);
	}
}
