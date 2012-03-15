package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;

public abstract class AbstractSvnAwareXMLReloadingOntologyModule extends XMLReloadingOntologyModule {

	private final String repositoryURLDefault;
	private final String remoteTargetFileDefault;
	private final String mappedIRIDefault;
	private final String workFolderDefault;

	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param repositoryURL
	 * @param remoteTargetFile
	 * @param mappedIRI
	 * @param workFolder
	 */
	protected AbstractSvnAwareXMLReloadingOntologyModule(String configFile,
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

}
