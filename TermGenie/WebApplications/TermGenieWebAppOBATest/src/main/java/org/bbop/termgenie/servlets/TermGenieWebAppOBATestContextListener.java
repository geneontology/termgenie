package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.ontology.git.CommitGitAnonymousModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.impl.GitAwareOntologyModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.startup.JettyTestStartup;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppOBATestContextListener extends TermGenieWebAppOBAContextListener {

	/**
	 * This main will use an embedded jetty to start this TG instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8080;
		String contextPath = "/termgenie-oba";
		String webappPath = "work/ant-webapp";
		JettyTestStartup.startup(port, contextPath, webappPath);
	}
	
	private final String localGitFolder;
	private final Map<IRI, String> mappedIRIs;
	private final String catalogXML;
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppOBATestContextListener() {
		try {
			localGitFolder = new File("./work/git").getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		mappedIRIs = new HashMap<IRI, String>();
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/oba.owl"), "src/ontology/oba-edit.obo");
		
		catalogXML = "src/ontology/catalog-v001.xml";
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_oba.xml";
			File localGitCache = new File("./work/read-only-git-checkout").getCanonicalFile();
			localGitCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localGitCache);
			}
			
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();
			
			final Set<IRI> ignoreIRIs = new HashSet<IRI>();
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
			
			GitAwareOntologyModule m = GitAwareOntologyModule.createAnonymousGitModule(configFile, applicationProperties);
			m.setGitAwareRepositoryURL(localGitFolder);
			m.setGitAwareMappedIRIs(mappedIRIs);
			m.setGitAwareCatalogXML(catalogXML);
			m.setGitAwareWorkFolder(localGitCache.getAbsolutePath());
			m.setFileCache(fileCache);
			m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
			return m;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		return CommitGitAnonymousModule.createOboModule(localGitFolder, "src/ontology/oba-edit.obo", applicationProperties);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-oba-db").getCanonicalFile();
			Logger.getLogger(getClass()).info("Setting db folder to: "+dbFolder.getAbsolutePath());
			FileUtils.forceMkdir(dbFolder);
			return new PersistenceBasicModule(dbFolder, applicationProperties);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	protected IOCModule getReviewMailHandlerModule() {
		// deactivate e-mail
		return new NoopReviewMailHandler.NoopReviewMailHandlerModule(applicationProperties);
	}
	
}
