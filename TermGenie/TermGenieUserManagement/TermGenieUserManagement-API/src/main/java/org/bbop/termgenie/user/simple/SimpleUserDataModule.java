package org.bbop.termgenie.user.simple;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;

/**
 * Module containing a simple implementation for a {@link UserDataProvider}.
 */
public class SimpleUserDataModule extends IOCModule {

	public SimpleUserDataModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class).to(SimpleUserDataProvider.class);
	}

}
