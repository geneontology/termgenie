package org.bbop.termgenie.freeform;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class FreeFormTermValidatorModule extends IOCModule {

	private final boolean defaultAddSubsetTag;
	private final List<String> defaultOboNamespaces;
	private final String defaultSubset;
	private final List<String> defaultAdditionalRelations;

	public FreeFormTermValidatorModule(Properties applicationProperties,
			boolean addSubsetTag,
			List<String> oboNamespaces,
			String subset,
			List<String> additionalRelations)
	{
		super(applicationProperties);
		this.defaultAddSubsetTag = addSubsetTag;
		this.defaultSubset = subset;
		this.defaultOboNamespaces = oboNamespaces;
		this.defaultAdditionalRelations = additionalRelations;
	}

	@Override
	protected void configure() {
		bind(FreeFormTermValidator.class, FreeFormTermValidatorImpl.class);
		bind(FreeFormTermValidatorImpl.ADD_SUBSET_TAG_PARAM, defaultAddSubsetTag);
		bindList(FreeFormTermValidatorImpl.SUPPORTED_NAMESPACES, defaultOboNamespaces, true);
		bind(FreeFormTermValidatorImpl.SUBSET_PARAM, defaultSubset, true);
		bindList(FreeFormTermValidatorImpl.ADDITIONAL_RELATIONS, defaultAdditionalRelations, true);
	}

}
