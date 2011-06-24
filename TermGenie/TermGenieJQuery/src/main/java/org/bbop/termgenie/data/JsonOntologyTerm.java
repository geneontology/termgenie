package org.bbop.termgenie.data;

import java.util.Arrays;

public class JsonOntologyTerm {
	
	private String id;
	private String label;
	private String definition;
	private String logDef;
	private String comment;
	private String[] synonyms; 
	private String[] defxRef;
	private JsonTermRelation[] relations;
	
	public JsonOntologyTerm() {
		super();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the logDef
	 */
	public String getLogDef() {
		return logDef;
	}

	/**
	 * @param logDef the logDef to set
	 */
	public void setLogDef(String logDef) {
		this.logDef = logDef;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonOntologyTerm [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
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
		if (logDef != null) {
			builder.append("logDef=");
			builder.append(logDef);
			builder.append(", ");
		}
		if (comment != null) {
			builder.append("comment=");
			builder.append(comment);
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
}