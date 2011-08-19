package org.bbop.termgenie.core;

import java.util.List;
import java.util.Map;

import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * Wrapper of an ontology, provides additional methods for identifying the
 * ontology.
 */
public class Ontology {

	protected String name;
	protected String subOntologyName;
	protected List<String> roots;

	/**
	 * @param name
	 * @param subOntologyName
	 * @param roots
	 */
	protected Ontology(String name, String subOntologyName, List<String> roots) {
		super();
		this.name = name;
		this.subOntologyName = subOntologyName;
		this.roots = roots;
	}

	public String getUniqueName() {
		return name;
	}

	public String getBranch() {
		return subOntologyName;
	}

	public List<String> getRoots() {
		return roots;
	}

	protected void setBranch(String subOntologyName, List<String> roots) {
		this.subOntologyName = subOntologyName;
		this.roots = roots;
	}

	/**
	 * Wrapper of an ontology term. Intended to be used during rule-based term
	 * generation.
	 */
	public abstract static class OntologyTerm {

		public abstract String getId();

		public abstract String getLabel();

		public abstract String getDefinition();

		public abstract List<Synonym> getSynonyms();

		public abstract List<String> getDefXRef();

		public abstract List<Relation> getRelations();

		public abstract Map<String, String> getMetaData();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("OntologyTerm [");
			if (getId() != null) {
				builder.append("getId()=");
				builder.append(getId());
				builder.append(", ");
			}
			if (getLabel() != null) {
				builder.append("getLabel()=");
				builder.append(getLabel());
				builder.append(", ");
			}
			if (getDefinition() != null) {
				builder.append("getDefinition()=");
				builder.append(getDefinition());
				builder.append(", ");
			}
			if (getSynonyms() != null) {
				builder.append("getSynonyms()=");
				builder.append(getSynonyms());
				builder.append(", ");
			}
			if (getDefXRef() != null) {
				builder.append("getDefXRef()=");
				builder.append(getDefXRef());
				builder.append(", ");
			}
			if (getRelations() != null) {
				builder.append("getRelations()=");
				builder.append(getRelations());
				builder.append(", ");
			}
			if (getMetaData() != null) {
				builder.append("getMetaData()=");
				builder.append(getMetaData());
			}
			builder.append("]");
			return builder.toString();
		}

		public static class DefaultOntologyTerm extends OntologyTerm {

			private final String id;
			private final String label;
			private final String definition;
			private final List<Synonym> synonyms;
			private final List<String> defXRef;
			private final List<Relation> relations;
			private final Map<String, String> metaData;

			public DefaultOntologyTerm(String id,
					String label,
					String definition,
					List<Synonym> synonyms,
					List<String> defXRef,
					Map<String, String> metaData,
					List<Relation> relations)
			{
				super();
				this.id = id;
				this.label = label;
				this.definition = definition;
				this.synonyms = synonyms;
				this.defXRef = defXRef;
				this.relations = relations;
				this.metaData = metaData;
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
			public List<Synonym> getSynonyms() {
				return synonyms;
			}

			/**
			 * @return the defXRef
			 */
			@Override
			public List<String> getDefXRef() {
				return defXRef;
			}

			/**
			 * @return the relations
			 */
			@Override
			public List<Relation> getRelations() {
				return relations;
			}

			/**
			 * @return the metaData
			 */
			@Override
			public Map<String, String> getMetaData() {
				return metaData;
			}
		}
	}

	public static class Relation {

		private final String source;
		private final String target;
		private final Map<String, String> properties;

		/**
		 * @param source
		 * @param target
		 * @param properties
		 */
		public Relation(String source, String target, Map<String, String> properties) {
			super();
			this.source = source;
			this.target = target;
			this.properties = properties;
		}

		/**
		 * @return the source
		 */
		public String getSource() {
			return source;
		}

		/**
		 * @return the target
		 */
		public String getTarget() {
			return target;
		}

		/**
		 * @return the properties
		 */
		public Map<String, String> getProperties() {
			return properties;
		}
	}
}
