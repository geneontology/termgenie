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


public class TermGenieWebAppHPTestContextListener extends TermGenieWebAppHPContextListener {
	
	/**
	 * This main will use an embedded jetty to start this TG instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8080;
		String contextPath = "/termgenie-hp";
		String webappPath = "work/ant-webapp";
		JettyTestStartup.startup(port, contextPath, webappPath);
	}
	
	private final String localGitFolder;
	private final String editFile = "src/ontology/hp-edit.owl";
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppHPTestContextListener() {
		try {
			localGitFolder = new File("./work/git").getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_hp.xml";
			File localGitCache = new File("./work/read-only-git-checkout").getCanonicalFile();
			localGitCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localGitCache);
			}
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();

			GitAwareOntologyModule m = GitAwareOntologyModule.createAnonymousGitModule(configFile, applicationProperties);
			m.setGitAwareRepositoryURL(localGitFolder);
			m.setGitAwareWorkFolder(localGitCache.getAbsolutePath());
			m.setFileCache(fileCache);
			m.setGitAwareMappedIRIs(Collections.singletonMap(IRI.create("http://purl.obolibrary.org/obo/hp.owl"), editFile));
			return m;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		return CommitGitAnonymousModule.createOwlModule(localGitFolder, editFile, null, applicationProperties);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-hp-db").getCanonicalFile();
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
