package org.bbop.termgenie.ontology.impl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.ontology.OntologyLoader;

/**
 * Ontology Module, which priodically reloads ontologies from the source.
 */
public class ReloadingOntologyModule extends DefaultOntologyModule {

	@Override
	protected void bindOntologyLoader() {
		bind(OntologyLoader.class).to(ReloadingOntologyLoader.class);
		bind("ReloadingOntologyLoaderPeriod", new Long(6L));
		bind("ReloadingOntologyLoaderTimeUnit", TimeUnit.HOURS);
	}

	@Override
	protected void bindIRIMapper() {
		bind(IRIMapper.class).to(FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", FileUtils.getTempDirectory().getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", new Long(6L));
		bind("FileCachingIRIMapperTimeUnit", TimeUnit.HOURS);
	}

	@Override
	protected Set<String> getDefaultOntologyLoaderSkipOntologies() {
		return Collections.emptySet();
	}

}
