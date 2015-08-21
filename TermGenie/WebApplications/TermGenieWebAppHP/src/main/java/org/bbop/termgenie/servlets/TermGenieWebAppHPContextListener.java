package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.mail.SimpleMailHandler;
import org.bbop.termgenie.mail.review.DefaultReviewMailHandlerModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.git.CommitGitTokenModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.impl.GitAwareOntologyModule;
import org.bbop.termgenie.permissions.UserPermissionsModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.freeform.FreeFormTermServiceModule;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.semanticweb.owlapi.model.IRI;

public class TermGenieWebAppHPContextListener extends AbstractTermGenieContextListener {
	
	private final static Logger logger = Logger.getLogger(TermGenieWebAppHPContextListener.class);

	public TermGenieWebAppHPContextListener() {
		super("TermGenieWebAppHPConfigFile");
	}
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-hp", applicationProperties);
	}
	
	@Override
	protected TermGenieServiceModule getServiceModule() {
		return new TermGenieServiceModule(applicationProperties) {

			@Override
			protected void bindTermCommitService() {
				bind(TermCommitService.class, DefaultTermCommitServiceImpl.class);
			}
			
			@Override
			public String getModuleName() {
				return "TermGenieHP-TermGenieServiceModule";
			}
		};
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_hp.xml";
		String repositoryURL = "https://github.com/obophenotype/human-phenotype-ontology.git";
		String catalogXML = null;
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		
		// http://purl.obolibrary.org/obo/hp.owl ->  hp/hp-edit.owl
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/hp.owl"), "src/ontology/hp-edit.owl");
		
		final Set<IRI> ignoreIRIs = new HashSet<IRI>();
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
		
		GitAwareOntologyModule m = GitAwareOntologyModule.createAnonymousGitModule(configFile, applicationProperties);
		m.setGitAwareRepositoryURL(repositoryURL);
		m.setGitAwareCatalogXML(catalogXML);
		m.setGitAwareMappedIRIs(mappedIRIs);
		m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
		return m;
	}

	@Override
	protected IOCModule getRulesModule() {
		boolean assertInferences = true;
		boolean useIsInferred = true;
		boolean filterNonAsciiSynonyms = true;
		return new XMLDynamicRulesModule("termgenie_rules_hp.xml", useIsInferred, assertInferences, filterNonAsciiSynonyms, applicationProperties);
	}

	@Override
	protected IOCModule getCommitModule() {
        
		String repositoryURL = "https://phenotype-ontologies.googlecode.com/svn/trunk/src/ontology";
		String remoteTargetFile = "src/ontology/hp-edit.owl";
		String catalogXml = null;
		
		return CommitGitTokenModule.createOwlModule(repositoryURL, remoteTargetFile, catalogXml, applicationProperties);
	}
	
	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		TermCommitReviewServiceModule module = new TermCommitReviewServiceModule(true, applicationProperties);
		module.setUseOboDiff(false);
		return module;
	}
	
	@Override
	protected Collection<IOCModule> getAdditionalModules() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		PersistenceBasicModule basicPersistenceModule = getBasicPersistenceModule();
		if (basicPersistenceModule != null) {
			modules.add(basicPersistenceModule);
		}
		// commit history and ontology id store
		AdvancedPersistenceModule advancedPersistenceModule = getAdvancedPersistenceModule();
		if (advancedPersistenceModule != null) {
			modules.add(advancedPersistenceModule);
		}
		return modules;
	}

	protected AdvancedPersistenceModule getAdvancedPersistenceModule() {
		// TODO use http://purl.obolibrary.org/obo/hp/hp-idranges.owl instead
		return new AdvancedPersistenceModule("HP-ID-Manager-Primary", 
				"ids/hp-id-manager-primary.conf",
				"HP-ID-Manager-Secondary", 
				"ids/hp-id-manager-secondary.conf", 
				applicationProperties);
	}

	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			String dbFolderString = IOCModule.getProperty("TermGenieWebappHPDatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-hp-db");
			}
			dbFolder = dbFolder.getCanonicalFile();
			logger.info("Using db folder: "+dbFolder);
			FileUtils.forceMkdir(dbFolder);
			return new PersistenceBasicModule(dbFolder, applicationProperties);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	protected IOCModule getReviewMailHandlerModule() {
		
		return new DefaultReviewMailHandlerModule(applicationProperties, "help@go.termgenie.org", "HP TermGenie") {
			
			@Override
			protected MailHandler provideMailHandler() {
				return new SimpleMailHandler("smtp.lbl.gov");
			}
		};
	}
	
	@Override
	protected IOCModule getFreeFormTermModule() {
		List<String> oboNamespaces = null;
		String defaultOntology = "default_hp";
		boolean addSubsetTag = false;
		String subset = null;
		List<String> additionalRelations = null;
		FreeFormTermServiceModule module = new FreeFormTermServiceModule(applicationProperties, addSubsetTag , defaultOntology, oboNamespaces, subset, additionalRelations );
		module.setDoAsciiCheck(false);
		return module;
	}
}
