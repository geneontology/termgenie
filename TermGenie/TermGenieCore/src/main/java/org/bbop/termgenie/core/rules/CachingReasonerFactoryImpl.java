package org.bbop.termgenie.core.rules;

import java.util.HashMap;
import java.util.Map;

import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.tools.Pair;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CachingReasonerFactoryImpl extends ReasonerFactoryImpl implements
		EventSubscriber<OntologyChangeEvent>
{

	private final Map<String, Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>> allManagers;

	@Inject
	CachingReasonerFactoryImpl(@Named("ReasonerFactoryDefaultReasoner") String defaultReasoner) {
		super(defaultReasoner);
		allManagers = new HashMap<String, Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>>();
		EventBus.subscribe(OntologyChangeEvent.class, this);
	}

	@Override
	protected ReasonerTaskManager getManager(OWLGraphWrapper ontology, String reasonerName) {
		synchronized (allManagers) {
			Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>> managers = allManagers.get(reasonerName);
			if (managers == null) {
				managers = new HashMap<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>();
				allManagers.put(reasonerName, managers);
			}
			String ontologyName = ontology.getOntologyId();
			Pair<OWLGraphWrapper, ReasonerTaskManager> pair = managers.get(ontologyName);
			if (pair == null || pair.getOne() != ontology) {
				pair = new Pair<OWLGraphWrapper, ReasonerTaskManager>(ontology, super.getManager(ontology,
						reasonerName));
				managers.put(ontologyName, pair);
			}
			return pair.getTwo();
		}
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
		updateBuffered(event.getManager().getOntologyId());
	}

	@Override
	public void updateBuffered(String id) {
		synchronized (allManagers) {
			for (String reasonerName : allManagers.keySet()) {
				Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>> managers = allManagers.get(reasonerName);
				if (managers != null && !managers.isEmpty()) {
					managers.remove(id);
				}
			}
		}
	}

}
