package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;

public class TermGenieWebAppCLContextListener extends AbstractTermGenieContextListener {

	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-cl");
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new XMLReloadingOntologyModule("ontology-configuration_cl.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_cl.xml");
	}

}
