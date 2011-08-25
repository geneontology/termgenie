package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ReloadingOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.rules.DefaultDynamicRulesModule;

public class TermGenieWebAppGOServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = 7176312523891484544L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new ServiceExecutor() {

			@Override
			protected IOCModule getOntologyModule() {
				return new ReloadingOntologyModule() {

					@Override
					protected void bindOntologyConfiguration() {
						bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
						bind("XMLOntologyConfigurationResource", "ontology-configuration_go.xml");
					}
				};
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultDynamicRulesModule() {

					@Override
					protected void bindTemplateIO() {
						install(new XMLTermTemplateModule());
						bind("DynamicRulesTemplateResource", "termgenie_rules_go.xml");
					}
				};
			}
		};
	}

}
