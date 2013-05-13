package org.bbop.termgenie.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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


public class TermGenieWebAppBattoTestContextListener extends TermGenieWebAppBattoContextListener {

	private final String localSVNFolder;
	private final Map<IRI, String> mappedIRIs;
	private final String catalogXML;
	private final boolean loadExternal;
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppBattoTestContextListener() {
		try {
			localSVNFolder = "file://"+new File("./work/svn").getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		mappedIRIs = new HashMap<IRI, String>();
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/go/extensions/bio-attributes.obo"), "extensions/bio-attributes.obo");
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo"), "extensions/x-attribute.obo");
		
		catalogXML = "extensions/catalog-v001.xml";
		loadExternal = true;
	}
	
	@Override
	protected IOCModule getOntologyModule() {
		try {
			String configFile = "ontology-configuration_batto.xml";
			File localSVNCache = new File("./work/read-only-svn-checkout").getCanonicalFile();
			localSVNCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localSVNCache);
			}
			
			String fileCache = new File("./work/termgenie-download-cache").getAbsolutePath();
			
			List<String> ignoreIRIs = Arrays.asList(
					"http://purl.obolibrary.org/obo/go/extensions/bio-attributes.owl", 
					"http://purl.obolibrary.org/obo/go/extensions/x-attribute.owl",
					"http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo.owl",
					"http://purl.obolibrary.org/obo/TEMP");
			return SvnAwareXMLReloadingOntologyModule.createAnonymousSvnModule(configFile , applicationProperties, localSVNFolder, mappedIRIs, catalogXML, localSVNCache.getAbsolutePath(), fileCache, loadExternal, ignoreIRIs);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	
	@Override
	protected IOCModule getCommitModule() {
		return new CommitSvnAnonymousModule(localSVNFolder, "extensions/bio-attributes.obo", applicationProperties, "Batto", Collections.<String>emptyList(), loadExternal);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-batto-db").getAbsoluteFile();
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
