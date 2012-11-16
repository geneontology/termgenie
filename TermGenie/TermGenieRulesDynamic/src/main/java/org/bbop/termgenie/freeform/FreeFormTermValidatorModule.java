package org.bbop.termgenie.freeform;

import java.util.List;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

public class FreeFormTermValidatorModule extends IOCModule {

	private final boolean defaultRequireLiteratureReference;
	private final boolean defaultAddSubsetTag;
	private final List<String> defaultOboNamespaces;
	private final String defaultSubset;

	public FreeFormTermValidatorModule(Properties applicationProperties,
			boolean requireLiteratureReference,
			boolean addSubsetTag,
			List<String> oboNamespaces,
			String subset)
	{
		super(applicationProperties);
		this.defaultRequireLiteratureReference = requireLiteratureReference;
		this.defaultAddSubsetTag = addSubsetTag;
		this.defaultSubset = subset;
		this.defaultOboNamespaces = oboNamespaces;
	}

	@Override
	protected void configure() {
		bind(FreeFormTermValidator.class, FreeFormTermValidatorImpl.class);
		bind(FreeFormTermValidatorImpl.REQ_LIT_REF_PARAM, defaultRequireLiteratureReference);
		bind(FreeFormTermValidatorImpl.ADD_SUBSET_TAG_PARAM, defaultAddSubsetTag);
		bindList(FreeFormTermValidatorImpl.SUPPORTED_NAMESPACES, defaultOboNamespaces);
		bind(FreeFormTermValidatorImpl.SUBSET_PARAM, defaultSubset, true);
	}

}
