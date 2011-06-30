package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerProcessor;

import owltools.graph.OWLGraphWrapper;

public final class ReasonerFactory {
	
	private static final String PELLET = "pellet";
	private static final String HERMIT = "hermit";
	private static final String JCEL = "jcel";
	private final static Set<String> supportedReasoners = new HashSet<String>(Arrays.asList(JCEL,HERMIT,PELLET));
	
	private final static Map<String, Map<String, ReasonerTaskManager>> allManagers = new HashMap<String, Map<String,ReasonerTaskManager>>();
	
	public static final ReasonerTaskManager getDefaultTaskManager(OWLGraphWrapper ontology) {
		return getTaskManager(ontology, JCEL);
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
		return null;
	}
	
	private static ReasonerTaskManager createJCelManager(final OWLOntology ontology) {
		return new ReasonerTaskManager() {
			
			@Override
			protected OWLReasoner updateManaged(OWLReasoner managed) {
				// TODO find out if there is an option to re-use the reasoner
				// TODO how to do incremental changes?
				return createManaged();
			}
			
			@Override
			protected OWLReasoner createManaged() {
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
