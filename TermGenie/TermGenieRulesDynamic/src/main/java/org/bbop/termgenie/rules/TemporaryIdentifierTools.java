package org.bbop.termgenie.rules;

import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.obolibrary.obo2owl.Obo2OWLConstants;

import owltools.graph.OWLGraphWrapper;


public class TemporaryIdentifierTools {
	
	public static String getTempIdPrefix(OntologyTaskManager manager) {
		try {
			GetTempIdPrefix job = new GetTempIdPrefix();
			manager.runManagedTask(job);
			
			return job.prefix;
		} catch (InvalidManagedInstanceException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private static class GetTempIdPrefix extends OntologyTask {

		String prefix = null;
		
		@Override
		protected void runCatching(OWLGraphWrapper managed) throws TaskException, Exception {
			prefix = getTempIdPrefix(managed);
		}
	}

	public static String getTempIdPrefix(OWLGraphWrapper ontology) {
		return Obo2OWLConstants.DEFAULT_IRI_PREFIX + ontology.getOntologyId().toUpperCase()+"_"+"TEMP-";
	}
}
