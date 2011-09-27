package org.bbop.termgenie.core.ioc;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Provide an application specific {@link Injector}, which always loads first
 * the {@link GlobalConfigModule}.
 */
public class TermGenieGuice {

	/**
	 * Create a new {@link Injector} for the given modules.
	 * 
	 * @param modules
	 * @return injector
	 */
	public static Injector createInjector(IOCModule...modules) {
		return createWebInjector(null, modules);
	}

	/**
	 * Create a new {@link Injector} for the given modules.
	 * 
	 * @param servletModule
	 * @param modules
	 * @return injector
	 */
	public static Injector createWebInjector(Module servletModule, IOCModule...modules) {
		List<Module> allModules = new ArrayList<Module>(modules.length + 1);
		allModules.add(new GlobalConfigModule());
		for (Module module : modules) {
			allModules.add(module);
		}
		if (servletModule != null) {
			allModules.add(servletModule);
		}
		return Guice.createInjector(allModules);
	}
}
