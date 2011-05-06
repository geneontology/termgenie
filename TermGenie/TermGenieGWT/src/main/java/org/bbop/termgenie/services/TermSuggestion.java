package org.bbop.termgenie.services;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class TermSuggestion implements Suggestion, IsSerializable
{
	private String label;
	private String identifier;
	private String description;
	private String externalLink;
	
	public TermSuggestion() {
		super();
	}
	
	/**
	 * @param label
	 * @param identifier
	 * @param description
	 * @param externalLink
	 */
	public TermSuggestion(String label, String identifier, String description,
			String externalLink) {
		super();
		this.label = label;
		this.identifier = identifier;
		this.description = description;
		this.externalLink = externalLink;
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
	 * @return the externalLink
	 */
	public String getExternalLink() {
		return externalLink;
	}

	/**
	 * @param externalLink the externalLink to set
	 */
	public void setExternalLink(String externalLink) {
		this.externalLink = externalLink;
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
		if (externalLink != null) {
			sb.append("<td><b>External Link</b></td>");
		}
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td>").append(label).append("</td>");
		sb.append("<td>").append(identifier).append("</td>");
		if (description != null) {
			sb.append("<td>").append(description).append("</td>");
		}
		if (externalLink != null) {
			sb.append("<td><a href=\"").append(externalLink).append("\">link</a></td>");
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