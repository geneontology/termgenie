package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.go.GeneOntologyCommitModule;
import org.bbop.termgenie.ontology.impl.CommitAwareOntologyLoader;
import org.bbop.termgenie.ontology.impl.XMLReloadingOntologyModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.DefaultXMLDynamicRulesModule;
import org.bbop.termgenie.services.GoTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;

public class TermGenieWebAppGOServlet extends AbstractJsonRPCServlet {

	// generated
	private static final long serialVersionUID = 7176312523891484544L;

	@Override
	protected ServiceExecutor createServiceExecutor() {
		return new ServiceExecutor() {

			@Override
			protected TermGenieServiceModule getServiceModule() {
				return new TermGenieServiceModule() {

					@Override
					protected void bindTermCommitService() {
						bind(TermCommitService.class).to(GoTermCommitServiceImpl.class);
					}
				};
			}

			@Override
			protected IOCModule getOntologyModule() {
				return new XMLReloadingOntologyModule("ontology-configuration_go.xml") {
					
					@Override
					protected void bindOntologyLoader() {
						bind(OntologyLoader.class).to(CommitAwareOntologyLoader.class);
						bind("ReloadingOntologyLoaderPeriod", new Long(6L));
						bind("ReloadingOntologyLoaderTimeUnit", TimeUnit.HOURS);
					}
				};
			}

			@Override
			protected IOCModule getRulesModule() {
				return new DefaultXMLDynamicRulesModule("termgenie_rules_go.xml");
			}

			@Override
			protected IOCModule getCommitModule() {
				String cvsFileName = ""; // TODO fill empty stub
				String cvsRoot = ""; // TODO fill empty stub
				return new GeneOntologyCommitModule(cvsFileName, cvsRoot);
			}

			@Override
			protected Collection<IOCModule> getAdditionalModules() {
				List<IOCModule> modules = new ArrayList<IOCModule>();
				try {
					// basic persistence
					File dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-go-db");
					FileUtils.forceMkdir(dbFolder);
					modules.add(new PersistenceBasicModule(dbFolder));
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
				// commit history and ontology id store
				modules.add(new AdvancedPersistenceModule("GO-ID-Manager","go-id-manager.conf"));
				return modules;
			}
		};
	}

}
