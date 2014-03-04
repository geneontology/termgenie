package org.bbop.termgenie.core.rules;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.eventbus.OntologyChangeEvent;
import org.bbop.termgenie.core.eventbus.SecondaryOntologyChangeEvent;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ReasonerFactoryImpl implements ReasonerFactory, EventSubscriber<OntologyChangeEvent> {

	private static final Logger logger = Logger.getLogger(ReasonerFactoryImpl.class);
	private static final OWLReasonerFactory factory = new ElkReasonerFactory();

	private volatile SharedReasoner cached = null;

	@Inject
	public ReasonerFactoryImpl() {
		super();
		EventBus.subscribe(OntologyChangeEvent.class, this);
	}

	@Override
	public OWLReasoner createReasoner(OWLGraphWrapper graph, ProcessState state) {
		OWLOntology ontology = graph.getSourceOntology();
		logger.info("Creating reasoner for: "+ontology.getOntologyID());
		ReasonerProgressWriter monitor = new ReasonerProgressWriter(state);
		OWLReasonerConfiguration config = new SimpleConfiguration(monitor);
		OWLReasoner reasoner = factory.createReasoner(ontology, config);
		return reasoner;
	}
	
	@Override
	public SharedReasoner getSharedReasoner(OWLGraphWrapper ontology) {
		synchronized (this) {
			if (cached == null) {
				cached = createSharedReasoner(ontology);
			}
		}
		return cached;
	}
	
	private SharedReasoner createSharedReasoner(OWLGraphWrapper graph) {
		OWLOntology ontology = graph.getSourceOntology();
		String name = "reasoner-manager-" + factory.getReasonerName() + "-" + ontology.getOntologyID();
		return new ReasonerTaskManagerImpl(name, factory, ontology);
	}

	/**
	 * Handle an ontology change. This assumes that an ontology change can only
	 * be executed, if the {@link OntologyTaskManager} has the lock and
	 * reasoning only happens also in locked phase via the
	 * {@link SharedReasoner}. Otherwise, it is not guaranteed, that the
	 * reasoner instances are up-to-date.
	 * 
	 * @param event
	 */
	@Override
	public void onEvent(OntologyChangeEvent event) {
		synchronized (this) {
			if (cached != null) {
				cached.dispose();
				cached = null;
			}
		}
		EventBus.publish(new SecondaryOntologyChangeEvent(event.getManager(), event.isReset()));
	}
	
	static final class ReasonerTaskManagerImpl extends SharedReasoner {
	
		private final OWLReasonerFactory reasonerFactory;
		private final OWLOntology ontology;
	
		ReasonerTaskManagerImpl(String name,
				OWLReasonerFactory reasonerFactory,
				OWLOntology ontology)
		{
			super(name);
			this.reasonerFactory = reasonerFactory;
			this.ontology = ontology;
		}
	
		@Override
		protected OWLReasoner updateManaged(OWLReasoner managed) {
			if (managed != null) {
				managed.dispose();	
			}
			return createManaged();
		}
	
		@Override
		protected OWLReasoner createManaged() {
			logger.info("Create reasoner: " + reasonerFactory.getReasonerName() + " for ontology: " + ontology.getOntologyID());
			OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
			return reasoner;
		}
	
		@Override
		protected OWLReasoner resetManaged(OWLReasoner managed) {
			// Do nothing as a reasoner cannot change the underlying
			// ontology
			return managed;
		}

		@Override
		public void dispose(OWLReasoner managed) {
			managed.dispose();
		}
	}
	
	private static final class ReasonerProgressWriter implements ReasonerProgressMonitor {
		
		private static final ThreadLocal<NumberFormat> nf = new ThreadLocal<NumberFormat>(){

			@Override
			protected NumberFormat initialValue() {
				return new DecimalFormat("#0.00");
			}
		};
		
		private final ProcessState state;
		private String currentTaskName = null;
		
		ReasonerProgressWriter(ProcessState state) {
			super();
			this.state = state;
		}

		@Override
		public void reasonerTaskStopped() {
			if (state != null) {
				StringBuilder message = new StringBuilder("Reasoner subtask end");
				if (currentTaskName != null) {
					message.append(": ").append(currentTaskName);
				}
				ProcessState.addMessage(state, message.toString());
			}
			currentTaskName = null;
		}
	
		@Override
		public void reasonerTaskStarted(String taskName) {
			currentTaskName = taskName;
			if (state != null) {
				ProcessState.addMessage(state, "Reasoner subtask start: "+currentTaskName);
			}
		}
	
		@Override
		public void reasonerTaskProgressChanged(int value, int max) {
			if (state != null) {
				double progress = 0.0;
				if (value > 0 && max > 0) {
					progress = ((double)value / max) * 100.0;
				}
				ProcessState.addMessage(state, "Reasoner subtask progress: "+nf.get().format(progress)+"%");
			}
		}
	
		@Override
		public void reasonerTaskBusy() {
			// do nothing.
		}
	}
}
