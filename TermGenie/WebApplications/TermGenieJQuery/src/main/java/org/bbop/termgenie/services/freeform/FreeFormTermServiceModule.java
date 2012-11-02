package org.bbop.termgenie.services.freeform;

import java.util.Properties;

import org.bbop.termgenie.freeform.FreeFormTermValidatorModule;

public class FreeFormTermServiceModule extends FreeFormTermValidatorModule {

	public FreeFormTermServiceModule(Properties applicationProperties,
			boolean requireLiteratureReference,
			boolean addSubsetTag,
			String subset)
	{
		super(applicationProperties, requireLiteratureReference, addSubsetTag, subset);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}

}
