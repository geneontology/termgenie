package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;

public class TermGenieContextListener extends AbstractTermGenieContextListener {

	public TermGenieContextListener() {
		super("TermGenieJQueryConfigFile");
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new XMLReloadingOntologyModule("ontology-configuration_simple.xml", null, applicationProperties);
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_simple.xml", false, true, applicationProperties);
	}

	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie", applicationProperties);
	}

}
