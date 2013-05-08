package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.impl.SvnAwareXMLReloadingOntologyModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.review.OboTermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppBattoContextListener extends AbstractTermGenieContextListener {

	private static final Logger logger = Logger.getLogger(TermGenieWebAppBattoContextListener.class);
	
	public TermGenieWebAppBattoContextListener() {
		super("TermGenieWebAppBattoConfigFile");
	}
	
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-batto", applicationProperties);
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
				return "TermGenieBatto-TermGenieServiceModule";
			}
		};
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_batto.xml";
		String repositoryURL = "svn+ssh://ext.geneontology.org/share/go/svn/trunk/ontology";
		String workFolder = null; // no default value
		String svnUserName = null; // no default value
		boolean loadExternal = true;
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		
		// http://www.geneontology.org/ontology/editors/gene_ontology_write.obo
		// editors/gene_ontology_write.obo
		mappedIRIs.put(IRI.create("http://www.geneontology.org/ontology/editors/gene_ontology_write.obo"), "editors/gene_ontology_write.obo");
			
		// http://www.geneontology.org/ontology/editors/gene_ontology_xp_write.obo
		// editors/gene_ontology_xp_write.obo
		mappedIRIs.put(IRI.create("http://www.geneontology.org/ontology/editors/gene_ontology_xp_write.obo"), "editors/gene_ontology_xp_write.obo");
					
		String catalogXML = "extensions/catalog-v001.xml";
		
		List<String> ignoreIRIs = Arrays.asList("http://purl.obolibrary.org/obo/go.owl",
				"http://purl.obolibrary.org/obo/go/extensions/x-chemical.owl",
				"http://purl.obolibrary.org/obo/TEMP");
		
		return SvnAwareXMLReloadingOntologyModule.createUsernamePasswordSvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUserName, loadExternal, ignoreIRIs);
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_battoo.xml", false, applicationProperties);
	}
	
	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(true, applicationProperties) {

			@Override
			public String getModuleName() {
				return "TermGenieBatto-TermCommitReviewServiceModule";
			}
			
			@Override
			protected void bindEnabled() {
				bind(TermCommitReviewService.class, OboTermCommitReviewServiceImpl.class);
			}
		};
	}
	

	@Override
	protected Collection<IOCModule> getAdditionalModules() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		try {
			// basic persistence
			String dbFolderString = IOCModule.getSystemProperty("TermGenieWebappBattoDatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-batto-db");
			}
			logger.info("Using db folder: "+dbFolder);
			FileUtils.forceMkdir(dbFolder);
			modules.add(new PersistenceBasicModule(dbFolder, applicationProperties));
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		// commit history and ontology id store
		modules.add(new AdvancedPersistenceModule("BATTO-ID-Manager-Primary", 
				"ids/batto-id-manager-primary.conf",
				"BATTO-ID-Manager-Secondary", 
				"ids/batto-id-manager-secondary.conf", 
				applicationProperties));
		return modules;
	}
}
