package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.bbop.termgenie.permissions.UserPermissionsModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;

public class TermGenieContextListener extends AbstractTermGenieContextListener {

	public TermGenieContextListener() {
		super("TermGenieJQueryConfigFile");
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new OntologyModule(applicationProperties, "ontology-configuration_simple.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_simple.xml", false, true, false, applicationProperties);
	}

	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie", applicationProperties);
	}

}
