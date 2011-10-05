package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;

public class TermGenieWebAppUberonContextListener extends AbstractTermGenieContextListener {

	@Override
	protected IOCModule getOntologyModule() {
		return new XMLReloadingOntologyModule("ontology-configuration_uberon.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_uberon.xml");
	}

	@Override
	protected IOCModule getReasoningModule() {
		return new ReasonerModule("hermit");
	}
}
