package org.bbop.termgenie.core.rules;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Singleton;

@Singleton
public class ReasonerFactoryImpl implements ReasonerFactory {

	private static final Logger logger = Logger.getLogger(ReasonerFactoryImpl.class);
	
	@Override
	public final ReasonerTaskManager getDefaultTaskManager(OWLGraphWrapper ontology) {
		return getTaskManager(ontology);
	}

	@Override
	public final ReasonerTaskManager getTaskManager(OWLGraphWrapper ontology) {

		return getManager(ontology);
	}

	protected ReasonerTaskManager getManager(OWLGraphWrapper ontology) {
		return createManager(ontology, new ElkReasonerFactory());
	}


	@Override
	public void updateBuffered(String id) {
		// do nothing
	}

	private ReasonerTaskManager createManager(OWLGraphWrapper graph,
			final OWLReasonerFactory reasonerFactory)
	{
		OWLOntology ontology = graph.getSourceOntology();
		String name = "reasoner-manager-" + reasonerFactory.getReasonerName() + "-" + ontology.getOntologyID();
		return new ReasonerTaskManagerImpl(name, reasonerFactory, ontology);
	}

	private static final class ReasonerTaskManagerImpl extends ReasonerTaskManager {
	
		private final OWLReasonerFactory reasonerFactory;
		private final OWLOntology ontology;
		private final ReasonerProgressWriter monitor;
	
		private ReasonerTaskManagerImpl(String name,
				OWLReasonerFactory reasonerFactory,
				OWLOntology ontology)
		{
			super(name);
			this.reasonerFactory = reasonerFactory;
			this.ontology = ontology;
			this.monitor = new ReasonerProgressWriter();
		}
	
		@Override
		protected OWLReasoner updateManaged(OWLReasoner managed) {
			managed.dispose();
			return createManaged();
		}
	
		@Override
		protected OWLReasoner createManaged() {
			logger.info("Create reasoner: " + reasonerFactory.getReasonerName() + " for ontology: " + ontology.getOntologyID());
			OWLReasonerConfiguration config = new SimpleConfiguration(monitor);
			OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config );
			reasoner.precomputeInferences(InferenceType.values());
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

		@Override
		public void setProcessState(ProcessState state) {
			monitor.setProcessState(state);
		}
		
		@Override
		public void removeProcessState() {
			monitor.removeProcessState();
		}
		
		private static final class ReasonerProgressWriter implements ReasonerProgressMonitor {
			
			private static final ThreadLocal<NumberFormat> nf = new ThreadLocal<NumberFormat>(){

				@Override
				protected NumberFormat initialValue() {
					return new DecimalFormat("#0.00");
				}
			};
			
			private ProcessState state = null;
			private String currentTaskName = null;
			
			public void setProcessState(ProcessState state) {
				this.state = state;
			}
			
			public void removeProcessState() {
				state = null;
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
}
