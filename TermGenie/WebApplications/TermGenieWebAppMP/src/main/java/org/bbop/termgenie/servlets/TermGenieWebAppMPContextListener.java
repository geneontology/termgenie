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
import org.bbop.termgenie.ontology.impl.SvnAwareOntologyModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.svn.CommitSvnUserPasswdModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.semanticweb.owlapi.model.IRI;

public class TermGenieWebAppMPContextListener extends AbstractTermGenieContextListener {
	
	private final static Logger logger = Logger.getLogger(TermGenieWebAppMPContextListener.class);

	public TermGenieWebAppMPContextListener() {
		super("TermGenieWebAppMPConfigFile");
	}
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-mp", applicationProperties);
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
				return "TermGenieMP-TermGenieServiceModule";
			}
		};
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_mp.xml";
		String repositoryURL = "https://phenotype-ontologies.googlecode.com/svn/trunk/src/ontology";
		String svnUserName = null; // no default value
		boolean loadExternal = false;
		String catalogXML = "mp/catalog-v001.xml";
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		
		// http://purl.obolibrary.org/obo/mp.owl ->  mp/mp-edit.owl
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/mp.owl"), "mp/mp-edit.owl");
		
		final Set<IRI> ignoreIRIs = new HashSet<IRI>();
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
		
		SvnAwareOntologyModule m = SvnAwareOntologyModule.createUsernamePasswordSvnModule(configFile, applicationProperties, svnUserName);
		m.setSvnAwareRepositoryURL(repositoryURL);
		m.setSvnAwareLoadExternal(loadExternal);
		m.setSvnAwareCatalogXML(catalogXML);
		m.setSvnAwareMappedIRIs(mappedIRIs);
		m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
		return m;
	}

	@Override
	protected IOCModule getRulesModule() {
		boolean assertInferences = false;
		boolean useIsInferred = false;
		return new XMLDynamicRulesModule("termgenie_rules_mp.xml", useIsInferred, assertInferences, applicationProperties);
	}

	@Override
	protected IOCModule getCommitModule() {
        
		String repositoryURL = "https://phenotype-ontologies.googlecode.com/svn/trunk/src/ontology";
		String remoteTargetFile = "mp/mp-edit.owl";
		String catalogXml = "mp/catalog-v001.xml";
		String svnUserName = null; // no default value
		boolean loadExternal = false;
		
		return CommitSvnUserPasswdModule.createOwlModule(repositoryURL, remoteTargetFile, catalogXml, svnUserName, applicationProperties, loadExternal);
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
		// TODO use http://purl.obolibrary.org/obo/mp/mp-idranges.owl instead
		return new AdvancedPersistenceModule("MP-ID-Manager-Primary", 
				"ids/mp-id-manager-primary.conf",
				"MP-ID-Manager-Secondary", 
				"ids/mp-id-manager-secondary.conf", 
				applicationProperties);
	}

	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			String dbFolderString = IOCModule.getProperty("TermGenieWebappMPDatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-mp-db");
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
		
		return new DefaultReviewMailHandlerModule(applicationProperties, "help@go.termgenie.org", "MP TermGenie") {
			
			@Override
			protected MailHandler provideMailHandler() {
				return new SimpleMailHandler("smtp.lbl.gov");
			}
		};
	}
	
//	@Override
//	protected IOCModule getFreeFormTermModule() {
//		List<String> oboNamespaces = null;
//		String defaultOntology = "default_mp";
//		boolean addSubsetTag = false;
//		String subset = null;
//		return new FreeFormTermServiceModule(applicationProperties, addSubsetTag , defaultOntology, oboNamespaces, subset);
//	}
}
