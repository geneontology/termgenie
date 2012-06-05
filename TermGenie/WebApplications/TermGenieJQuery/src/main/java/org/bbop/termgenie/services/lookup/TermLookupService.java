package org.bbop.termgenie.services.lookup;

import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;


public interface TermLookupService {

	public void lookup(String id, LookupCallBack callback);
	
	
	public static interface LookupCallBack {
		
		public void regular(OWLGraphWrapper graph, OWLClass cls, String id);
		
		public void pending(String id, String label);
		
		public void unknown(String id);
		
		public void error(String message, Exception e);
		
	}
}
