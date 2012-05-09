package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.mail.SimpleMailHandler;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.impl.SvnAwareXMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.obo.OboPatternSpecificTermFilter;
import org.bbop.termgenie.ontology.svn.CommitSvnUserPasswdModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.permissions.UserPermissionsModule;
import org.bbop.termgenie.services.resources.ResourceProviderModule;
import org.bbop.termgenie.services.resources.ResourceProviderModule.ConfiguredResourceProviderModule;
import org.bbop.termgenie.services.review.OboTermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewService;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.bbop.termgenie.services.review.mail.DefaultReviewMailHandlerModule;
import org.bbop.termgenie.user.go.GeneOntologyUserDataModule;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.IRI;

public class TermGenieWebAppGOContextListener extends AbstractTermGenieContextListener {

	private static final Logger logger = Logger.getLogger(TermGenieWebAppGOContextListener.class);
	
	public TermGenieWebAppGOContextListener() {
		super("TermGenieWebAppGOConfigFile");
	}
	
	@Override
	protected IOCModule getUserPermissionModule() {
		return new UserPermissionsModule("termgenie-go", applicationProperties);
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
				return "TermGenieGO-TermGenieServiceModule";
			}
		};
	}

	@Override
	protected IOCModule getOntologyModule() {
		String configFile = "ontology-configuration_go.xml";
		String repositoryURL = "svn+ssh://ext.geneontology.org/share/go/svn/trunk/ontology";
		String workFolder = null; // no default value
		String svnUserName = null; // no default value
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		
		// http://www.geneontology.org/ontology/editors/gene_ontology_write.obo
		// editors/gene_ontology_write.obo
		mappedIRIs.put(IRI.create("http://www.geneontology.org/ontology/editors/gene_ontology_write.obo"), "editors/gene_ontology_write.obo");
			
		// http://www.geneontology.org/ontology/editors/go_xp_chebi.obo
		// editors/go_xp_chebi.obo
		mappedIRIs.put(IRI.create("http://www.geneontology.org/ontology/editors/go_xp_chebi.obo"), "editors/go_xp_chebi.obo");
					
		String catalogXML = "extension/catalog-v001.xml";
		
		
		return SvnAwareXMLReloadingOntologyModule.createUsernamePasswordSvnModule(configFile, applicationProperties, repositoryURL, mappedIRIs, catalogXML, workFolder, svnUserName);
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_go.xml", applicationProperties);
	}

	@Override
	protected IOCModule getCommitModule() {
		final Map<String, Integer> specialPatterns = Collections.singletonMap("metabolism_catabolism_biosynthesis", 1);
		
		String repositoryURL = "svn+ssh://ext.geneontology.org/share/go/svn/trunk/ontology";
		String remoteTargetFile = "editors/gene_ontology_write.obo";
		String svnUserName = null; // no default value
		List<String> additional = Collections.singletonList("editors/go_xp_chebi.obo");
		return new CommitSvnUserPasswdModule(repositoryURL, remoteTargetFile, svnUserName, applicationProperties, "GeneOntology", additional ){

			@Override
			protected TermFilter<OBODoc> provideTermFilter() {
				return new OboPatternSpecificTermFilter(specialPatterns);
			}
			
		};
	}
	
	

	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		return new TermCommitReviewServiceModule(true, applicationProperties) {

			@Override
			public String getModuleName() {
				return "TermGenieGO-TermCommitReviewServiceModule";
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
			String dbFolderString = IOCModule.getSystemProperty("TermGenieWebappGODatabaseFolder", applicationProperties);
			File dbFolder;
			if (dbFolderString != null && !dbFolderString.isEmpty()) {
				dbFolder = new File(dbFolderString);
			}
			else {
				dbFolder = new File(FileUtils.getUserDirectory(), "termgenie-go-db");
			}
			logger.info("Using db folder: "+dbFolder);
			FileUtils.forceMkdir(dbFolder);
			modules.add(new PersistenceBasicModule(dbFolder, applicationProperties));
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		// commit history and ontology id store
		modules.add(new AdvancedPersistenceModule("GO-ID-Manager", "go-id-manager.conf", applicationProperties));
		return modules;
	}

	@Override
	protected IOCModule getUserDataModule() {
		String gocConfigResource = "GO.curators_dbxrefs";
		String gocMappingResource = "GO.curators_email_dbxrefs";
		return new GeneOntologyUserDataModule(applicationProperties, gocConfigResource, '\t', gocMappingResource, '\t');
	}
	
	@Override
	protected ResourceProviderModule getResourceProviderModule() {
		return new ConfiguredResourceProviderModule(applicationProperties);
	}

	@Override
	protected IOCModule getReviewMailHandlerModule() {
		
		return new DefaultReviewMailHandlerModule(applicationProperties, "help@go.termgenie.org", "GeneOntology TermGenie") {
			
			@Override
			protected MailHandler provideMailHandler() {
				return new SimpleMailHandler("smtp.lbl.gov");
			}
		};
	}
	
}
