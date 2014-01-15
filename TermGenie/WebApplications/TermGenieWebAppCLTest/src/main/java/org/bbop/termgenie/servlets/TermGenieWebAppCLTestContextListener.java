package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.review.NoopReviewMailHandler;
import org.bbop.termgenie.ontology.impl.SvnAwareXMLReloadingOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnAnonymousModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppCLTestContextListener extends TermGenieWebAppCLContextListener {

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
			
			String fileCache = new File("./work/termgenie-download-cache").getCanonicalPath();

			// no special handling
			List<String> ignoreIRIs = Collections.emptyList();
			Map<IRI, String> mappedIRIs = Collections.emptyMap();
			
			return SvnAwareXMLReloadingOntologyModule.createAnonymousSvnModule(configFile , applicationProperties, localSVNFolder, mappedIRIs, catalogXML, localSVNCache.getAbsolutePath(), fileCache, loadExternal, ignoreIRIs);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		return CommitSvnAnonymousModule.createOwlModule(localSVNFolder, "cl-edit.owl", applicationProperties, loadExternal);
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
