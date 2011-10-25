package org.bbop.termgenie.core.ioc;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Wrapper for guice configuration modules. E.g., allow overwrites for some
 * parameters with system properties.
 */
public abstract class IOCModule extends AbstractModule {

	public static String getProperty(String name) {
		String property = System.getProperty("termgenie."+name, null);
		if (property == null) {
			property = System.getProperty("overwrite." + name, null);
		}
		return property;
	}

	/**
	 * Convenience method for binding a {@link String} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, String value) {
		String property = getProperty(name);
		if (property != null) {
			value = property;
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '"+name+"' is null");
		}
		bind(String.class).annotatedWith(Names.named(name)).toInstance(value);
	}

	/**
	 * Convenience method for binding a {@link String} parameter to a system 
	 * parameter.
	 * 
	 * @param name
	 */
	protected void bind(String name) {
		String property = getProperty(name);
		if (property == null) {
			throw new RuntimeException("No system property value found for key: "+name);
		}
		bind(String.class).annotatedWith(Names.named(name)).toInstance(property);
	}

	/**
	 * Convenience method for binding a {@link Long} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, Long value) {
		String property = getProperty(name);
		if (property != null) {
			value = Long.valueOf(property);
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '"+name+"' is null");
		}
		bind(Long.class).annotatedWith(Names.named(name)).toInstance(value);
	}

	/**
	 * Convenience method for binding a {@link TimeUnit} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, TimeUnit value) {
		String property = getProperty(name);
		if (property != null) {
			value = TimeUnit.valueOf(property);
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '"+name+"' is null");
		}
		bind(TimeUnit.class).annotatedWith(Names.named(name)).toInstance(value);
	}

	/**
	 * Convenience method for binding a {@link Integer} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, Integer value) {
		String property = getProperty(name);
		if (property != null) {
			value = Integer.valueOf(property);
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '"+name+"' is null");
		}
		bind(Integer.class).annotatedWith(Names.named(name)).toInstance(value);
	}

	/**
	 * Convenience method for binding a {@link Boolean} parameter, a flag. Check
	 * system properties for overwrites.
	 * 
	 * @param name
	 * @param defaultValue
	 */
	protected void bind(String name, boolean defaultValue) {
		Boolean value = Boolean.valueOf(defaultValue);
		String property = getProperty(name);
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

	/**
	 * Convenience method for binding a {@link File} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, File value) {
		String property = getProperty(name);
		if (property != null) {
			value = new File(property);
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '"+name+"' is null");
		}
		bind(File.class).annotatedWith(Names.named(name)).toInstance(value);
	}
}
