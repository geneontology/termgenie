package org.bbop.termgenie.core.ioc;

/**
 * Module for global configuration flags and parameters.
 */
public class GlobalConfigModule extends IOCModule {

	@Override
	protected void configure() {
		bind("TryResourceLoadAsFiles", false);
	}

}
