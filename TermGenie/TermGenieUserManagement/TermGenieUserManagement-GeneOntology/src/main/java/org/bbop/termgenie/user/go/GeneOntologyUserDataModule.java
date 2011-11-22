package org.bbop.termgenie.user.go;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.user.UserDataProvider;


public class GeneOntologyUserDataModule extends IOCModule {

	public GeneOntologyUserDataModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(UserDataProvider.class, GeneOntologyUserDataProvider.class);
	}

}
