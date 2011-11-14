package org.bbop.termgenie.core;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Wrapper of an ontology, provides additional methods for identifying the
 * ontology.
 */
public class Ontology {

	protected String name;
	protected String subOntologyName;
	protected List<String> roots;
	protected String dlQuery;

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
	
	public String getDLQuery() {
		return dlQuery;
	}

	protected void setBranch(String subOntologyName, List<String> roots) {
		this.subOntologyName = subOntologyName;
		this.roots = roots;
	}
	
	protected void setBranch(String subOntologyName, String dlQuery) {
		this.subOntologyName = subOntologyName;
		this.dlQuery = dlQuery;
	}

	public static interface OntologyTerm<S extends ISynonym, R extends IRelation> {
		
		public String getId();

		public String getLabel();

		public String getDefinition();

		public List<S> getSynonyms();

		public List<String> getDefXRef();

		public List<R> getRelations();

		public Map<String, String> getMetaData();
		
		public boolean isObsolete();
	}
	
	/**
	 * Wrapper of an ontology term. Intended to be used during rule-based term
	 * generation.
	 */
	public abstract static class AbstractOntologyTerm implements OntologyTerm<ISynonym, IRelation> {

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
			if (isObsolete() == true) {
				builder.append("isObsolete()=true");
			}
			builder.append("]");
			return builder.toString();
		}

		public static class DefaultOntologyTerm extends AbstractOntologyTerm {

			private final String id;
			private final String label;
			private final String definition;
			private final List<ISynonym> synonyms;
			private final List<String> defXRef;
			private final List<IRelation> relations;
			private final Map<String, String> metaData;
			private final boolean isObsolete;

			public DefaultOntologyTerm(String id,
					String label,
					String definition,
					List<ISynonym> synonyms,
					List<String> defXRef,
					Map<String, String> metaData,
					List<IRelation> relations,
					boolean isObsolete)
			{
				super();
				this.id = id;
				this.label = label;
				this.definition = definition;
				this.synonyms = synonyms;
				this.defXRef = defXRef;
				this.relations = relations;
				this.metaData = metaData;
				this.isObsolete = isObsolete;
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
			public List<ISynonym> getSynonyms() {
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
			public List<IRelation> getRelations() {
				return relations;
			}

			/**
			 * @return the metaData
			 */
			@Override
			public Map<String, String> getMetaData() {
				return metaData;
			}

			@Override
			public boolean isObsolete() {
				return isObsolete;
			}
		}
	}
	
	public static interface IRelation {

		/**
		 * @return the source
		 */
		public String getSource();

		/**
		 * @return the target
		 */
		public String getTarget();
		
		/**
		 * @return the target label
		 */
		public String getTargetLabel();

		/**
		 * @return the properties
		 */
		public Map<String, String> getProperties();

		
		public static final Comparator<IRelation> RELATION_SORT_COMPARATOR = new RelationSortComparator();
	}
	
	private static final class RelationSortComparator implements Comparator<IRelation> {
		
		@Override
		public int compare(IRelation r1, IRelation r2) {
			// compare type
			String t1 = Relation.getType(r1.getProperties());
			String t2 = Relation.getType(r2.getProperties());
			int tv1 = value(t1);
			int tv2 = value(t2);
			if (tv1 == tv2) {
				if (tv1 == 4 || tv1 == 2 || tv1 == 1) {
					return r1.getTarget().compareTo(r2.getTarget());
				}
				if (tv1 == 3) {
					String rs1 = Relation.getRelationShip(r1.getProperties());
					String rs2 = Relation.getRelationShip(r2.getProperties());
					if (rs1 == null && rs2 == null) {
						return r1.getTarget().compareTo(r2.getTarget());
					}
					if (rs1 == null) {
						return -1;
					}
					if (rs2 == null) {
						return 1;
					}
					if (rs1.equals(rs2)) {
						return r1.getTarget().compareTo(r2.getTarget());
					}
					return rs1.compareTo(rs2);
				}
				if (tv1 == 0) {
					if (t1.equals(t2)) {
						return r1.getTarget().compareTo(r2.getTarget());
					}
					return t1.compareTo(t2);
				}
				return 0;
			}
			return tv2 - tv1;
		}
	
		private int value(String type) {
			if (type.equals(OboFormatTag.TAG_IS_A.getTag())) {
				return 4;
			}
			if (type.equals(OboFormatTag.TAG_INTERSECTION_OF.getTag())) {
				return 3;
			}
			if (type.equals(OboFormatTag.TAG_UNION_OF.getTag())) {
				return 2;
			}
			if (type.equals(OboFormatTag.TAG_DISJOINT_FROM.getTag())) {
				return 1;
			}
			return 0;
		}
	}

	public static class Relation implements IRelation {

		private final String source;
		private final String target;
		private final String targetLabel;
		private final Map<String, String> properties;

		/**
		 * @param source
		 * @param target
		 * @param targetLabel 
		 * @param properties
		 */
		public Relation(String source, String target, String targetLabel, Map<String, String> properties) {
			super();
			this.source = source;
			this.target = target;
			this.targetLabel = targetLabel;
			this.properties = properties;
		}

		/**
		 * @return the source
		 */
		@Override
		public String getSource() {
			return source;
		}

		/**
		 * @return the target
		 */
		@Override
		public String getTarget() {
			return target;
		}
		
		/**
		 * @return the target label;
		 */
		@Override
		public String getTargetLabel() {
			return targetLabel;
		}

		/**
		 * @return the properties
		 */
		@Override
		public Map<String, String> getProperties() {
			return properties;
		}
		
		public static void setType(Map<String, String> properties, OboFormatTag tag) {
			setType(properties, tag.getTag());
		}
		
		public static void setType(Map<String, String> properties, OboFormatTag tag, String relationshipType) {
			setType(properties, tag.getTag(), relationshipType);
		}
		
		public static void setType(Map<String, String> properties, String type, String relationshipType) {
			setType(properties, type);
			if (relationshipType != null) {
				properties.put("relationship", relationshipType);
			}
		}
		
		public static void setType(Map<String, String> properties, String relationship) {
			properties.put("type", relationship);
		}
		
		public static String getType(Map<String, String> properties) {
			return properties.get("type");
		}
		
		public static String getRelationShip(Map<String, String> properties) {
			return properties.get("relationship");
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Relation [source=");
			builder.append(source);
			builder.append(", target=");
			builder.append(target);
			builder.append(", targetLabel=");
			builder.append(targetLabel);
			builder.append(", properties=");
			builder.append(properties);
			builder.append("]");
			return builder.toString();
		}
	}
}
