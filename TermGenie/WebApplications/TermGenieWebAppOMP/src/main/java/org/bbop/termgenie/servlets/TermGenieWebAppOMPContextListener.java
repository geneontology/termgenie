package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.OntologyModule;
import org.bbop.termgenie.permissions.UserPermissionsModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;

public class TermGenieWebAppOMPContextListener extends AbstractTermGenieContextListener {

	public TermGenieWebAppOMPContextListener() {
		super("TermGenieWebAppOMPConfigFile");
	}
	
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-omp", applicationProperties);
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		return new OntologyModule(applicationProperties, "ontology-configuration_omp.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_omp.xml", false, true, applicationProperties);
	}

}
