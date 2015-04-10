package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.ontology.git.CommitGitAnonymousModule;
import org.bbop.termgenie.ontology.impl.GitAwareOntologyModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.startup.JettyTestStartup;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppEnvoTestContextListener extends TermGenieWebAppEnvoContextListener {
	
	/**
	 * This main will use an embedded jetty to start this TG instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8080;
		String contextPath = "/termgenie-envo";
		String webappPath = "work/ant-webapp";
//		java.util.logging.Logger.getLogger("com.sun.net.ssl").setLevel(java.util.logging.Level.ALL);
		JettyTestStartup.startup(port, contextPath, webappPath);
	}
	
	private final String localGitFolder;
	private final String catalogXML = "src/envo/catalog-v001.xml";
	private final String gitOntologyFileName = "src/envo/envo-edit.owl";
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppEnvoTestContextListener() {
		try {
			localGitFolder = new File("./work/envo").getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_envo.xml";
			File localGitCache = new File("./work/read-only-git-checkout").getCanonicalFile();
			localGitCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localGitCache);
			}
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();

			GitAwareOntologyModule m = GitAwareOntologyModule.createAnonymousGitModule(configFile, applicationProperties);
			m.setGitAwareRepositoryURL(localGitFolder);
			m.setGitAwareWorkFolder(localGitCache.getAbsolutePath());
			m.setGitAwareCatalogXML(catalogXML);
			m.setFileCache(fileCache);
			m.setGitAwareMappedIRIs(Collections.singletonMap(IRI.create("http://purl.obolibrary.org/obo/envo.owl"), gitOntologyFileName));
			return m;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		
		return CommitGitAnonymousModule.createOwlModule(localGitFolder, gitOntologyFileName, catalogXML, applicationProperties);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-envo-db").getCanonicalFile();
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
