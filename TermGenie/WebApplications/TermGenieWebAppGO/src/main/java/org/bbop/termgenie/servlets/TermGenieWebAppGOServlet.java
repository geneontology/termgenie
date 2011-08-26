package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.go.GeneOntologyCommitModule;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.DefaultXMLDynamicRulesModule;

public class TermGenieWebAppGOServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = 7176312523891484544L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new ServiceExecutor() {

			@Override
			protected IOCModule getOntologyModule() {
				return new XMLReloadingOntologyModule("ontology-configuration_go.xml");
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultXMLDynamicRulesModule("termgenie_rules_go.xml");
			}

			@Override
			protected IOCModule getCommitModule() {
				return new GeneOntologyCommitModule();
			}

			@Override
			protected Collection<IOCModule> getAdditionalModules() {
				try {
					File dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-db");
					FileUtils.forceMkdir(dbFolder);
					List<IOCModule> modules = Collections.<IOCModule>singletonList(new PersistenceBasicModule(dbFolder));
					return modules;
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		};
	}

}
