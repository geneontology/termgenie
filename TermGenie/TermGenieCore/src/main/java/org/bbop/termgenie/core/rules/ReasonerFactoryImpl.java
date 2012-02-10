package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelOWLReasonerFactory;

@Singleton
public class ReasonerFactoryImpl implements ReasonerFactory {

	private static final Logger logger = Logger.getLogger(ReasonerFactoryImpl.class);
	static final String REASONER_FACTORY_DEFAULT_REASONER = "ReasonerFactoryDefaultReasoner";
	
	static final String HERMIT = "hermit";
	static final String JCEL = "jcel";
	static final String ELK = "elk";

	private final static Set<String> supportedReasoners = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(JCEL, HERMIT, ELK)));

	private final String defaultReasoner;

	@Inject
	ReasonerFactoryImpl(@Named(REASONER_FACTORY_DEFAULT_REASONER) String defaultReasoner) {
		super();
		this.defaultReasoner = defaultReasoner;
	}

	@Override
	public final ReasonerTaskManager getDefaultTaskManager(OWLGraphWrapper ontology) {
		return getTaskManager(ontology, defaultReasoner);
	}

	@Override
	public final ReasonerTaskManager getTaskManager(OWLGraphWrapper ontology, String reasonerName) {

		if (reasonerName == null || !supportedReasoners.contains(reasonerName)) {
			throw new RuntimeException("Unknown reasoner: " + reasonerName);
		}
		return getManager(ontology, reasonerName);
	}

	protected ReasonerTaskManager getManager(OWLGraphWrapper ontology, String reasonerName) {
		return createManager(ontology, reasonerName);
	}

	@Override
	public Collection<String> getSupportedReasoners() {
		return supportedReasoners;
	}

	@Override
	public void updateBuffered(String id) {
		// do nothing
	}

	private ReasonerTaskManager createManager(OWLGraphWrapper ontology, String reasonerName) {
		if (JCEL.equals(reasonerName)) {
			OWLReasonerFactory factory = new JcelOWLReasonerFactory();
			return createManager(ontology, factory);
		}
		else if (HERMIT.equals(reasonerName)) {
			return createManager(ontology, new Reasoner.ReasonerFactory());
		}
		else if (ELK.equals(reasonerName)) {
			return createManager(ontology, new ElkReasonerFactory());
		}
		return null;
	}

	private ReasonerTaskManager createManager(OWLGraphWrapper graph,
			final OWLReasonerFactory reasonerFactory)
	{
		final OWLOntology ontology = graph.getSourceOntology();
		Set<OWLOntology> importsClosure = ontology.getImportsClosure();
		Set<OWLOntology> supportOntologies = graph.getSupportOntologySet();
		if (importsClosure.containsAll(supportOntologies) == false) {
			throw new RuntimeException("Import closure for: "+ontology.getOntologyID()+" does not contain all support ontologies.");
		}
		
		return new ReasonerTaskManagerImpl("reasoner-manager-" + reasonerFactory.getReasonerName() + "-" + ontology.getOntologyID(), reasonerFactory, ontology);
	}

	private static final class ReasonerTaskManagerImpl extends ReasonerTaskManager {
	
		private final OWLReasonerFactory reasonerFactory;
		private final OWLOntology ontology;
	
		private ReasonerTaskManagerImpl(String name,
				OWLReasonerFactory reasonerFactory,
				OWLOntology ontology)
		{
			super(name);
			this.reasonerFactory = reasonerFactory;
			this.ontology = ontology;
		}
	
		@Override
		protected OWLReasoner updateManaged(OWLReasoner managed) {
			return createManaged();
		}
	
		@Override
		protected OWLReasoner createManaged() {
			logger.info("Create reasoner: " + reasonerFactory.getReasonerName() + " for ontology: " + ontology.getOntologyID());
			OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
			reasoner.precomputeInferences(InferenceType.values());
			return reasoner;
		}
	
		@Override
		protected OWLReasoner resetManaged(OWLReasoner managed) {
			// Do nothing as a reasoner cannot change the underlying
			// ontology
			return managed;
		}
	}
}
