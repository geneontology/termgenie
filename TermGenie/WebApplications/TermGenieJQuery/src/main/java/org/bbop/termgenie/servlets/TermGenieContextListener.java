package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;

public class TermGenieContextListener extends AbstractTermGenieContextListener {

	@Override
	protected IOCModule getOntologyModule() {
		return new XMLReloadingOntologyModule("ontology-configuration_simple.xml");
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_simple.xml");
	}

	@Override
	protected IOCModule getReasoningModule() {
		return new ReasonerModule("hermit");
	}

}
