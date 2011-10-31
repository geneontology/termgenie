package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyLoader;

/**
 * Ontology Module, which periodically reloads ontologies from the source.
 */
public class ReloadingOntologyModule extends DefaultOntologyModule {

	public ReloadingOntologyModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void bindOntologyLoader() {
		bind(OntologyLoader.class).to(ReloadingOntologyLoader.class);
		bind("ReloadingOntologyLoaderPeriod", new Long(6L));
		bind("ReloadingOntologyLoaderTimeUnit", TimeUnit.HOURS);
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class).to(FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", new File(FileUtils.getTempDirectory(),"termgenie-download-cache").getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
	}

	@Override
	protected Set<String> getDefaultOntologyLoaderSkipOntologies() {
		return Collections.emptySet();
	}

}
