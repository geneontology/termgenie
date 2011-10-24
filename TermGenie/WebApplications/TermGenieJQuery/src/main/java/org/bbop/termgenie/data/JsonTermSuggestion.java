package org.bbop.termgenie.data;

import java.util.List;

import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;

public class JsonTermSuggestion {

	private String label;
	private JsonOntologyTermIdentifier identifier;
	private String description;
	private List<String> synonyms;

	public JsonTermSuggestion() {
		super();
	}

	/**
	 * @param label
	 * @param identifier
	 * @param description
	 * @param synonyms
	 */
	public JsonTermSuggestion(String label,
			JsonOntologyTermIdentifier identifier,
			String description,
			List<String> synonyms)
	{
		super();
		this.label = label;
		this.identifier = identifier;
		this.description = description;
		this.synonyms = synonyms;
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
	 * @return the identifier
	 */
	public JsonOntologyTermIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(JsonOntologyTermIdentifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the synonyms
	 */
	public List<String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonTermSuggestion:{");
		if (label != null) {
			builder.append("label:");
			builder.append(label);
			builder.append(", ");
		}
		if (identifier != null) {
			builder.append("identifier:");
			builder.append(identifier);
			builder.append(", ");
		}
		if (description != null) {
			builder.append("description:\"");
			builder.append(description);
			builder.append("\", ");
		}
		if (synonyms != null) {
			builder.append("synonyms:");
			builder.append(synonyms);
		}
		builder.append("}");
		return builder.toString();
	}
}
