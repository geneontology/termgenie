package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.DefaultOntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ReloadingOntologyModule;
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
						bind(OntologyConfiguration.class).to(DefaultOntologyConfiguration.class);
						bind("DefaultOntologyConfigurationResource",
								"go-ontology-configuration.xml");
					}
				};
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultDynamicRulesModule() {

					@Override
					protected void bindTemplateIO() {
						install(new XMLTermTemplateModule());
						bind("DynamicRulesTemplateResource", "termgenie_go_rules.xml");
					}
				};
			}
		};
	}

}
