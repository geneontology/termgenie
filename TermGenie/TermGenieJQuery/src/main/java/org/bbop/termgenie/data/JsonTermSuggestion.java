package org.bbop.termgenie.data;

import java.util.Arrays;

import org.bbop.termgenie.data.JsonTermGenerationParameter.JsonOntologyTermIdentifier;

public class JsonTermSuggestion
{
	private String label;
	private JsonOntologyTermIdentifier identifier;
	private String description;
	private String[] synonyms;
	
	public JsonTermSuggestion() {
		super();
	}
	
	/**
	 * @param label
	 * @param identifier
	 * @param description
	 * @param synonyms
	 */
	public JsonTermSuggestion(String label, JsonOntologyTermIdentifier identifier, String description,
			String[] synonyms) {
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
			builder.append(Arrays.toString(synonyms));
		}
		builder.append("}");
		return builder.toString();
	}

//	public String getDisplayString() {
//		StringBuilder sb = new StringBuilder();
//		
//		sb.append("<table cellspacing=\"4\">");
//		sb.append("<tr>");
//		sb.append("<td><b>Name</b></td>");
//		sb.append("<td><b>Identifier</b></td>");
//		if (description != null) {
//			sb.append("<td><b>Description</b></td>");
//		}
//		if (synonyms != null && !synonyms.isEmpty()) {
//			sb.append("<td><b>Synonyms</b></td>");
//		}
//		sb.append("</tr>");
//		sb.append("<tr>");
//		sb.append("<td>").append(label).append("</td>");
//		sb.append("<td>").append(identifier.getTermId()).append("</td>");
//		if (description != null) {
//			sb.append("<td>").append(description).append("</td>");
//		}
//		if (synonyms != null && !synonyms.isEmpty()) {
//			sb.append("<td><a href=\"");
//			boolean first = true;
//			for(String synonym : synonyms) {
//				if (first) {
//					first = false;
//				}
//				else {
//					sb.append(", ");
//				}
//				sb.append(synonym);
//			}
//			sb.append("\">link</a></td>");
//		}
//		sb.append("</tr>");
//		sb.append("</table>");
//		return sb.toString();
//	}
//	
//	public String getReplacementString() {
//		return label;
//	}
}