package org.bbop.termgenie.core.ioc;

import java.util.concurrent.TimeUnit;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Wrapper for guice configuration modules. E.g., allow overwrites 
 * for some parameters with system properties. 
 *
 */
public abstract class IOCModule extends AbstractModule {

	/**
	 * Convinence method for binding a {@link String} parameter. 
	 * Check system properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, String value) {
		String property = System.getProperty(name, null);
		if (property != null) {
			value = property;
		}
		bind(String.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
	/**
	 * Convinence method for binding a {@link Long} parameter. 
	 * Check system properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, Long value) {
		String property = System.getProperty(name, null);
		if (property != null) {
			value = Long.valueOf(property);
		}
		bind(Long.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
	/**
	 * Convinence method for binding a {@link TimeUnit} parameter. 
	 * Check system properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, TimeUnit value) {
		String property = System.getProperty(name, null);
		if (property != null) {
			value = TimeUnit.valueOf(property);
		}
		bind(TimeUnit.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
	/**
	 * Convinence method for binding a {@link Integer} parameter. 
	 * Check system properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, Integer value) {
		String property = System.getProperty(name, null);
		if (property != null) {
			value = Integer.valueOf(property);
		}
		bind(Integer.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
	/**
	 * Convinence method for binding a {@link Boolean} parameter, a flag. 
	 * Check system properties for overwrites.
	 * 
	 * @param name
	 * @param defaultValue
	 */
	protected void bind(String name, boolean defaultValue) {
		Boolean value = Boolean.valueOf(defaultValue);
		String property = System.getProperty(name, null);
		if (property != null) {
			property = property.toLowerCase();
			if ("true".equals(property)) {
				value = Boolean.TRUE;
			}
			else if ("false".equals(property)) {
				value = Boolean.FALSE;
			}
		}
		bind(Boolean.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
}
