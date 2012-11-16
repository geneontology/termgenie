package org.bbop.termgenie.services.freeform;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.freeform.FreeFormTermValidatorModule;

public class FreeFormTermServiceModule extends FreeFormTermValidatorModule {

	public FreeFormTermServiceModule(Properties applicationProperties,
			boolean requireLiteratureReference,
			boolean addSubsetTag,
			List<String> oboNamespaces,
			String subset)
	{
		super(applicationProperties, requireLiteratureReference, addSubsetTag, oboNamespaces, subset);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}

}
