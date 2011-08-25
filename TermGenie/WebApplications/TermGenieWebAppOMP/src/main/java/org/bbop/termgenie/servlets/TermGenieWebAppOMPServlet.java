package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ReloadingOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.rules.DefaultDynamicRulesModule;

public class TermGenieWebAppOMPServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = -6343036475184322054L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new ServiceExecutor() {

			@Override
			protected IOCModule getOntologyModule() {
				return new ReloadingOntologyModule() {

					@Override
					protected void bindOntologyConfiguration() {
						bind(OntologyConfiguration.class).to(XMLOntologyConfiguration.class);
						bind("XMLOntologyConfigurationResource", "ontology-configuration_omp.xml");
					}
				};
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultDynamicRulesModule() {

					@Override
					protected void bindTemplateIO() {
						install(new XMLTermTemplateModule());
						bind("DynamicRulesTemplateResource", "termgenie_rules_omp.xml");
					}
				};
			}
			
			@Override
			protected IOCModule getReasoningModule() {
				return new ReasonerModule("hermit");
			}
		};
	}

}
