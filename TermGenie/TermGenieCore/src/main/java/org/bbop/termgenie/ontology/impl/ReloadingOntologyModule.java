package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;
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

	protected final List<String> ignoreMappings;

	public ReloadingOntologyModule(List<String> ignoreMappings, Properties applicationProperties) {
		super(applicationProperties);
		this.ignoreMappings = ignoreMappings;
	}

	@Override
	protected void bindOntologyLoader() {
		bind(OntologyLoader.class, ReloadingOntologyLoader.class);
		bind("ReloadingOntologyLoaderPeriod", new Long(6L));
		bind("ReloadingOntologyLoaderTimeUnit", TimeUnit.HOURS);
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class, FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", new File(FileUtils.getTempDirectory(),"termgenie-download-cache").getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
		bindList("FileCachingIRIMapperIgnoreMappings", ignoreMappings, true);
	}

	@Override
	protected Set<String> getDefaultOntologyLoaderSkipOntologies() {
		return Collections.emptySet();
	}

}
