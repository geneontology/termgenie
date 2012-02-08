package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class CatalogXmlSvnAwareXMLReloadingOntologyModule extends XMLReloadingOntologyModule {

	private final String repositoryURLDefault;
	private final String workFolderDefault;
	private final String checkoutDefault;
	private final String catalogXmlDefault;

	/**
	 * @param configFile
	 * @param applicationProperties
	 */
	public CatalogXmlSvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties)
	{
		this(configFile, applicationProperties, null, null, null, null);
	}

	/**
	 * @param configFile
	 * @param applicationProperties
	 * @param repositoryURL
	 * @param workFolder
	 * @param checkout
	 * @param catalogXml 
	 */
	public CatalogXmlSvnAwareXMLReloadingOntologyModule(String configFile,
			Properties applicationProperties,
			String repositoryURL,
			String workFolder,
			String checkout,
			String catalogXml)
	{
		super(configFile, applicationProperties);
		this.repositoryURLDefault = repositoryURL;
		this.checkoutDefault = checkout;
		this.catalogXmlDefault = catalogXml;
		if (workFolder != null) {
			this.workFolderDefault = workFolder;
		}
		else {
			this.workFolderDefault = new File(FileUtils.getTempDirectory(), "termgenie-svn-catalog-xml-iri-mapper-folder").getAbsolutePath();
		}
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, "FallbackIRIMapper", FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache",
				new File(FileUtils.getTempDirectory(), "termgenie-download-cache").getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);

		bind("CatalogXmlSvnAwareIRIMapperRepositoryURL", repositoryURLDefault);
		bind("CatalogXmlSvnAwareIRIMapperWorkFolder", workFolderDefault);
		bind("CatalogXmlSvnAwareIRIMapperCheckout", checkoutDefault);
		bind("CatalogXmlSvnAwareIRIMapperCatalogXml", catalogXmlDefault);
	}

	@Provides
	@Singleton
	protected IRIMapper getIRIMapper(@Named("FallbackIRIMapper") IRIMapper fallbackIRIMapper,
			@Named("CatalogXmlSvnAwareIRIMapperRepositoryURL") String repositoryURL,
			@Named("CatalogXmlSvnAwareIRIMapperWorkFolder") String workFolder,
			@Named("CatalogXmlSvnAwareIRIMapperCheckout") String checkout,
			@Named("CatalogXmlSvnAwareIRIMapperCatalogXml") String catalogXml)
	{
		SvnTool svnTool = SvnTool.createAnonymousSVN(new File(workFolder), repositoryURL);

		return new CatalogXmlSvnAwareIRIMapper(fallbackIRIMapper, svnTool, checkout, catalogXml);
	}

}
