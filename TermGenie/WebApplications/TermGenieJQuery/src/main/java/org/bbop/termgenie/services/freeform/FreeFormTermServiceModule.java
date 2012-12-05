package org.bbop.termgenie.services.freeform;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.freeform.FreeFormTermValidatorModule;

public class FreeFormTermServiceModule extends FreeFormTermValidatorModule {
	
	private final List<String> xrefResources;

	public FreeFormTermServiceModule(Properties applicationProperties,
			boolean addSubsetTag,
			List<String> oboNamespaces,
			List<String> xrefResources,
			String subset)
	{
		super(applicationProperties, addSubsetTag, oboNamespaces, subset);
		this.xrefResources = xrefResources;
	}

	@Override
	protected void configure() {
		super.configure();
		bindList("FreeFormXrefResources", xrefResources, true);
		bind(FreeFormTermService.class, FreeFormTermServiceImpl.class);
	}

}
