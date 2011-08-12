package org.bbop.termgenie.core.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.tools.Pair;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelOWLReasonerFactory;

import owltools.graph.OWLGraphWrapper;

@Singleton
public final class ReasonerFactoryImpl implements
		ReasonerFactory,
		EventSubscriber<OntologyChangeEvent>
{

	private static final Logger logger = Logger.getLogger(ReasonerFactoryImpl.class);

	static final String PELLET = "pellet";
	static final String HERMIT = "hermit";
	static final String JCEL = "jcel";

	private final static Set<String> supportedReasoners = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(JCEL,
			HERMIT,
			PELLET)));

	private final Map<String, Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>> allManagers;
	private final String defaultReasoner;

	@Inject
	ReasonerFactoryImpl(@Named("ReasonerFactoryDefaultReasoner") String defaultReasoner) {
		super();
		allManagers = new HashMap<String, Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>>();
		this.defaultReasoner = defaultReasoner;
		EventBus.subscribe(OntologyChangeEvent.class, this);
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

	private ReasonerTaskManager getManager(OWLGraphWrapper ontology, String reasonerName) {
		synchronized (allManagers) {
			Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>> managers = allManagers.get(reasonerName);
			if (managers == null) {
				managers = new HashMap<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>();
				allManagers.put(reasonerName, managers);
			}
			String ontologyName = ontology.getOntologyId();
			Pair<OWLGraphWrapper, ReasonerTaskManager> pair = managers.get(ontologyName);
			if (pair == null || pair.getOne() != ontology) {
				pair = new Pair<OWLGraphWrapper, ReasonerTaskManager>(ontology, createManager(ontology,
						reasonerName));
				managers.put(ontologyName, pair);
			}
			return pair.getTwo();
		}
	}

	@Override
	public Collection<String> getSupportedReasoners() {
		return supportedReasoners;
	}

	/**
	 * Handle an ontology change. This assumes that an ontology change can only
	 * be executed, if the {@link OntologyTaskManager} has the lock and
	 * reasoning only happens also in locked phase via the
	 * {@link OntologyTaskManager}. Otherwise, it is not guaranteed, that the
	 * reasoner instances are up-to-date.
	 * 
	 * @param event
	 */
	@Override
	public void onEvent(OntologyChangeEvent event) {
		synchronized (allManagers) {
			for (String reasonerName : allManagers.keySet()) {
				Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>> managers = allManagers.get(reasonerName);
				if (managers != null && !managers.isEmpty()) {
					managers.remove(event.getManager().getOntologyId());
				}
			}
		}
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
