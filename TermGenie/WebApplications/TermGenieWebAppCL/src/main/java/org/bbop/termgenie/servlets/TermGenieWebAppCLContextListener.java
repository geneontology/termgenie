package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.mail.SimpleMailHandler;
import org.bbop.termgenie.mail.review.DefaultReviewMailHandlerModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.impl.SvnAwareXMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnUserPasswdModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.semanticweb.owlapi.model.IRI;

public class TermGenieWebAppCLContextListener extends AbstractTermGenieContextListener {
	
	private final static Logger logger = Logger.getLogger(TermGenieWebAppCLContextListener.class);

	public TermGenieWebAppCLContextListener() {
		super("TermGenieWebAppCLConfigFile");
	}
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-cl", applicationProperties);
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
				return "TermGenieCL-TermGenieServiceModule";
			}
		};
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_cl.xml";
		String repositoryURL = "https://cell-ontology.googlecode.com/svn/trunk/src/ontology";
		String workFolder = null; // no default value
		String svnUserName = null; // no default value
		boolean loadExternal = false;
		String catalogXML = "catalog-v001.xml";
		
		// no special handling of certain IRIs
		Map<IRI, String> mappedIRIs = Collections.emptyMap(); 
		List<String> ignoreIRIs = Arrays.asList();
		
		return SvnAwareXMLReloadingOntologyModule.createUsernamePasswordSvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUserName, loadExternal, ignoreIRIs);
	}

	@Override
	protected IOCModule getRulesModule() {
		boolean assertInferences = false;
		boolean useIsInferred = false;
		return new XMLDynamicRulesModule("termgenie_rules_cl.xml", useIsInferred, assertInferences, applicationProperties);
	}

	@Override
	protected IOCModule getCommitModule() {
        
		String repositoryURL = "https://cell-ontology.googlecode.com/svn/trunk/src/ontology";
		String remoteTargetFile = "cl-edit.owl";
		String svnUserName = null; // no default value
		boolean loadExternal = false;
		
		return CommitSvnUserPasswdModule.createOwlModule(repositoryURL, remoteTargetFile, svnUserName, applicationProperties, loadExternal);
	}
	
	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(true, applicationProperties) {

			@Override
			public String getModuleName() {
				return "TermGenieCL-TermCommitReviewServiceModule";
			}
			
			@Override
			protected void bindEnabled() {
				// TODO bind and OWL specific review service!!
				bind(TermCommitReviewService.class, TermCommitReviewServiceImpl.class);
			}
		};
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
		return new AdvancedPersistenceModule("CL-ID-Manager-Primary", 
				"ids/cl-id-manager-primary.conf",
				"CL-ID-Manager-Secondary", 
				"ids/cl-id-manager-secondary.conf", 
				applicationProperties);
	}

	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			String dbFolderString = IOCModule.getProperty("TermGenieWebappCLDatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-cl-db");
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
		
		return new DefaultReviewMailHandlerModule(applicationProperties, "help@go.termgenie.org", "CL TermGenie") {
			
			@Override
			protected MailHandler provideMailHandler() {
				return new SimpleMailHandler("smtp.lbl.gov");
			}
		};
	}
	
//	@Override
//	protected IOCModule getFreeFormTermModule() {
//		List<String> oboNamespaces = null;
//		String defaultOntology = "default_cl";
//		boolean addSubsetTag = false;
//		String subset = null;
//		return new FreeFormTermServiceModule(applicationProperties, addSubsetTag , defaultOntology, oboNamespaces, subset);
//	}
}
