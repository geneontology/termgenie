package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.ontology.impl.SvnAwareOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnAnonymousModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.bbop.termgenie.startup.JettyTestStartup;


public class TermGenieWebAppCLTestContextListener extends TermGenieWebAppCLContextListener {

	/**
	 * This main will use an embedded jetty to start this TG instance.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8080;
		String contextPath = "/termgenie-cl";
		String webappPath = "work/ant-webapp";
		JettyTestStartup.startup(port, contextPath, webappPath);
	}
	
	private final String localSVNFolder;
	private final String catalogXML = "catalog-v001.xml";
	private final boolean loadExternal = false;
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppCLTestContextListener() {
		try {
			localSVNFolder = "file://"+new File("./work/svn").getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_cl.xml";
			File localSVNCache = new File("./work/read-only-svn-checkout").getCanonicalFile();
			localSVNCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localSVNCache);
			}
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();

			SvnAwareOntologyModule m = SvnAwareOntologyModule.createAnonymousSvnModule(configFile , applicationProperties);
			m.setSvnAwareRepositoryURL(localSVNFolder);
			m.setSvnAwareWorkFolder(localSVNCache.getAbsolutePath());
			m.setSvnAwareCatalogXML(catalogXML);
			m.setSvnAwareLoadExternal(loadExternal);
			m.setFileCache(fileCache);
			return m;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		return CommitSvnAnonymousModule.createOwlModule(localSVNFolder, "cl-edit.owl", catalogXML, applicationProperties, loadExternal);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-cl-db").getCanonicalFile();
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
