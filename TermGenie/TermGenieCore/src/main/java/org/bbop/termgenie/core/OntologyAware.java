package org.bbop.termgenie.core;

import java.util.List;
import java.util.Set;

import owltools.graph.OWLGraphWrapper;

/**
 * Interface to specify the methods required to access ontologies.
 *
 */
public interface OntologyAware {

	/**
	 * Retrieve the corresponding ontology, connected to this object.
	 * 
	 * @return List of ontologies or null if not available
	 */
	public List<Ontology> getCorrespondingOntologies();
	
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
		public abstract String getDefinition();
		public abstract Set<String> getSynonyms();
		public abstract String getLogicalDefinition();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("OntologyTerm [");
			if (getId() != null)
				builder.append("getId()=").append(getId()).append(", ");
			if (getLabel() != null)
				builder.append("getLabel()=").append(getLabel()).append(", ");
			if (getDefinition() != null)
				builder.append("getDefinition()=").append(getDefinition()).append(", ");
			if (getSynonyms() != null)
				builder.append("getSynonyms()=").append(getSynonyms());
			builder.append("]");
			return builder.toString();
		}
		
		public static class DefaultOntologyTerm extends OntologyTerm {
			
			private final String id;
			private final String label;
			private final String definition;
			private final Set<String> synonyms;
			private final String logicalDefinition;
			/**
			 * @param id
			 * @param label
			 * @param definition
			 * @param synonyms
			 * @param logicalDefinition
			 */
			public DefaultOntologyTerm(String id, String label, String definition,
					Set<String> synonyms, String logicalDefinition) {
				super();
				this.id = id;
				this.label = label;
				this.definition = definition;
				this.synonyms = synonyms;
				this.logicalDefinition = logicalDefinition;
			}
			/**
			 * @return the id
			 */
			public String getId() {
				return id;
			}
			/**
			 * @return the label
			 */
			public String getLabel() {
				return label;
			}
			/**
			 * @return the definition
			 */
			public String getDefinition() {
				return definition;
			}
			/**
			 * @return the synonyms
			 */
			public Set<String> getSynonyms() {
				return synonyms;
			}
			/**
			 * @return the logicalDescription
			 */
			public String getLogicalDefinition() {
				return logicalDefinition;
			}
		}
	}
}