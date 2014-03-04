package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.GlobalConfigModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.impl.FileCachingIRIMapper.FileCachingFilter;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.tools.ResourceLoader;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Abstract module providing the default ontologies.
 * 
 * @see OntologyLoader
 * @see OntologyConfiguration
 * @see OWLOntologyIRIMapper
 */
public class OntologyModule extends IOCModule {

	private static final String xmlOntologyConfigurationResourceName = "XMLOntologyConfigurationResource";
	
	private final String ontologyConfigurationFile;
	
	private File fileCache = new File(FileUtils.getTempDirectory(),"termgenie-download-cache").getAbsoluteFile();
	private Long fileCachePeriod = 6L;
	private TimeUnit fileCacheUnit = TimeUnit.HOURS;
	private List<String> ignoreMappings = null;
	private FileCachingFilter cacheFilter = null;

	public OntologyModule(Properties applicationProperties, String ontologyConfigurationFile) {
		super(applicationProperties);
		this.ontologyConfigurationFile = ontologyConfigurationFile;
	}
	
	public OntologyModule(String ontologyConfigurationFile) {
		this(null, ontologyConfigurationFile);
	}
	
	public void setFileCacheIgnoreMappings(List<String> ignoreMappings) {
		this.ignoreMappings = ignoreMappings;
	}

	public void setFileCacheIgnoreMappings(String...ignoreMappings) {
		setFileCacheIgnoreMappings(Arrays.asList(ignoreMappings));
	}
	
	public void setFileCacheFilter(FileCachingFilter cacheFilter) {
		this.cacheFilter = cacheFilter;
	}
	
	public void setFileCache(File fileCache) {
		this.fileCache = fileCache;
	}

	public void setFileCachePeriod(Long fileCachePeriod) {
		this.fileCachePeriod = fileCachePeriod;
	}

	public void setFileCacheUnit(TimeUnit fileCacheUnit) {
		this.fileCacheUnit = fileCacheUnit;
	}

	@Override
	protected void configure() {
		bindOntologyLoader();

		bindOntologyConfiguration();

		bindIRIMappers();
	}

	protected void bindOntologyLoader() {
		bind(OntologyLoader.class, ReloadingOntologyLoader.class);
		bind("ReloadingOntologyLoaderPeriod", new Long(6L));
		bind("ReloadingOntologyLoaderTimeUnit", TimeUnit.HOURS);
	}

	protected void bindOntologyConfiguration() {
		bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
		bind("XMLOntologyConfigurationResource", ontologyConfigurationFile);
	}
	
	protected void bindIRIMappers() {
		bind(OWLOntologyIRIMapper.class, "FileCachingIRIMapper", FileCachingIRIMapper.class);
		bind("FileCachingIRIMapperLocalCache", fileCache.getAbsolutePath());
		bind("FileCachingIRIMapperPeriod", fileCachePeriod);
		bind("FileCachingIRIMapperTimeUnit", fileCacheUnit);
		
		if (cacheFilter != null) {
			bind(FileCachingFilter.class, cacheFilter);
		}
		else if (ignoreMappings != null && !ignoreMappings.isEmpty()) {
			final Set<IRI> ignoreIRIs = new HashSet<IRI>();
			cacheFilter = new FileCachingIgnoreFilter(ignoreIRIs);
			bind(FileCachingFilter.class, cacheFilter);
		}
	}

	@Provides
	@Named("IRIMappers")
	@Singleton
	protected final List<OWLOntologyIRIMapper> provideIRIMappers(
			PrimaryIRIMapperHolder primaryIRIMapperHolder,
			@Named("DefaultIRIMappers") List<OWLOntologyIRIMapper> defaultMappers)
	{
		if (primaryIRIMapperHolder.primaryMapper != null) {
			List<OWLOntologyIRIMapper> allMappers = new ArrayList<OWLOntologyIRIMapper>(defaultMappers.size() + 1);
			allMappers.addAll(defaultMappers);
			allMappers.add(primaryIRIMapperHolder.primaryMapper);
			return allMappers;
		}
		return defaultMappers;
	}
	
	static class PrimaryIRIMapperHolder {
		@Inject(optional=true) @Named("PrimaryIRIMapper") OWLOntologyIRIMapper primaryMapper = null;
	}
	
	@Provides
	@Named("DefaultIRIMappers")
	@Singleton
	protected List<OWLOntologyIRIMapper> provideIRIMappers(
			@Named("FileCachingIRIMapper") OWLOntologyIRIMapper fileMapper)
	{
		return Arrays.asList(fileMapper);
	}

	@Override
	public List<Pair<String, String>> getAdditionalData(Injector injector) {
		// this adds the ontology configuration file details to the management console
		Key<String> configKey = Key.get(String.class, Names.named(xmlOntologyConfigurationResourceName));
		String configFile = injector.getInstance(configKey);
		Key<Boolean> loadAsFileKey = Key.get(Boolean.class, Names.named(GlobalConfigModule.TryResourceLoadAsFilesName));
		XMLConfigReader reader = new XMLConfigReader(injector.getInstance(loadAsFileKey));
		String config = reader.loadXmlConfig(configFile);
		if (config != null) {
			return Collections.singletonList(new Pair<String, String>(configFile, config));
		}
		return null;
	}
	
	private static class XMLConfigReader extends ResourceLoader {
		
		private static final Logger logger = Logger.getLogger(XMLConfigReader.class);

		XMLConfigReader(boolean tryLoadAsFiles) {
			super(tryLoadAsFiles);
		}
		
		String loadXmlConfig(String resource) {
			InputStream stream = null;
			try {
				stream = loadResourceSimple(resource);
				if (stream != null) {
					return IOUtils.toString(stream);
				}
				logger.warn("Missing resource: "+resource);
			} catch (IOException exception) {
				logger.warn("Could not read from resource: "+resource, exception);
			}
			finally {
				IOUtils.closeQuietly(stream);
			}
			return null;
		}
	}
}
