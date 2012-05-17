package org.bbop.termgenie.core;

import java.util.List;

public class TermSuggestion {

	private String label;
	private String identifier;
	private String description;
	private List<String> synonyms;

	public TermSuggestion() {
		super();
	}

	/**
	 * @param label
	 * @param identifier
	 * @param description
	 * @param synonyms
	 */
	public TermSuggestion(String label,
			String identifier,
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
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TermSuggestion [");
		if (identifier != null) {
			builder.append("identifier=");
			builder.append(identifier);
		}
		if (label != null) {
			builder.append(", ");
			builder.append("label=");
			builder.append(label);
		}
		if (description != null) {
			builder.append(", ");
			builder.append("description=");
			builder.append(description);
		}
		if (synonyms != null) {
			builder.append(", ");
			builder.append("synonyms=");
			builder.append(synonyms);
		}
		builder.append("]");
		return builder.toString();
	}

}
