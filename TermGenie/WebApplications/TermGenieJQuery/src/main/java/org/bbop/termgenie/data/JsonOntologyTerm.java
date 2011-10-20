package org.bbop.termgenie.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class JsonOntologyTerm {

	private String tempId;
	private String label;
	private String definition;
	private JsonSynonym[] synonyms;
	private String[] defxRef;
	private JsonTermRelation[] relations;
	private JsonTermMetaData metaData;

	public JsonOntologyTerm() {
		super();
	}

	/**
	 * @return the tempId
	 */
	public String getTempId() {
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
	public String[] getDefxRef() {
		return defxRef;
	}

	/**
	 * @param defxRef the defxRef to set
	 */
	public void setDefxRef(String[] defxRef) {
		this.defxRef = defxRef;
	}

	/**
	 * @return the relations
	 */
	public JsonTermRelation[] getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(JsonTermRelation[] relations) {
		this.relations = relations;
	}

	/**
	 * @return the synonyms
	 */
	public JsonSynonym[] getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(JsonSynonym[] synonyms) {
		this.synonyms = synonyms;
	}

	/**
	 * @return the metaData
	 */
	public JsonTermMetaData getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(JsonTermMetaData metaData) {
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
			builder.append(Arrays.toString(synonyms));
		}
		if (defxRef != null) {
			builder.append(", ");
			builder.append("defxRef=");
			builder.append(Arrays.toString(defxRef));
		}
		if (relations != null) {
			builder.append(", ");
			builder.append("relations=");
			builder.append(Arrays.toString(relations));
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
		List<String> defXRef = source.getDefXRef();
		if (defXRef != null && !defXRef.isEmpty()) {
			term.setDefxRef(defXRef.toArray(new String[defXRef.size()]));
		}
		term.setTempId(source.getId());
		term.setLabel(source.getLabel());
		term.setSynonyms(JsonSynonym.convert(source.getSynonyms()));
		term.setMetaData(new JsonTermMetaData(source.getMetaData()));
		List<? extends IRelation> relations = source.getRelations();
		if (relations != null && !relations.isEmpty()) {
			JsonTermRelation[] jsonRelations = new JsonTermRelation[relations.size()];
			for (int i = 0; i < jsonRelations.length; i++) {
				jsonRelations[i] = JsonTermRelation.convert(relations.get(i));
			}
			term.setRelations(jsonRelations);
		}
		return term;
	}
	
	public static class JsonSynonym {

		private String label;
		private String scope;
		private String category;
		private String[] xrefs;

		/**
		 * @return the label
		 */
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
		public String[] getXrefs() {
			return xrefs;
		}

		/**
		 * @param xrefs the xrefs to set
		 */
		public void setXrefs(String[] xrefs) {
			this.xrefs = xrefs;
		}

		/**
		 * @return the category
		 */
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
				builder.append(Arrays.toString(xrefs));
			}
			builder.append("]");
			return builder.toString();
		}
		
		public static JsonSynonym[] convert(Collection<? extends ISynonym> synonyms) {
			if (synonyms != null && !synonyms.isEmpty()) {
				List<JsonSynonym> jsonSynonyms = new ArrayList<JsonSynonym>(synonyms.size());
				for (ISynonym synonym : synonyms) {
					JsonSynonym jsonSynonym = new JsonSynonym();
					jsonSynonym.setLabel(synonym.getLabel());
					jsonSynonym.setScope(synonym.getScope());
					jsonSynonym.setCategory(synonym.getCategory());
					String[] axrefs = null;
					Set<String> xrefs = synonym.getXrefs();
					if (xrefs != null && !xrefs.isEmpty()) {
						axrefs = xrefs.toArray(new String[xrefs.size()]);
					}
					jsonSynonym.setXrefs(axrefs);
					jsonSynonyms.add(jsonSynonym);
				}
				return jsonSynonyms.toArray(new JsonSynonym[jsonSynonyms.size()]);
			}
			return null;
		}
	}

	public static class JsonTermRelation {

		private String source;
		private String target;
		private String targetLabel;
		private String[][] properties;

		public JsonTermRelation() {
			super();
		}

		/**
		 * @return the source
		 */
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
		public String[][] getProperties() {
			return properties;
		}

		/**
		 * @param properties the properties to set
		 */
		public void setProperties(String[][] properties) {
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
				builder.append(Arrays.toString(properties));
			}
			builder.append("]");
			return builder.toString();
		}
		
		public static IRelation convert(JsonTermRelation jsonRelation) {
			final Map<String, String> properties = new HashMap<String, String>();
			
			String[][] strings = jsonRelation.getProperties();
			if (strings != null) {
				for (int i = 0; i < strings.length; i++) {
					String[] pair = strings[i];
					if (pair != null && pair.length == 2) {
						properties.put(pair[0], pair[1]);
					}
				}
			}
			return new Relation(jsonRelation.source, jsonRelation.target, jsonRelation.targetLabel, properties);
		}
		
		public static JsonTermRelation convert(IRelation relation) {
			JsonTermRelation jsonRelation = new JsonTermRelation();
			jsonRelation.source = relation.getSource();
			jsonRelation.target = relation.getTarget();
			jsonRelation.targetLabel = relation.getTargetLabel();
			Map<String, String> properties = relation.getProperties();
			if (properties != null && !properties.isEmpty()) {
				String[][] strings = new String[properties.size()][];
				int counter = 0;
				for (Entry<String, String> entry : properties.entrySet()) {
					strings[counter] = new String[]{entry.getKey(), entry.getValue()};
					counter += 1;
				}
				jsonRelation.properties = strings;
			}
			return jsonRelation;
		}
	}

	public static class JsonTermMetaData {

		private String created_by;
		private String creation_date;
		private String resource;
		private String comment;

		/**
		 * Default constructor
		 */
		public JsonTermMetaData() {
			super();
		}

		public JsonTermMetaData(Map<String, String> metaData) {
			this();
			this.created_by = metaData.get("created_by");
			this.creation_date = metaData.get("creation_date");
			this.resource = metaData.get("resource");
			this.comment = metaData.get("comment");
		}
		
		public static Map<String, String> getMap(JsonTermMetaData metaData) {
			Map<String, String> map = new HashMap<String, String>();
			setValue(map, "created_by", metaData.getCreated_by());
			setValue(map, "creation_date", metaData.getCreation_date());
			setValue(map, "resource", metaData.getResource());
			setValue(map, "comment", metaData.getComment());
			return map;
		}
		
		private static void setValue(Map<String, String> map, String key, String value) {
			if (value != null) {
				map.put(key, value);
			}
		}

		/**
		 * @return the created_by
		 */
		public String getCreated_by() {
			return created_by;
		}

		/**
		 * @param created_by the created_by to set
		 */
		public void setCreated_by(String created_by) {
			this.created_by = created_by;
		}

		/**
		 * @return the creation_date
		 */
		public String getCreation_date() {
			return creation_date;
		}

		/**
		 * @param creation_date the creation_date to set
		 */
		public void setCreation_date(String creation_date) {
			this.creation_date = creation_date;
		}

		/**
		 * @return the resource
		 */
		public String getResource() {
			return resource;
		}

		/**
		 * @param resource the resource to set
		 */
		public void setResource(String resource) {
			this.resource = resource;
		}

		/**
		 * @return the comment
		 */
		public String getComment() {
			return comment;
		}

		/**
		 * @param comment the comment to set
		 */
		public void setComment(String comment) {
			this.comment = comment;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonTermMetaData [");
			if (created_by != null) {
				builder.append("created_by=");
				builder.append(created_by);
				builder.append(", ");
			}
			if (creation_date != null) {
				builder.append("creation_date=");
				builder.append(creation_date);
				builder.append(", ");
			}
			if (comment != null) {
				builder.append("comment=");
				builder.append(comment);
				builder.append(", ");
			}
			if (resource != null) {
				builder.append("resource=");
				builder.append(resource);
			}
			builder.append("]");
			return builder.toString();
		}
	}
}
