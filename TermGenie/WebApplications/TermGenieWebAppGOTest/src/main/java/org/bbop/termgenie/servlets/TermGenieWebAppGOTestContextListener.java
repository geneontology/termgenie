package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.AdvancedPersistenceModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.impl.SvnAwareOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnAnonymousModule;
import org.bbop.termgenie.permissions.UserPermissionsModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.rules.XMLDynamicRulesModule;
import org.bbop.termgenie.services.DefaultTermCommitServiceImpl;
import org.bbop.termgenie.services.TermCommitService;
import org.bbop.termgenie.services.TermGenieServiceModule;
import org.bbop.termgenie.services.freeform.FreeFormTermServiceModule;
import org.bbop.termgenie.services.review.OboTermCommitReviewServiceImpl;
import org.bbop.termgenie.services.review.TermCommitReviewServiceModule;
import org.bbop.termgenie.startup.JettyTestStartup;
import org.bbop.termgenie.user.go.GeneOntologyJsonUserDataModule;
import org.semanticweb.owlapi.model.IRI;

public class TermGenieWebAppGOTestContextListener extends AbstractTermGenieContextListener {

	/**
	 * This main will use an embedded jetty to start this TG instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8080;
		String contextPath = "/termgenie-go";
		String webappPath = "work/ant-webapp";
		JettyTestStartup.startup(port, contextPath, webappPath);
	}
	
	private final String localSVNFolder;
	private final Map<IRI, String> mappedIRIs;
	private final String ontologyFilePath;
	private final String catalogXML;
	private final boolean loadExternal;

	public TermGenieWebAppGOTestContextListener() {
		super("TermGenieWebAppGOTestConfigFile");
		try {
			localSVNFolder = "file://"+new File("./work/svn").getCanonicalPath();
			ontologyFilePath = "editors/gene_ontology_write.obo";
			mappedIRIs = new HashMap<IRI, String>();
			
			// http://purl.obolibrary.org/obo/go.obo
			// editors/gene_ontology_write.obo
			mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/go.obo"), ontologyFilePath);
			
			catalogXML = "editors/catalog-v001.xml";
			loadExternal = true;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
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
				return "TermGenieGOMini-TermGenieServiceModule";
			}
		};
	}

	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_go.xml";
			File localSVNCache = new File("./work/read-only-svn-checkout").getCanonicalFile();
			localSVNCache.mkdirs();
			FileUtils.cleanDirectory(localSVNCache);
			
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();
			final Set<IRI> ignoreIRIs = new HashSet<IRI>();
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go.owl"));
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-chemical.owl"));
			
			SvnAwareOntologyModule m = SvnAwareOntologyModule.createAnonymousSvnModule(configFile, applicationProperties);
			m.setSvnAwareCatalogXML(catalogXML);
			m.setSvnAwareRepositoryURL(localSVNFolder);
			m.setSvnAwareMappedIRIs(mappedIRIs);
			m.setSvnAwareWorkFolder(localSVNCache.getAbsolutePath());
			m.setSvnAwareLoadExternal(loadExternal);
			m.setFileCache(fileCache);
			m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
			return m;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	protected IOCModule getRulesModule() {
		return new XMLDynamicRulesModule("termgenie_rules_go.xml", true, true, true, applicationProperties);
	}

	@Override
	protected IOCModule getCommitModule() {
		return CommitSvnAnonymousModule.createOboModule(localSVNFolder, ontologyFilePath, applicationProperties, loadExternal);
	}

	@Override
	protected TermCommitReviewServiceModule getCommitReviewWebModule() {
		TermCommitReviewServiceModule module = new TermCommitReviewServiceModule(true, OboTermCommitReviewServiceImpl.class, applicationProperties);
		module.setDoAsciiCheck(true);
		return module;
	}

	@Override
	protected Collection<IOCModule> getAdditionalModules() {
		List<IOCModule> modules = new ArrayList<IOCModule>();
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-go-db").getAbsoluteFile();
			Logger.getLogger(getClass()).info("Setting db folder to: "+dbFolder.getAbsolutePath());
			FileUtils.forceMkdir(dbFolder);
			modules.add(new PersistenceBasicModule(dbFolder, applicationProperties));
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		// commit history and ontology id store
		modules.add(new AdvancedPersistenceModule("GO-ID-Manager-Primary", 
				"go-id-manager-primary.conf",
				"GO-ID-Manager-Secondary", 
				"go-id-manager-secondary.conf", 
				applicationProperties));
		return modules;
	}

	@Override
	protected IOCModule getUserDataModule() {
		String gocjson = "GO.user_data.json";
		List<String> additionalXrefResources = Collections.singletonList("GO.curator_dbxrefs");
		return new GeneOntologyJsonUserDataModule(applicationProperties, gocjson, additionalXrefResources);
	}
	
	@Override
	protected IOCModule getFreeFormTermModule() {
		List<String> oboNamespaces = new ArrayList<String>();
		oboNamespaces.add("biological_process");
		oboNamespaces.add("molecular_function");
		oboNamespaces.add("cellular_component");
		String defaultOntology = "default_go";
		List<String> additionalRelations = Arrays.asList("part_of","has_part", "capable_of");
		FreeFormTermServiceModule module = new FreeFormTermServiceModule(applicationProperties, true, defaultOntology, oboNamespaces, "termgenie_unvetted", additionalRelations);
		module.setDoAsciiCheck(true);
		return module;
	}
	
}
