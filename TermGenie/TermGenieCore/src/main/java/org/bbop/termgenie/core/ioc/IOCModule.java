package org.bbop.termgenie.core.ioc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.IRI;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * Wrapper for guice configuration modules. E.g., allow overwrites for some
 * parameters with system properties.
 */
public abstract class IOCModule extends AbstractModule {
	
	private final static List<IOCModule> allModules = new ArrayList<IOCModule>();

	protected final Properties applicationProperties;
	
	private final Map<String, String> configuredParameters = new HashMap<String, String>();
	private final Map<Class<?>, Class<?>> boundClasses = new HashMap<Class<?>, Class<?>>();
	private final List<Pair<Method, String>> providesClasses = new ArrayList<Pair<Method,String>>();
	
	/**
	 * @param applicationProperties
	 */
	protected IOCModule(Properties applicationProperties) {
		super();
		this.applicationProperties = applicationProperties;
		allModules.add(this);
		addProvidedClasses(providesClasses, this);
	}
	
	private static void addProvidedClasses(List<Pair<Method, String>> provided, Object instance) {
		Method[] methods = instance.getClass().getDeclaredMethods();
		if (methods != null) {
			for (Method method : methods) {
				Provides annotation = method.getAnnotation(Provides.class);
				if (annotation != null) {
					String name = null;
					Named named = method.getAnnotation(Named.class);
					if (named != null) {
						name = named.value();
					}
					provided.add(Pair.of(method, name));
				}
			}
		}
	}

	/**
	 * Load the global system properties configuration for the given application
	 * name.<br>
	 * Expect a system property called 'termgenie.'+name with the path to the
	 * configuration file. If this is not set an empty properties object will be
	 * returned. If the file exists, but cannot be fully read an
	 * {@link RuntimeException} is thrown.
	 * 
	 * @param name application name
	 * @return {@link Properties}, never null
	 */
	public static Properties getGlobalSystemProperties(String name) {
		final Logger logger = Logger.getLogger(IOCModule.class);
		logger.info("Checking for global application configuration properties file: "+name);
		String property = System.getProperty("termgenie." + name, null);
		Properties applicationProperties = new Properties();
		if (property != null) {
			File propertyFile = new File(property);
			if (propertyFile.isFile() && propertyFile.canRead()) {
				logger.info("Start loading global propertyFile: "+propertyFile.getAbsolutePath());
				try {
					applicationProperties.load(new FileInputStream(propertyFile));
				} catch (IOException exception) {
					logger.error("Could not load global propertyFile: "+propertyFile.getAbsolutePath()+" Exception: "+exception.getMessage());
					throw new RuntimeException(exception);
				}
			}
			else {
				String message = "Could not load global propertyFile: "+propertyFile.getAbsolutePath();
				logger.error(message);
				throw new RuntimeException(message);
			}
		}
		else {
			logger.warn("No system property found for key: "+name);
		}
		return applicationProperties;
	}

	public static String getProperty(String name, Properties applicationProperties) {
		String property = null;
		if (applicationProperties != null && !applicationProperties.isEmpty()) {
			property = applicationProperties.getProperty(name, null);
		}
		return property;
	}

	public String getProperty(String name) {
		return getProperty(name, applicationProperties);
	}

	/**
	 * Convenience method for binding a {@link String} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, String value) {
		bind(name, value, false, false);
	}
	
	/**
	 * Convenience method for binding a {@link String} parameter to a null value.<br>
	 * Warning this will only work if the target has been annotated with {@link javax.annotation.Nullable}.
	 * 
	 * @param name
	 */
	protected void bindNull(String name) {
		bind(String.class).annotatedWith(Names.named(name)).toProvider(Providers.<String>of(null));
	}
	
	/**
	 * Convenience method for binding a {@link String} parameter. Check system
	 * properties for overwrites. Ignore null values, if optional is true.
	 * 
	 * @param name
	 * @param value
	 * @param optional
	 */
	protected void bind(String name, String value, boolean optional) {
		bind(name, value, optional, false);
	}
	
	/**
	 * Convenience method for binding a {@link String} parameter. Check system
	 * properties for overwrites. Allow null values, if optional is true.
	 * Register null value if useNull is true.
	 * 
	 * @param name
	 * @param value
	 * @param optional
	 * @param useNull
	 */
	protected void bind(String name, String value, boolean optional, boolean useNull) {
		String property = getProperty(name);
		if (property != null) {
			value = property;
		}
		if (value == null) {
			if(!optional) {
				Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
				throw new RuntimeException("No value found for key: " + name);
			}
			if (useNull) {
				bindNull(name);
				configuredParameters.put(name, value);
			}
		}
		else {
			bind(String.class).annotatedWith(Names.named(name)).toInstance(value);
			configuredParameters.put(name, value);
		}
	}
	
	/**
	 * Convenience method for binding a {@link Character} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, Character value) {
		String property = getProperty(name);
		if (property != null && property.length() == 1) {
			value = Character.valueOf(property.charAt(0));
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for key: " + name);
		}
		bind(Character.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.toString());
	}

	/**
	 * Convenience method for binding a {@link String} parameter to a system
	 * parameter.
	 * 
	 * @param name
	 */
	protected void bind(String name) {
		bindPropertyValue(name, false);
	}
	
	/**
	 * Convenience method for binding a {@link String} parameter to a system
	 * parameter. The value will not be shown in the management console. 
	 * 
	 * @param name
	 */
	protected void bindSecret(String name) {
		bindPropertyValue(name, true);
	}
	
	private void bindPropertyValue(String name, boolean secret) {
		String property = getProperty(name);
		if (property == null) {
			throw new RuntimeException("No system property value found for key: " + name);
		}
		bind(String.class).annotatedWith(Names.named(name)).toInstance(property);
		configuredParameters.put(name, secret ? "*******" : property);
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
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for long key: " + name);
		}
		bind(Long.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.toString());
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
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for time unit key: " + name);
		}
		bind(TimeUnit.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.toString());
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
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for integer key: " + name);
		}
		bind(Integer.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.toString());
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
			else {
				throw new RuntimeException("No valid value found for boolean key: " + name);
			}
		}
		bind(Boolean.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.toString());
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
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for file key: " + name);
		}
		bind(File.class).annotatedWith(Names.named(name)).toInstance(value);
		configuredParameters.put(name, value.getAbsolutePath());
	}
	
	/**
	 * Convenience method for binding a Map<IRI, String> parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bindIRIMap(String name, Map<IRI, String> value) {
		String property = getProperty(name);
		if (property != null) {
			property = StringUtils.trimToNull(property);
			if (property != null) {
				String[] split = StringUtils.split(property, ",;");
				value = new HashMap<IRI, String>();
				for (String string : split) {
					String[] pair = StringUtils.split(string, "|");
					if (pair != null && pair.length >= 2) {
						String p1 = pair[0];
						String p2 = pair[1];
						if(p1 != null && p2 != null) {
							p1 = StringUtils.trimToNull(p1);
							p2 = StringUtils.trimToNull(p2);
							if(p1 != null && p2 != null) {
								value.put(IRI.create(p1), p2);
							}
						}
					}
				}
			}
		}
		if (value == null) {
			Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
			throw new RuntimeException("No value found for file key: " + name);
		}
		bind(new TypeLiteral<Map<IRI, String>>() { /* Intentionally empty */}).
					annotatedWith(Names.named(name)).
					toInstance(value);
		configuredParameters.put(name, value.toString());
	}
	
	/**
	 * Convenience method for binding a String {@link List} parameter. Check system
	 * properties for overwrites.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bindList(String name, List<String> value) {
		bindList(name, value, false);
	}

	/**
	 * Convenience method for binding a String {@link List} parameter. Check system
	 * properties for overwrites. Allow null values, if optional is true. 
	 * 
	 * @param name
	 * @param value
	 * @param optional
	 */
	protected void bindList(String name, List<String> value, boolean optional) {
		String property = getProperty(name);
		if (property != null) {
			property = StringUtils.trimToNull(property);
			if (property != null) {
				String[] split = StringUtils.split(property, ",;|");
				value = new ArrayList<String>(split.length);
				for (String string : split) {
					string = StringUtils.trimToNull(string);
					if (string != null) {
						value.add(string);
					}
				}
			}
		}
		if (value == null) {
			if (!optional) {
				Logger.getLogger(getClass()).error("Named value '" + name + "' is null");
				throw new RuntimeException("No value found for file key: " + name);
			}
		}
		else {
			bind(new TypeLiteral<List<String>>() { /* Intentionally empty */}).
					annotatedWith(Names.named(name)).
					toInstance(value);
			configuredParameters.put(name, value.toString());
		}
	}
	
	protected <T> void bindNamedList(String name, List<T> values) {
		bind(new TypeLiteral<List<T>>() { /* Intentionally empty */}).
		annotatedWith(Names.named(name)).
		toInstance(values);
		StringBuilder sb = new StringBuilder();
		for (T value : values) {
			sb.append(value.getClass().getSimpleName()).append(" ");
		}
		configuredParameters.put(name, sb.toString());
	}
	
	protected <T> void bind(Class<T> interfaceClass, Class<? extends T> implementationClass) {
		bind(interfaceClass).to(implementationClass);
		boundClasses.put(interfaceClass, implementationClass);
	}
	
	protected <T> void bind(Class<T> interfaceClass, T implementation) {
		bind(interfaceClass).toInstance(implementation);
		boundClasses.put(interfaceClass, implementation.getClass());
	}
	
	protected <T> void bind(TypeLiteral<T> typeLiteral, Class<? extends T> implementationClass) {
		// use type literal for generic interfaces
		bind(typeLiteral).to(implementationClass);
		boundClasses.put(typeLiteral.getClass(), implementationClass);
	}
	
	protected <T> void bind(Class<T> interfaceClass, String name, Class<? extends T> implementationClass) {
		bind(interfaceClass).annotatedWith(Names.named(name)).to(implementationClass);
		configuredParameters.put(name, interfaceClass.getName()+" to "+implementationClass.getName());
	}
	
	/**
	 * @return map of configured parameters
	 */
	public Map<String, String> getConfiguredParameters() {
		return configuredParameters;
	}
	
	/**
	 * @return the map of bound classes
	 */
	public Map<Class<?>, Class<?>> getBoundClasses() {
		return boundClasses;
	}

	/**
	 * @return the set of provided classes
	 */
	public List<Pair<Method, String>> getProvidesClasses() {
		return providesClasses;
	}

	/**
	 * @return the module name
	 */
	public String getModuleName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * @return the module description or null
	 */
	public String getModuleDescription() {
		return null;
	}
	
	/**
	 * @param injector
	 * @return additional data or null;
	 */
	public List<Pair<String, String>> getAdditionalData(Injector injector) {
		return null;
	}
	
	/**
	 * @return list of all modules
	 */
	public static List<IOCModule> getAllModules() {
		return allModules;
	}
	
}
