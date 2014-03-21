package org.bbop.termgenie.core.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.Message;

/**
 * Provide an application specific {@link Injector}, which always loads first
 * the {@link GlobalConfigModule}.
 */
public class TermGenieGuice {
	
	private static Logger logger = Logger.getLogger(TermGenieGuice.class);

	private TermGenieGuice() {
		// no instance, use static methods only
	}

	/**
	 * Create a new {@link Injector} for the given modules.
	 * 
	 * @param modules
	 * @return injector
	 */
	public static Injector createInjector(IOCModule...modules) {
		return createWebInjector(null, null, modules);
	}

	/**
	 * Create a new {@link Injector} for the given modules.
	 * 
	 * @param servletModule
	 * @param applicationProperties
	 * @param modules
	 * @return injector
	 */
	public static Injector createWebInjector(Module servletModule,
			Properties applicationProperties,
			IOCModule...modules)
	{
		List<Module> allModules = new ArrayList<Module>(modules.length + 1);
		allModules.add(new GlobalConfigModule(applicationProperties));
		for (Module module : modules) {
			allModules.add(module);
		}
		if (servletModule != null) {
			allModules.add(servletModule);
		}
		try {
			return Guice.createInjector(allModules);
		} catch (CreationException exception) {
			Collection<Message> errorMessages = exception.getErrorMessages();
			StringBuilder sb = new StringBuilder("Could not create injector:");
			for (Message message : errorMessages) {
				sb.append('\n').append('\t').append(message.getMessage());
			}
			logger.error(sb.toString());
			throw exception;
		}
	}
}
