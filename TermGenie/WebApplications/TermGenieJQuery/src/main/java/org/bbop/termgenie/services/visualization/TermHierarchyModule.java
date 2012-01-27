package org.bbop.termgenie.services.visualization;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;


public class TermHierarchyModule extends IOCModule {

	public TermHierarchyModule(Properties applicationProperties) {
		super(applicationProperties);
	}

	@Override
	protected void configure() {
		bind(TermHierarchyRenderer.class, TermHierarchyRendererImpl.class);
	}

}
