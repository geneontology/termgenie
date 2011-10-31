package org.bbop.termgenie.core.ioc;

import java.util.Properties;

/**
 * Module for global configuration flags and parameters.
 */
public class GlobalConfigModule extends IOCModule {

	protected GlobalConfigModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind("TryResourceLoadAsFiles", false);
	}

}
