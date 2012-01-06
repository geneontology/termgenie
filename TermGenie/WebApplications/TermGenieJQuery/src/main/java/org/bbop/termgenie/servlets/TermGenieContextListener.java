package org.bbop.termgenie.servlets;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.resources.ResourceProviderModule;
import org.bbop.termgenie.services.resources.ResourceProviderModule.ConfiguredResourceProviderModule;

public class TermGenieContextListener extends AbstractTermGenieContextListener {

	public TermGenieContextListener() {
		super((Properties) null);
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new XMLReloadingOntologyModule("ontology-configuration_simple.xml", applicationProperties);
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_simple.xml", applicationProperties);
	}

	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie", applicationProperties);
	}

	@Override
	protected ResourceProviderModule getResourceProviderModule() {
		return new ConfiguredResourceProviderModule(applicationProperties);
	}

	
}
