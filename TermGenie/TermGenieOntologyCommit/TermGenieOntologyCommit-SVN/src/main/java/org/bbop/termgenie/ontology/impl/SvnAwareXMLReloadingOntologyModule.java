package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class SvnAwareXMLReloadingOntologyModule extends XMLReloadingOntologyModule {

	private final String repositoryURLDefault;
	private final String remoteTargetFileDefault;
	private final String mappedIRIDefault;
	private final String workFolderDefault;

	public SvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties)
	{
		this(configFile, applicationProperties, null, null, null, null);
	}
	
	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param repositoryURL
	 * @param remoteTargetFile
	 * @param mappedIRI
	 * @param workFolder
	 */
	public SvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			String remoteTargetFile,
			String mappedIRI,
			String workFolder)
	{
		super(configFile, applicationProperties);
		this.repositoryURLDefault = repositoryURL;
		this.remoteTargetFileDefault = remoteTargetFile;
		this.mappedIRIDefault = mappedIRI;
		this.workFolderDefault = workFolder;

	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, "FallbackIRIMapper", FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache",
				new File(FileUtils.getTempDirectory(), "termgenie-download-cache").getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);

		bind("SVNAwareIRIMapperRepositoryURL", repositoryURLDefault);
		bind("SVNAwareIRIMapperRemoteTargetFile", remoteTargetFileDefault);
		bind("SVNAwareIRIMapperMappedIRI", mappedIRIDefault);
		bind("SVNAwareIRIMapperWorkFolder", workFolderDefault);
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
		return new SvnAwareIRIMapper(fallbackIRIMapper, repositoryURL, new File(workFolder), mappedCVSFiles, remoteTargetFile);
	}
}
