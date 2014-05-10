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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.mail.SimpleMailHandler;
import org.bbop.termgenie.mail.review.DefaultReviewMailHandlerModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.impl.SvnAwareOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnUserKeyFileModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.freeform.FreeFormTermServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.review.OboTermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppOBAContextListener extends AbstractTermGenieContextListener {

	private static final Logger logger = Logger.getLogger(TermGenieWebAppOBAContextListener.class);
	
	public TermGenieWebAppOBAContextListener() {
		super("TermGenieWebAppOBAConfigFile");
	}
	
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-oba", applicationProperties);
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
				return "TermGenieOBA-TermGenieServiceModule";
			}
		};
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_oba.xml";
		String repositoryURL = "svn+ssh://ext.geneontology.org/share/go/svn/trunk/ontology";
		String workFolder = null; // no default value
		String svnUserName = null; // no default value
		String keyFile = null;		// no default value
		boolean loadExternal = true;
		boolean usePassphrase = false;
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/go/extensions/bio-attributes.obo"), "extensions/bio-attributes.obo");
		
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo"), "extensions/x-attribute.obo");
			
		String catalogXML = "extensions/catalog-v001.xml";
		
		final Set<IRI> ignoreIRIs = new HashSet<IRI>();
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/oba.owl"));
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/bio-attributes.owl")); 
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.owl"));
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo.owl"));
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
		
		SvnAwareOntologyModule m = SvnAwareOntologyModule.createSshKeySvnModule(configFile, applicationProperties, svnUserName, keyFile, usePassphrase);
		m.setSvnAwareRepositoryURL(repositoryURL);
		m.setSvnAwareMappedIRIs(mappedIRIs);
		m.setSvnAwareCatalogXML(catalogXML);
		m.setSvnAwareWorkFolder(workFolder);
		m.setSvnAwareLoadExternal(loadExternal);
		m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
		return m;
	}
	
	protected Map<IRI, File> getLocalMappings(String prefix) {
		Map<IRI, File> localMappings = new HashMap<IRI, File>();
		String localIRIMappings = IOCModule.getProperty(prefix, applicationProperties);
		if (localIRIMappings != null) {
			String[] localIRIMappingComponents = StringUtils.split(localIRIMappings, ','); // treat as comma separated list
			for (String component : localIRIMappingComponents) {
				String iri = IOCModule.getProperty(prefix+"."+component+".IRI", applicationProperties);
				String file = IOCModule.getProperty(prefix+"."+component+".File", applicationProperties);
				if (iri != null && file != null) {
					localMappings.put(IRI.create(iri), new File(file).getAbsoluteFile());
				}
			}
		}
		return localMappings;
	}

	@Override
	protected IOCModule getCommitModule() {
		
		String repositoryURL = "svn+ssh://ext.geneontology.org/share/go/svn/trunk/ontology";
		String remoteTargetFile = "extensions/bio-attributes.obo";
		String svnUserName = null;	// no default value
		File keyFile = null;		// no default value
		boolean loadExternal = true;
		boolean usePassphrase = false;
		
		return CommitSvnUserKeyFileModule.createOboModule(repositoryURL, remoteTargetFile, svnUserName, keyFile , applicationProperties, loadExternal, usePassphrase);
	}
	
	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_oba.xml", false, true, applicationProperties);
	}
	
	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(true, OboTermCommitReviewServiceImpl.class, applicationProperties);
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
		return new AdvancedPersistenceModule("OBA-ID-Manager-Primary", 
				"ids/oba-id-manager-primary.conf",
				"OBA-ID-Manager-Secondary", 
				"ids/oba-id-manager-secondary.conf", 
				applicationProperties);
	}

	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			String dbFolderString = IOCModule.getProperty("TermGenieWebappOBADatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-oba-db");
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
		
		return new DefaultReviewMailHandlerModule(applicationProperties, "help@go.termgenie.org", "OBA TermGenie") {
			
			@Override
			protected MailHandler provideMailHandler() {
				return new SimpleMailHandler("smtp.lbl.gov");
			}
		};
	}
	
	@Override
	protected IOCModule getFreeFormTermModule() {
		List<String> oboNamespaces = null;
		String defaultOntology = "default_oba";
		boolean addSubsetTag = false;
		String subset = null;
		List<String> additionalRelations = null;
		FreeFormTermServiceModule module = new FreeFormTermServiceModule(applicationProperties, addSubsetTag , defaultOntology, oboNamespaces, subset, additionalRelations );
		module.setDoAsciiCheck(false);
		return module;
	}
}
