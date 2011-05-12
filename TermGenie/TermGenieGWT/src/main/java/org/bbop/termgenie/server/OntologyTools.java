package org.bbop.termgenie.server;

import org.bbop.termgenie.core.OntologyAware.Ontology;

import owltools.graph.OWLGraphWrapper;

public class OntologyTools {
	
	static final String GENE_ONTOLOGY_NAME = "GeneOntology";
	
	static final OntologyTools instance = new OntologyTools();
	
	Ontology getOntology(String ontology) {
		// TODO remove hard-coded mapping and support more ontologies
		if (ontology.startsWith(GENE_ONTOLOGY_NAME)) {
			String branch = null;
			int length = GENE_ONTOLOGY_NAME.length();
			if (ontology.length() > length + 1 && ontology.charAt(length) == '|') {
				branch = ontology.substring(length + 1);
			} 
			return new SimpleOntology(GENE_ONTOLOGY_NAME, branch);
		}
		return null;
	}
	
	private final class SimpleOntology extends Ontology {
		
		private final String name;
		private final String branch;
	
		/**
		 * @param name
		 * @param branch
		 */
		private SimpleOntology(String name, String branch) {
			super();
			this.name = name;
			this.branch = branch;
		}
	
		@Override
		public OWLGraphWrapper getRealInstance() {
			return null;
		}
	
		@Override
		public String getUniqueName() {
			return name;
		}
	
		@Override
		public String getBranch() {
			return branch;
		}
	}

	String getOntologyName(Ontology ontology) {
		StringBuilder sb = new StringBuilder();
		sb.append(ontology.getUniqueName());
		String branch = ontology.getBranch();
		if (branch != null) {
			sb.append('|');
			sb.append(branch);
		}
		return sb.toString();
	}
}
