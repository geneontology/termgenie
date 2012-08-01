package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;
import org.semanticweb.owlapi.model.IRI;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class CvsAwareXMLReloadingOntologyModule extends XMLReloadingOntologyModule {

	private final String cvsRootDefault;
	private final String remoteTargetFileDefault;
	private final String mappedIRIDefault;
	private final String workFolderDefault;
	
	/**
	 * @param configFile
	 * @param applicationProperties
	 */
	public CvsAwareXMLReloadingOntologyModule(String configFile, Properties applicationProperties) {
		this(configFile, applicationProperties, null, null, null, null);
	}
	
	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param cvsRoot
	 * @param remoteTargetFile
	 * @param mappedIRI
	 * @param workFolder
	 */
	public CvsAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String cvsRoot,
			String remoteTargetFile,
			String mappedIRI,
			String workFolder)
	{
		super(configFile, null, applicationProperties);
		this.cvsRootDefault = cvsRoot;
		this.remoteTargetFileDefault = remoteTargetFile;
		this.mappedIRIDefault = mappedIRI;
		if (workFolder != null) {
			this.workFolderDefault = workFolder;
		}
		else {
			this.workFolderDefault = new File(FileUtils.getTempDirectory(), "termgenie-cvs-iri-mapper-folder").getAbsolutePath();
		}
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, "FallbackIRIMapper", FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", new File(FileUtils.getTempDirectory(), "termgenie-download-cache").getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
		
		bind("CVSAwareIRIMapperCVSRoot", cvsRootDefault);
		bind("CVSAwareIRIMapperRemoteTargetFile", remoteTargetFileDefault);
		bind("CVSAwareIRIMapperMappedIRI", mappedIRIDefault);
		bind("CVSAwareIRIMapperWorkFolder", workFolderDefault);
	}

	@Provides
	@Singleton
	protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
			@Named("CVSAwareIRIMapperCVSRoot") String cvsRoot,
			@Named("CVSAwareIRIMapperRemoteTargetFile") String remoteTargetFile,
			@Named("CVSAwareIRIMapperMappedIRI") String mappedIRI,
			@Named("CVSAwareIRIMapperWorkFolder") String workFolder)
	{
		int i = remoteTargetFile.lastIndexOf('/');
		if (i <= 1 || i >= (remoteTargetFile.length() - 1)) {
			throw new RuntimeException("Could not process remote target file: " + remoteTargetFile);
		}
		String checkout = remoteTargetFile.substring(0, i);
		Map<IRI, String> mappedCVSFiles = Collections.singletonMap(IRI.create(mappedIRI), remoteTargetFile);

		String password = null; // assume anonymous access
		return new CvsIRIMapper(fallbackIRIMapper, cvsRoot, password, new File(workFolder), checkout, mappedCVSFiles, null);
	}

}
