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
	public static class Ontology
	{
		protected OWLGraphWrapper realInstance;
		protected String name;
		protected String subOntologyName;
		protected String subOntologyParentId;
		
		/**
		 * @param realInstance
		 * @param name
		 * @param subOntologyName
		 * @param subOntologyParentId
		 */
		protected Ontology(OWLGraphWrapper realInstance, String name, String subOntologyName,
				String subOntologyParentId) {
			super();
			this.realInstance = realInstance;
			this.name = name;
			this.subOntologyName = subOntologyName;
			this.subOntologyParentId = subOntologyParentId;
		}

		protected void setRealInstance(OWLGraphWrapper realInstance) {
			this.realInstance = realInstance;
		}
		
		public OWLGraphWrapper getRealInstance() {
			return realInstance;
		}
		
		public String getUniqueName() {
			return name;
		}
		
		public String getBranch() {
			return subOntologyName;
		}
		
		public String getBranchId() {
			return subOntologyParentId;
		}
		
		protected void setBranch(String subOntologyName, String subOntologyParentId) {
			this.subOntologyName = subOntologyName;
			this.subOntologyParentId= subOntologyParentId;
		} 
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
		public abstract List<String> getDefXRef();
		public abstract String getComment();

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
			if (getDefXRef() != null)
				builder.append("getSynonyms()=").append(getDefXRef());
			if (getComment() != null)
				builder.append("getSynonyms()=").append(getComment());
			builder.append("]");
			return builder.toString();
		}
		
		public static class DefaultOntologyTerm extends OntologyTerm {
			
			private final String id;
			private final String label;
			private final String definition;
			private final Set<String> synonyms;
			private final String logicalDefinition;
			private final List<String> defXRef;
			private final String comment;
			/**
			 * @param id
			 * @param label
			 * @param definition
			 * @param synonyms
			 * @param logicalDefinition
			 */
			public DefaultOntologyTerm(String id, String label, String definition,
					Set<String> synonyms, String logicalDefinition, List<String> defXRef, String comment) {
				super();
				this.id = id;
				this.label = label;
				this.definition = definition;
				this.synonyms = synonyms;
				this.logicalDefinition = logicalDefinition;
				this.defXRef = defXRef;
				this.comment = comment;
			}
			/**
			 * @return the id
			 */
			@Override
			public String getId() {
				return id;
			}
			
			/**
			 * @return the label
			 */
			@Override
			public String getLabel() {
				return label;
			}
			
			/**
			 * @return the definition
			 */
			@Override
			public String getDefinition() {
				return definition;
			}
			
			/**
			 * @return the synonyms
			 */
			@Override
			public Set<String> getSynonyms() {
				return synonyms;
			}
			
			/**
			 * @return the logicalDescription
			 */
			@Override
			public String getLogicalDefinition() {
				return logicalDefinition;
			}
			
			/**
			 * @return the defXRef
			 */
			@Override
			public List<String> getDefXRef() {
				return defXRef;
			}
			/**
			 * @return the comment
			 */
			@Override
			public String getComment() {
				return comment;
			}
		}
	}
}