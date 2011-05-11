package org.bbop.termgenie.core;

import owltools.graph.OWLGraphWrapper;

/**
 * Interface to specify the methods required to access ontologies.
 *
 */
public interface OntologyAware {

	/**
	 * Retrieve the corresponding ontology, connected to this object.
	 * 
	 * @return ontology or null if not available
	 */
	public Ontology getCorrespondingOntology();
	
	/**
	 * Wrapper of an ontology, provides additional methods for identifying the ontology.
	 */
	public abstract static class Ontology
	{
		public abstract OWLGraphWrapper getRealInstance();
		
		public abstract String getUniqueName();
		
		public abstract String getBranch();
	}

	/**
	 * Wrapper of an ontology term. Intended to be used during 
	 * rule-based term generation.
	 * TODO: Fill this with appropriate methods.
	 */
	public abstract static class OntologyTerm
	{
		public abstract String getId();
		public abstract String getLabel();
		public abstract String getDescription();
		public abstract String getReferenceLink();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("OntologyTerm [");
			if (getId() != null)
				builder.append("getId()=").append(getId()).append(", ");
			if (getLabel() != null)
				builder.append("getLabel()=").append(getLabel()).append(", ");
			if (getDescription() != null)
				builder.append("getDescription()=").append(getDescription()).append(", ");
			if (getReferenceLink() != null)
				builder.append("getReferenceLink()=").append(getReferenceLink());
			builder.append("]");
			return builder.toString();
		}

		
	}
}