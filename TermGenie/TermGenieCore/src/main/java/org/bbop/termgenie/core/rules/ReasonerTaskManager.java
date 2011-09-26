package org.bbop.termgenie.core.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;

import owltools.graph.OWLGraphWrapper;

public abstract class ReasonerTaskManager extends GenericTaskManager<OWLReasoner> {

	private static final Logger logger = Logger.getLogger(ReasonerTaskManager.class);
	
	protected ReasonerTaskManager(String name) {
		super(name);
	}

	@Override
	protected OWLReasoner resetManaged(OWLReasoner managed) {
		// Do nothing as the reasoner cannot change the ontology.
		return managed;
	}

	@Override
	protected void setChanged(boolean reset) {
		// do nothing for now
	}

	/**
	 * A task which requires a reasoner.
	 */
	public static interface ReasonerTask extends ManagedTask<OWLReasoner> {
		// intentionally empty
	}

	// implement some common reasoner tasks.

	public Collection<OWLObject> getDescendants(final OWLObject x, final OWLGraphWrapper wrapper) {
		final Collection<OWLObject> result = new HashSet<OWLObject>();
		ReasonerTask task = new ReasonerTask() {

			@Override
			public Modified run(OWLReasoner managed) {
				NodeSet<OWLClass> subClasses = managed.getSubClasses(wrapper.getOWLClass(x), false);
				result.addAll(subClasses.getFlattened());
				return Modified.no;
			}
		};
		runManagedTask(task);
		return result;
	}

	public Collection<OWLObject> getAncestors(final OWLObject x, final OWLGraphWrapper wrapper) {
		final Collection<OWLObject> result = new HashSet<OWLObject>();
		ReasonerTask task = new ReasonerTask() {

			@Override
			public Modified run(OWLReasoner managed) {
				NodeSet<OWLClass> subClasses = managed.getSuperClasses(wrapper.getOWLClass(x),
						false);
				result.addAll(subClasses.getFlattened());
				return Modified.no;
			}
		};
		runManagedTask(task);
		return result;
	}
	
	private final class DLQueryExecutor implements ReasonerTask {
	
		private final String queryString;
		private final OWLGraphWrapper wrapper;
		
		private Set<OWLObject> result = null;
		private Exception exception;
	
		private DLQueryExecutor(String queryString, OWLGraphWrapper wrapper)
		{
			this.queryString = queryString;
			this.result = new HashSet<OWLObject>();
			this.wrapper = wrapper;
		}
	
		@Override
		public Modified run(OWLReasoner managed)
		{
			QueryEngine engine = QueryEngine.create(wrapper.getManager(), managed, true);
			try {
				Query query = Query.create(queryString);
				QueryResult queryResult = engine.execute(query);
				if(queryResult.ask()) {
					result = new HashSet<OWLObject>();
					for (Iterator<QueryBinding> iterator = queryResult.iterator(); iterator.hasNext();) {
						QueryBinding binding = iterator.next();
						Set<QueryArgument> boundArgs = binding.getBoundArgs();
						for (QueryArgument queryArgument : boundArgs) {
							if (queryArgument.isURI()) {
								String value = queryArgument.getValue();
								OWLObject owlObject = wrapper.getOWLObject(value);
								if (owlObject != null) {
									result.add(owlObject);
								}
							}
						}
						
					}
				}
			} catch (QueryParserException exception) {
				this.exception = exception;
			} catch (QueryEngineException exception) {
				this.exception = exception;
			}
			return Modified.no;
		}
	}

	public Set<OWLObject> executeDLQuery(final String queryString, final OWLGraphWrapper wrapper) {
		DLQueryExecutor task = new DLQueryExecutor(queryString, wrapper);
		runManagedTask(task);
		if (task.exception == null && task.result != null) {
			return task.result;
		}
		if (task.exception != null) {
			logger.error("Could not execute DL query: "+queryString+" for ontology: "+wrapper.getOntologyId(), task.exception);
		}
		return Collections.emptySet();
	}
}
