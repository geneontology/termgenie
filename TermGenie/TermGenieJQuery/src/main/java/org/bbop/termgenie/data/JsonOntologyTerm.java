package org.bbop.termgenie.data;

import java.util.Arrays;
import java.util.Map;

public class JsonOntologyTerm {
	
	private String label;
	private String definition;
	private String[] synonyms; 
	private String[] defxRef;
	private JsonTermRelation[] relations;
	private JsonTermMetaData metaData;
	
	public JsonOntologyTerm() {
		super();
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
	public String[] getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(String[] synonyms) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonOntologyTerm [");
		if (label != null) {
			builder.append("label=");
			builder.append(label);
			builder.append(", ");
		}
		if (definition != null) {
			builder.append("definition=");
			builder.append(definition);
			builder.append(", ");
		}
		if (synonyms != null) {
			builder.append("synonyms=");
			builder.append(Arrays.toString(synonyms));
			builder.append(", ");
		}
		if (defxRef != null) {
			builder.append("defxRef=");
			builder.append(Arrays.toString(defxRef));
			builder.append(", ");
		}
		if (relations != null) {
			builder.append("relations=");
			builder.append(Arrays.toString(relations));
			builder.append(", ");
		}
		if (metaData != null) {
			builder.append("metaData=");
			builder.append(metaData.toString());
		}
		builder.append("]");
		return builder.toString();
	}

	public static class JsonTermRelation {
		private String source;
		private String target;
		private String[] properties;
		
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
		 * @return the properties
		 */
		public String[] getProperties() {
			return properties;
		}

		/**
		 * @param properties the properties to set
		 */
		public void setProperties(String[] properties) {
			this.properties = properties;
		}

		/* (non-Javadoc)
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
		/* (non-Javadoc)
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