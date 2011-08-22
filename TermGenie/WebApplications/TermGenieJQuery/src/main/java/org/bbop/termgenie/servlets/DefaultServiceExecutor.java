package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.rules.DefaultXMLDynamicRulesModule;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.tools.TermGenieToolsModule;

public class DefaultServiceExecutor extends ServiceExecutor {

	@Override
	protected IOCModule[] getConfiguration() {
		return new IOCModule[] { new DefaultOntologyModule(),
				new DefaultXMLDynamicRulesModule(),
				new TermGenieToolsModule(),
				new TermGenieServiceModule(),
				new ReasonerModule() };
	}
}
