package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.rules.DefaultXMLDynamicRulesModule;

public class JsonRPCServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = -3052651034871303985L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new ServiceExecutor() {

			@Override
			protected IOCModule getOntologyModule() {
				return new DefaultOntologyModule();
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultXMLDynamicRulesModule();
			}
		};
	}
}
