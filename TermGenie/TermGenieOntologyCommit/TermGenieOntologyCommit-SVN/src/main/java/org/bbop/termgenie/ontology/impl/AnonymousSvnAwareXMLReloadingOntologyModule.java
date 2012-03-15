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

public class AnonymousSvnAwareXMLReloadingOntologyModule extends AbstractSvnAwareXMLReloadingOntologyModule {

	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param repositoryURL
	 * @param remoteTargetFile
	 * @param mappedIRI
	 * @param workFolder
	 */
	public AnonymousSvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			String remoteTargetFile,
			String mappedIRI,
			String workFolder)
	{
		super(configFile, applicationProperties, repositoryURL, remoteTargetFile, mappedIRI, workFolder);

	}
	
	@Provides
	@Singleton
	protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
			@Named("SVNAwareIRIMapperRepositoryURL") String repositoryURL,
			@Named("SVNAwareIRIMapperRemoteTargetFile") String remoteTargetFile,
			@Named("SVNAwareIRIMapperMappedIRI") String mappedIRI,
			@Named("SVNAwareIRIMapperWorkFolder") String workFolder)
	{
		Map<String, String> mappedCVSFiles = Collections.singletonMap(mappedIRI, remoteTargetFile);
		final File workFolderFile = new File(workFolder);
		final SvnTool svn = SvnTool.createAnonymousSVN(workFolderFile, repositoryURL);
		return new SvnAwareIRIMapper(fallbackIRIMapper, svn, workFolderFile, mappedCVSFiles, remoteTargetFile);
	}
}
