package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerProcessor;

import owltools.graph.OWLGraphWrapper;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;

public final class ReasonerFactory {
	
	private static final Logger logger = Logger.getLogger(ReasonerFactory.class);
	
	private static final String PELLET = "pellet";
	private static final String HERMIT = "hermit";
	private static final String JCEL = "jcel";
	private static final String FACT = "Fact++";
	private final static Set<String> supportedReasoners = new HashSet<String>(Arrays.asList(JCEL,HERMIT,PELLET,FACT));
	
	private final static Map<String, Map<String, ReasonerTaskManager>> allManagers = new HashMap<String, Map<String,ReasonerTaskManager>>();
	
	public static final ReasonerTaskManager getDefaultTaskManager(OWLGraphWrapper ontology) {
//		String ontologyId = ontology.getOntologyId();
//		if ("go".equals(ontologyId) || "po".equals(ontologyId)) {
//			// use jcel for GeneOntology and Plant ontology
			return getTaskManager(ontology, JCEL);
//		}
		// use slow reasoner for other ontologies
		// Because: JCEL does not support:
		/*
		 * This class expression is not supported: 
		 * 'ObjectExactCardinality(1 
		 * <http://purl.obolibrary.org/obo/pr_has_part> 
		 * <http://purl.obolibrary.org/obo/PR_000026037>)'
		 * 
		 * 
		 * This class expression is not supported: 
		 * 'ObjectUnionOf(
		 * <http://purl.obolibrary.org/obo/UBERON_0003898> 
		 * <http://purl.obolibrary.org/obo/UBERON_0003899>)'
		 */
//		return getTaskManager(ontology, FACT);
		/*
		 * FACT++ seems to work, but throws also exceptions for pro and uberon when used:
		 * 
		 * pro:
		 * OWLReasonerRuntimeException: Non-simple object property 'http://purl.obolibrary.org/obo/pr_has_part' is used as a simple one
		 * 
		 * uberon:
		 * OWLReasonerRuntimeException: Non-simple object property 'http://purl.obolibrary.org/obo/BFO_0000050' is used as a simple one
		 */
		/*
		 * PELLET also complains a bit, but does not fail when loading PRO and Uberon:
		 * 
		 * Ignoring unsupported axiom: TransitiveObjectProperty(<http://purl.obolibrary.org/obo/pr_has_part>)
		 * 
		 * Ignoring unsupported axiom: SubObjectPropertyOf(
		 * ObjectPropertyChain( <http://purl.obolibrary.org/obo/BFO_0000068> <http://purl.obolibrary.org/obo/uberon_preceded_by> ) <http://purl.obolibrary.org/obo/uberon_existence_starts_after>)
		 * 
		 * Ignoring unsupported axiom: TransitiveObjectProperty(<http://purl.obolibrary.org/obo/BFO_0000050>)
		 * 
		 * Ignoring unsupported axiom: TransitiveObjectProperty(<http://purl.obolibrary.org/obo/uberon_preceded_by>)
		 * 
		 * Unforuntally it is not possible to use pellet with pro or uberon, it takes too long to put a result. 
		 * (I have not seen any result in my tests.)
		 */
	}

	public static final ReasonerTaskManager getTaskManager(OWLGraphWrapper ontology, String reasonerName) {
	
		if(reasonerName == null || !supportedReasoners.contains(reasonerName)) {
			throw new RuntimeException("Unknown reasoner: "+reasonerName);
		}
		Map<String, ReasonerTaskManager> managers = allManagers.get(reasonerName);
		if (managers == null) {
			managers = new HashMap<String, ReasonerTaskManager>();
			allManagers.put(reasonerName, managers);
		}
		String ontologyName = ontology.getOntologyId();
		ReasonerTaskManager manager = managers.get(ontologyName);
		if (manager == null) {
			manager = createManager(ontology, reasonerName);
			managers.put(ontologyName, manager);
		}
		return manager;
	}
	
	private static ReasonerTaskManager createManager(OWLGraphWrapper ontology, String reasonerName) {
		if (JCEL.equals(reasonerName)) {
			return createJCelManager(ontology.getSourceOntology());
		}
		else if (HERMIT.equals(reasonerName)) {
			return createManager(ontology.getSourceOntology(), new Reasoner.ReasonerFactory());
		}
		else if (PELLET.equals(reasonerName)) {
			return createManager(ontology.getSourceOntology(), PelletReasonerFactory.getInstance());
		}
		else if (FACT.equals(reasonerName)) {
			return createManager(ontology.getSourceOntology(), new FaCTPlusPlusReasonerFactory());
		}
		return null;
	}
	
	private static ReasonerTaskManager createJCelManager(final OWLOntology ontology) {
		return new ReasonerTaskManager() {
			
			@Override
			protected OWLReasoner updateManaged(OWLReasoner managed) {
				// TODO find out if there is an option to re-use the reasoner
				// TODO how to do incremental changes?
				return createReasoner("Updating jcel reasoner: "+ontology.getOntologyID());
			}
			
			@Override
			protected OWLReasoner createManaged() {
				return createReasoner("Creating jcel reasoner: "+ontology.getOntologyID());
			}
			
			private OWLReasoner createReasoner(String logMessage) {
				logger.info(logMessage);
				JcelReasoner reasoner = new JcelReasoner(ontology);
				JcelReasonerProcessor processor = reasoner.getProcessor();
				processor.precomputeInferences();
				return reasoner;
			}
		};
	}

	private static ReasonerTaskManager createManager(final OWLOntology ontology, final OWLReasonerFactory reasonerFactory) {
		return new ReasonerTaskManager() {
			
			@Override
			protected OWLReasoner updateManaged(OWLReasoner managed) {
				// Do nothing, as the reasoner should reflect changes to 
				// the underlying ontology (non-buffering).
				return managed;
			}
			
			@Override
			protected OWLReasoner createManaged() {
				return reasonerFactory.createNonBufferingReasoner(ontology);
			}
		};
	}
}
