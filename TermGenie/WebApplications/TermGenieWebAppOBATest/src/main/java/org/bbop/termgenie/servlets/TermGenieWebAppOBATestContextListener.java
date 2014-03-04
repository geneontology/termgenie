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
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.bbop.termgenie.ontology.impl.SvnAwareOntologyModule;
import org.bbop.termgenie.ontology.svn.CommitSvnAnonymousModule;
import org.bbop.termgenie.presistence.PersistenceBasicModule;
import org.semanticweb.owlapi.model.IRI;


public class TermGenieWebAppOBATestContextListener extends TermGenieWebAppOBAContextListener {

	private final String localSVNFolder;
	private final Map<IRI, String> mappedIRIs;
	private final String catalogXML;
	private final boolean loadExternal;
	
	private final boolean cleanDownloadCache = false;
	
	
	public TermGenieWebAppOBATestContextListener() {
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
			String configFile = "ontology-configuration_oba.xml";
			File localSVNCache = new File("./work/read-only-svn-checkout").getCanonicalFile();
			localSVNCache.mkdirs();
			if (cleanDownloadCache) {
				FileUtils.cleanDirectory(localSVNCache);
			}
			
			File fileCache = new File("./work/termgenie-download-cache").getCanonicalFile();
			
			final Set<IRI> ignoreIRIs = new HashSet<IRI>();
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/oba.owl"));
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/bio-attributes.owl")); 
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.owl"));
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/go/extensions/x-attribute.obo.owl"));
			ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
			
			SvnAwareOntologyModule m = SvnAwareOntologyModule.createAnonymousSvnModule(configFile , applicationProperties);
			m.setSvnAwareRepositoryURL(localSVNFolder);
			m.setSvnAwareMappedIRIs(mappedIRIs);
			m.setSvnAwareCatalogXML(catalogXML);
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
	protected IOCModule getCommitModule() {
		return CommitSvnAnonymousModule.createOboModule(localSVNFolder, "extensions/bio-attributes.obo", applicationProperties, loadExternal);
	}
	
	@Override
	protected PersistenceBasicModule getBasicPersistenceModule() {
		try {
			// basic persistence
			File dbFolder = new File("./work/termgenie-oba-db").getAbsoluteFile();
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
