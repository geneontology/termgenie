package org.bbop.termgenie.tools;

import java.util.List;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyTermSuggestor;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.DefaultOntologyLoader;
import org.bbop.termgenie.rules.HardCodedTermGenerationEngine;
import org.bbop.termgenie.solr.LuceneOnlyClient;

public class ImplementationFactory {

	private final static ImplementationFactory instance = new ImplementationFactory();
	
	private final TermGenerationEngine engine;
	private final OntologyTools ontologyTools;
	private final OntologyTermSuggestor suggestor;
	
	private ImplementationFactory() {
		List<Ontology> ontologies = DefaultOntologyLoader.getOntologies();
		engine = new HardCodedTermGenerationEngine(ontologies);
		ontologyTools = new OntologyTools(engine);
		suggestor = new LuceneOnlyClient(ontologies);
//		suggestor = new SimpleSolrClient();
	}
	
	public static TermGenerationEngine getTermGenerationEngine() {
		return instance.engine;
	}
	
	public static OntologyCommitTool getOntologyCommitTool() {
		return OntologyCommitTool.getInstance();
	}
	
	public static OntologyTools getOntologyTools() {
		return instance.ontologyTools;
	}
	
	public static UserCredentialValidatorTools getUserCredentialValidator() {
		return UserCredentialValidatorTools.getInstance();
	}
	
	public static OntologyTermSuggestor getOntologyTermSuggestor() {
		return instance.suggestor;
	}
}
