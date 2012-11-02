package org.bbop.termgenie.freeform;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class FreeFormTermValidatorModule extends IOCModule {

	private final boolean defaultRequireLiteratureReference;
	private final boolean defaultAddSubsetTag;
	private final String defaultSubset;

	public FreeFormTermValidatorModule(Properties applicationProperties,
			boolean requireLiteratureReference,
			boolean addSubsetTag,
			String subset)
	{
		super(applicationProperties);
		this.defaultRequireLiteratureReference = requireLiteratureReference;
		this.defaultAddSubsetTag = addSubsetTag;
		this.defaultSubset = subset;
	}

	@Override
	protected void configure() {
		bind(FreeFormTermValidator.class, FreeFormTermValidatorImpl.class);
		bind(FreeFormTermValidatorImpl.REQ_LIT_REF_PARAM, defaultRequireLiteratureReference);
		bind(FreeFormTermValidatorImpl.ADD_SUBSET_TAG_PARAM, defaultAddSubsetTag);
		bind(FreeFormTermValidatorImpl.SUBSET_PARAM, defaultSubset, true);
	}

}
