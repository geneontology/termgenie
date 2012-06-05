package org.bbop.termgenie.services.lookup;

import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.ontology.CommitHistoryStore;
import org.bbop.termgenie.ontology.CommitHistoryStore.CommitHistoryStoreException;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class TermLookupServiceDefaultImpl implements TermLookupService {


	private final OntologyLoader loader;
	private final CommitHistoryStoreHolder holder;

	@Inject
	public TermLookupServiceDefaultImpl(OntologyLoader loader,
			CommitHistoryStoreHolder holder) {
		this.loader = loader;
		this.holder = holder;
	}
	
	static class CommitHistoryStoreHolder {
		
		@Inject(optional=true)
		CommitHistoryStore store = null;
	}
	
	@Override
	public void lookup(final String id, final LookupCallBack callback) {
		List<OntologyTaskManager> ontologies = loader.getOntologies();
		for (OntologyTaskManager manager : ontologies) {
			try {
				FindClassTask task = new FindClassTask(callback, id);
				manager.runManagedTask(task);
				if (task.success) {
					return;
				}
			} catch (InvalidManagedInstanceException exception) {
				callback.error("Could not determine state of the term due to an internal error (inconsistent state)", exception);
			}
		}
		if (holder != null && holder.store != null) {
			try {
				List<Pair<String, String>> pairs = holder.store.checkRecentCommitIds(Collections.singletonList(id));
				if (pairs != null && !pairs.isEmpty()) {
					Pair<String, String> pair = pairs.get(0);
					callback.pending(id, pair.getTwo());
					return;
				}
			} catch (CommitHistoryStoreException exception) {
				callback.error("Could not determine state of the term due to an internal error (database error)", exception);
			}
			
		}
		callback.unknown(id);
	}

	private static final class FindClassTask extends OntologyTask {
	
		private final LookupCallBack callback;
		private final String id;
		
		private boolean success = false;
	
		private FindClassTask(LookupCallBack callback, String id) {
			this.callback = callback;
			this.id = id;
		}
	
		@Override
		protected void runCatching(OWLGraphWrapper graph) throws TaskException, Exception
		{
			OWLClass owlClass = graph.getOWLClassByIdentifier(id);
			if (owlClass != null) {
				callback.regular(graph, owlClass, id);
				success = true;
			}
		}
	}

}
