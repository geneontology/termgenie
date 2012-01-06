package org.bbop.termgenie.services.resources;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.services.resources.ResourceProviderConfiguration.ConfiguredResourceProvider;
import org.bbop.termgenie.services.resources.ResourceProviderConfiguration.EmptyResourceProviderConfiguration;

public abstract class ResourceProviderModule extends IOCModule {

	public ResourceProviderModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(ResourceProviderService.class, ResourceProviderServiceImpl.class);
		bindResourceProviderConfiguration();
	}

	protected abstract void bindResourceProviderConfiguration();
	
	public static class EmptyResourceProviderModule extends ResourceProviderModule {
	
		public EmptyResourceProviderModule(Properties applicationProperties) {
			super(applicationProperties);
		}

		@Override
		protected void bindResourceProviderConfiguration() {
			bind(ResourceProviderConfiguration.class, EmptyResourceProviderConfiguration.class);
		}
	}
	
	public static class ConfiguredResourceProviderModule extends ResourceProviderModule {
		
		public ConfiguredResourceProviderModule(Properties applicationProperties) {
			super(applicationProperties);
		}

		@Override
		protected void bindResourceProviderConfiguration() {
			bind("ConfiguredResourceProviderFile");
			bind(ResourceProviderConfiguration.class, ConfiguredResourceProvider.class);
		}
		
		
	}
}
