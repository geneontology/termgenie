package org.bbop.termgenie.services;

import java.util.Set;

import org.bbop.termgenie.shared.GWTTermGenerationParameter.GWTOntologyTerm;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class TermSuggestion implements Suggestion, IsSerializable
{
	private String label;
	private GWTOntologyTerm identifier;
	private String description;
	private Set<String> synonyms;
	
	public TermSuggestion() {
		super();
	}
	
	/**
	 * @param label
	 * @param identifier
	 * @param description
	 * @param synonyms
	 */
	public TermSuggestion(String label, GWTOntologyTerm identifier, String description,
			Set<String> synonyms) {
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
	public GWTOntologyTerm getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(GWTOntologyTerm identifier) {
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
	public Set<String> getSynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(Set<String> synonyms) {
		this.synonyms = synonyms;
	}

	@Override
	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<table cellspacing=\"4\">");
		sb.append("<tr>");
		sb.append("<td><b>Name</b></td>");
		sb.append("<td><b>Identifier</b></td>");
		if (description != null) {
			sb.append("<td><b>Description</b></td>");
		}
		if (synonyms != null & !synonyms.isEmpty()) {
			sb.append("<td><b>Synonyms</b></td>");
		}
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td>").append(label).append("</td>");
		sb.append("<td>").append(identifier.getTermId()).append("</td>");
		if (description != null) {
			sb.append("<td>").append(description).append("</td>");
		}
		if (synonyms != null && !synonyms.isEmpty()) {
			sb.append("<td><a href=\"");
			boolean first = true;
			for(String synonym : synonyms) {
				if (first) {
					first = false;
				}
				else {
					sb.append(", ");
				}
				sb.append(synonym);
			}
			sb.append("\">link</a></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	@Override
	public String getReplacementString() {
		return label;
	}
}