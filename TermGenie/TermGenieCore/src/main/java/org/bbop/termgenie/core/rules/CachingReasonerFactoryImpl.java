package org.bbop.termgenie.core.rules;

import java.util.HashMap;
import java.util.Map;

import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.core.eventbus.SecondaryOntologyChangeEvent;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.tools.Pair;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CachingReasonerFactoryImpl extends ReasonerFactoryImpl implements
		EventSubscriber<OntologyChangeEvent>
{

	private final Map<String, Pair<OWLGraphWrapper, ReasonerTaskManager>> allManagers;

	@Inject
	CachingReasonerFactoryImpl() {
		super();
		allManagers = new HashMap<String, Pair<OWLGraphWrapper, ReasonerTaskManager>>();
		EventBus.subscribe(OntologyChangeEvent.class, this);
	}

	@Override
	protected ReasonerTaskManager getManager(OWLGraphWrapper ontology) {
		synchronized (allManagers) {
			String ontologyName = ontology.getOntologyId();
			Pair<OWLGraphWrapper, ReasonerTaskManager> pair = allManagers.get(ontologyName);
			if (pair == null || pair.getOne() != ontology) {
				pair = new Pair<OWLGraphWrapper, ReasonerTaskManager>(ontology, super.getManager(ontology));
				allManagers.put(ontologyName, pair);
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
		EventBus.publish(new SecondaryOntologyChangeEvent(event.getManager(), event.isReset()));
	}

	@Override
	public void updateBuffered(String id) {
		synchronized (allManagers) {
			Pair<OWLGraphWrapper, ReasonerTaskManager> pair = allManagers.remove(id);
			if (pair != null) {
				ReasonerTaskManager manager = pair.getTwo();
				if (manager != null) {
					manager.dispose();
				}
			}
		}
	}

}
