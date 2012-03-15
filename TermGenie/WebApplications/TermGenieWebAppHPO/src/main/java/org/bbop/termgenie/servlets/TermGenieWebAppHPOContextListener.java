package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.impl.AnonymousSvnAwareXMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnAnonymousModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.review.OboTermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;

public class TermGenieWebAppHPOContextListener extends AbstractTermGenieContextListener {

	private final String localSVNFolder;
	private final String remoteTargetFile;
	
	public TermGenieWebAppHPOContextListener() {
		super("TermGenieWebAppHPOConfigFile");
		try {
			localSVNFolder = "file://"+new File("work/svn").getCanonicalPath(); // TODO replace with real data
			remoteTargetFile = "hpo.obo";  // TODO replace with real data
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	// configure permission module
	@Override
	protected IOCModule getUserPermissionModule() {
		// needs as command-line flag or config file with the actual permissions file location
		return new UserPermissionsModule("termgenie-hpo", applicationProperties);
	}
	
	// configure ontology config file and loading from scm
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String mappedIRI = "http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology.obo";
			File localSVNCache = new File("work/read-only-svn-checkout");
			localSVNCache.mkdirs();
			FileUtils.cleanDirectory(localSVNCache);
			return new AnonymousSvnAwareXMLReloadingOntologyModule("ontology-configuration_hpo.xml", applicationProperties, localSVNFolder, remoteTargetFile, mappedIRI, localSVNCache.getAbsolutePath());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	// configure rule file name
	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_hpo.xml", applicationProperties);
	}

	
	// configure module to handle commits: store terms for review before commit 
	@Override
	protected TermGenieServiceModule getServiceModule() {
		return new TermGenieServiceModule(applicationProperties) {

			@Override
			protected void bindTermCommitService() {
				bind(TermCommitService.class, DefaultTermCommitServiceImpl.class);
			}
			
			@Override
			public String getModuleName() {
				return "TermGenieHPO-TermGenieServiceModule";
			}
		};
	}
	
	// configure module for access to scm
	@Override
	protected IOCModule getCommitModule() {
		return new CommitSvnAnonymousModule(localSVNFolder, remoteTargetFile, applicationProperties, "HumanPhenotype");
	}

	// configure module to review terms before final commit
	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(true, applicationProperties) {

			@Override
			public String getModuleName() {
				return "TermGenieHPO-TermCommitReviewServiceModule";
			}
			
			@Override
			protected void bindEnabled() {
				bind(TermCommitReviewService.class, OboTermCommitReviewServiceImpl.class);
			}
		};
	}
	
	// Persistence: internal db for terms and id counter
	@Override
	protected Collection<IOCModule> getAdditionalModules() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		try {
			// basic persistence
			File dbFolder = new File("work/termgenie-hpo-db").getCanonicalFile();
			Logger.getLogger(getClass()).info("Setting db folder to: "+dbFolder.getAbsolutePath());
			FileUtils.forceMkdir(dbFolder);
			modules.add(new PersistenceBasicModule(dbFolder, applicationProperties));
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		// commit history and ontology id store
		modules.add(new AdvancedPersistenceModule("HPO-ID-Manager", "hpo-id-manager.conf", applicationProperties));
		return modules;
	}
}
