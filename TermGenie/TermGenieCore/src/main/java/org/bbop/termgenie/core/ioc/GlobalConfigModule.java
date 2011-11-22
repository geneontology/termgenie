package org.bbop.termgenie.core.ioc;

import java.util.Properties;

/**
 * Module for global configuration flags and parameters.
 */
public class GlobalConfigModule extends IOCModule {
	
	public static final String TryResourceLoadAsFilesName = "TryResourceLoadAsFiles";

	protected GlobalConfigModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(TryResourceLoadAsFilesName, false);
	}

}
