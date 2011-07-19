package org.bbop.termgenie.core.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public abstract class IOCModule extends AbstractModule {


	/**
	 * Convinence method for binding a String parameter.
	 * 
	 * @param name
	 * @param value
	 */
	protected void bind(String name, String value) {
		bind(String.class).annotatedWith(Names.named(name)).toInstance(value);
	}
	
}
