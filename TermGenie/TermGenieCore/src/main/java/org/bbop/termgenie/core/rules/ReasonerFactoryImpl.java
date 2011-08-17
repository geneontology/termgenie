package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelOWLReasonerFactory;

@Singleton
public class ReasonerFactoryImpl implements ReasonerFactory {

	private static final Logger logger = Logger.getLogger(ReasonerFactoryImpl.class);
	static final String REASONER_FACTORY_DEFAULT_REASONER = "ReasonerFactoryDefaultReasoner";
	
	static final String PELLET = "pellet";
	static final String HERMIT = "hermit";
	static final String JCEL = "jcel";

	private final static Set<String> supportedReasoners = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(JCEL, HERMIT, PELLET)));

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

	private ReasonerTaskManager createManager(OWLGraphWrapper ontology, String reasonerName) {
		if (JCEL.equals(reasonerName)) {
			OWLReasonerFactory factory = new JcelOWLReasonerFactory();
			return createManager(ontology.getSourceOntology(), factory);
		}
		else if (HERMIT.equals(reasonerName)) {
			return createManager(ontology.getSourceOntology(), new Reasoner.ReasonerFactory());
		}
		else if (PELLET.equals(reasonerName)) {
			return createManager(ontology.getSourceOntology(), PelletReasonerFactory.getInstance());
		}
		return null;
	}

	private ReasonerTaskManager createManager(final OWLOntology ontology,
			final OWLReasonerFactory reasonerFactory)
	{
		return new ReasonerTaskManager("reasoner-manager-" + reasonerFactory.getReasonerName() + "-" + ontology.getOntologyID())
		{

			@Override
			protected OWLReasoner updateManaged(OWLReasoner managed) {
				// Do nothing, as the reasoner should reflect changes to
				// the underlying ontology (non-buffering).
				return managed;
			}

			@Override
			protected OWLReasoner createManaged() {
				logger.info("Create reasoner: " + reasonerFactory.getReasonerName() + " for ontology: " + ontology.getOntologyID());
				OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
				reasoner.precomputeInferences();
				return reasoner;
			}

			@Override
			protected OWLReasoner resetManaged(OWLReasoner managed) {
				// Do nothing as a reasoner cannot change the underlying
				// ontology
				return managed;
			}
		};
	}
}
