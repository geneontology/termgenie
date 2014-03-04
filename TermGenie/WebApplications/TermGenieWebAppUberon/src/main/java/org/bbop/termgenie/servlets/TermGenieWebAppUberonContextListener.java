package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;

public class TermGenieWebAppUberonContextListener extends AbstractTermGenieContextListener {

	public TermGenieWebAppUberonContextListener() {
		super("TermGenieWebAppUberonConfigFile");
	}
	
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-uberon", applicationProperties);
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new OntologyModule(applicationProperties, "ontology-configuration_uberon.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_uberon.xml", false, true, applicationProperties);
	}

}
