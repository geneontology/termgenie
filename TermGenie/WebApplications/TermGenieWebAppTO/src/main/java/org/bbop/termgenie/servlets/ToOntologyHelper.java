package org.bbop.termgenie.servlets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.ontology.git.CommitGitTokenModule;
import org.bbop.termgenie.ontology.impl.GitAwareOntologyModule;
import org.bbop.termgenie.ontology.impl.FileCachingIgnoreFilter.IgnoresContainsDigits;
import org.semanticweb.owlapi.model.IRI;

public class ToOntologyHelper {

	public static IOCModule createDefaultOntologyModule(String workFolder, Properties applicationProperties) {
		String configFile = "ontology-configuration_to.xml";
		String repositoryURL = "https://github.com/Planteome/plant-trait-ontology.git";
		
		Map<IRI, String> mappedIRIs = new HashMap<IRI, String>();
		mappedIRIs.put(IRI.create("http://purl.obolibrary.org/obo/to.owl"), "plant-trait-ontology.obo");
			
		String catalogXML = "catalog-v001.xml";
		
		final Set<IRI> ignoreIRIs = new HashSet<IRI>();
		ignoreIRIs.add(IRI.create("http://purl.obolibrary.org/obo/TEMP"));
		
		GitAwareOntologyModule m = GitAwareOntologyModule.createAnonymousGitModule(configFile, applicationProperties);
		m.setGitAwareRepositoryURL(repositoryURL);
		m.setGitAwareMappedIRIs(mappedIRIs);
		m.setGitAwareCatalogXML(catalogXML);
		m.setGitAwareWorkFolder(workFolder);
		m.setFileCacheFilter(new IgnoresContainsDigits(ignoreIRIs));
		return m;
	}
	
	static IOCModule getCommitModule(Properties applicationProperties) {
		String repositoryURL = "https://github.com/Planteome/plant-trait-ontology.git";
		String remoteTargetFile = "plant-trait-ontology.obo";
		return CommitGitTokenModule.createOboModule(repositoryURL, remoteTargetFile, applicationProperties);
	}
}
