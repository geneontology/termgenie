package org.bbop.termgenie.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonTermRelation;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class JsonOntologyTerm implements OntologyTerm<JsonSynonym, JsonTermRelation>{

	private String tempId;
	private String label;
	private String definition;
	private List<JsonSynonym> synonyms;
	private List<String> defXRef;
	private List<JsonTermRelation> relations;
	private Map<String, String> metaData;

	public JsonOntologyTerm() {
		super();
	}

	/**
	 * @return the tempId
	 */
	public String getTempId() {
		return tempId;
	}

	@Override
	public String getId() {
		return tempId;
	}

	/**
	 * @param tempId the tempId to set
	 */
	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the definition
	 */
	@Override
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the defxRef
	 */
	@Override
	public List<String> getDefXRef() {
		return defXRef;
	}

	/**
	 * @param defXRef the defxRef to set
	 */
	public void setDefXRef(List<String> defXRef) {
		this.defXRef = defXRef;
	}

	/**
	 * @return the relations
	 */
	@Override
	public List<JsonTermRelation> getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(List<JsonTermRelation> relations) {
		this.relations = relations;
	}

	/**
	 * @return the synonyms
	 */
	@Override
	public List<JsonSynonym> getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<JsonSynonym> synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return the metaData
	 */
	@Override
	public Map<String, String> getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonOntologyTerm [");
		builder.append("tempId=");
		builder.append(tempId);
		if (label != null) {
			builder.append(", ");
			builder.append("label=");
			builder.append(label);
		}
		if (definition != null) {
			builder.append(", ");
			builder.append("definition=");
			builder.append(definition);
		}
		if (synonyms != null) {
			builder.append(", ");
			builder.append("synonyms=");
			builder.append(synonyms);
		}
		if (defXRef != null) {
			builder.append(", ");
			builder.append("defXRef=");
			builder.append(defXRef);
		}
		if (relations != null) {
			builder.append(", ");
			builder.append("relations=");
			builder.append(relations);
		}
		if (metaData != null) {
			builder.append(", ");
			builder.append("metaData=");
			builder.append(metaData.toString());
		}
		builder.append("]");
		return builder.toString();
	}

	public static JsonOntologyTerm convert(OntologyTerm<? extends ISynonym, ? extends IRelation> source) {
		JsonOntologyTerm term = new JsonOntologyTerm();
		term.setDefinition(source.getDefinition());
		term.setDefXRef(source.getDefXRef());
		term.setTempId(source.getId());
		term.setLabel(source.getLabel());
		term.setSynonyms(JsonSynonym.convert(source.getSynonyms()));
		term.setMetaData(source.getMetaData());
		List<? extends IRelation> relations = source.getRelations();
		if (relations != null && !relations.isEmpty()) {
			List<JsonTermRelation> jsonRelations = new ArrayList<JsonTermRelation>(relations.size());
			for (IRelation relation : relations) {
				jsonRelations.add(JsonTermRelation.convert(relation));
			}
			term.setRelations(jsonRelations);
		}
		return term;
	}
	
	public static class JsonSynonym implements ISynonym {

		private String label;
		private String scope;
		private String category;
		private Set<String> xrefs;

		/**
		 * @return the label
		 */
		@Override
		public String getLabel() {
			return label;
		}

		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * @return the scope
		 */
		@Override
		public String getScope() {
			return scope;
		}

		/**
		 * @param scope the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}

		/**
		 * @return the xrefs
		 */
		@Override
		public Set<String> getXrefs() {
			return xrefs;
		}

		/**
		 * @param xrefs the xrefs to set
		 */
		public void setXrefs(Set<String> xrefs) {
			this.xrefs = xrefs;
		}

		/**
		 * @return the category
		 */
		@Override
		public String getCategory() {
			return category;
		}

		/**
		 * @param category the category to set
		 */
		public void setCategory(String category) {
			this.category = category;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonSynonym [");
			if (label != null) {
				builder.append("label=");
				builder.append(label);
				builder.append(", ");
			}
			if (scope != null) {
				builder.append("scope=");
				builder.append(scope);
				builder.append(", ");
			}
			if (category != null) {
				builder.append("category=");
				builder.append(category);
				builder.append(", ");
			}
			if (xrefs != null) {
				builder.append("xrefs=");
				builder.append(xrefs);
			}
			builder.append("]");
			return builder.toString();
		}
		
		static List<JsonSynonym> convert(Collection<? extends ISynonym> synonyms) {
			if (synonyms != null && !synonyms.isEmpty()) {
				List<JsonSynonym> jsonSynonyms = new ArrayList<JsonSynonym>(synonyms.size());
				for (ISynonym synonym : synonyms) {
					if (synonym instanceof JsonSynonym) {
						jsonSynonyms.add((JsonSynonym) synonym);
					}else {
						JsonSynonym jsonSynonym = new JsonSynonym();
						jsonSynonym.setLabel(synonym.getLabel());
						jsonSynonym.setScope(synonym.getScope());
						jsonSynonym.setCategory(synonym.getCategory());
						jsonSynonym.setXrefs(synonym.getXrefs());
						jsonSynonyms.add(jsonSynonym);
					}
				}
				return jsonSynonyms;
			}
			return null;
		}
	}

	public static class JsonTermRelation implements IRelation {

		private String source;
		private String target;
		private String targetLabel;
		private Map<String, String> properties;

		public JsonTermRelation() {
			super();
		}

		/**
		 * @return the source
		 */
		@Override
		public String getSource() {
			return source;
		}

		/**
		 * @param source the source to set
		 */
		public void setSource(String source) {
			this.source = source;
		}

		/**
		 * @return the target
		 */
		@Override
		public String getTarget() {
			return target;
		}

		/**
		 * @param target the target to set
		 */
		public void setTarget(String target) {
			this.target = target;
		}
		
		/**
		 * @return the targetLabel
		 */
		@Override
		public String getTargetLabel() {
			return targetLabel;
		}

		/**
		 * @param targetLabel the targetLabel to set
		 */
		public void setTargetLabel(String targetLabel) {
			this.targetLabel = targetLabel;
		}

		/**
		 * @return the properties
		 */
		@Override
		public Map<String, String> getProperties() {
			return properties;
		}

		/**
		 * @param properties the properties to set
		 */
		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonTermRelation [");
			if (source != null) {
				builder.append("source=");
				builder.append(source);
				builder.append(", ");
			}
			if (target != null) {
				builder.append("target=");
				builder.append(target);
				builder.append(", ");
			}
			if (properties != null) {
				builder.append("properties=");
				builder.append(properties);
			}
			builder.append("]");
			return builder.toString();
		}
		
		static JsonTermRelation convert(IRelation relation) {
			if (relation instanceof JsonTermRelation) {
				return (JsonTermRelation) relation;
			}
			JsonTermRelation jsonRelation = new JsonTermRelation();
			jsonRelation.source = relation.getSource();
			jsonRelation.target = relation.getTarget();
			jsonRelation.targetLabel = relation.getTargetLabel();
			jsonRelation.properties = relation.getProperties();
			return jsonRelation;
		}
	}
}
