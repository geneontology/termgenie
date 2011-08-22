package org.bbop.termgenie.data;

public class JsonTermGenerationInput {

	private JsonTermTemplate termTemplate;
	private JsonTermGenerationParameter termGenerationParameter;

	/**
	 * @param termTemplate
	 * @param termGenerationParameter
	 */
	public JsonTermGenerationInput(JsonTermTemplate termTemplate,
			JsonTermGenerationParameter termGenerationParameter)
	{
		this();
		this.termTemplate = termTemplate;
		this.termGenerationParameter = termGenerationParameter;
	}

	public JsonTermGenerationInput() {
		super();
	}

	/**
	 * @return the termTemplate
	 */
	public JsonTermTemplate getTermTemplate() {
		return termTemplate;
	}

	/**
	 * @param termTemplate the termTemplate to set
	 */
	public void setTermTemplate(JsonTermTemplate termTemplate) {
		this.termTemplate = termTemplate;
	}

	/**
	 * @return the termGenerationParameter
	 */
	public JsonTermGenerationParameter getTermGenerationParameter() {
		return termGenerationParameter;
	}

	/**
	 * @param termGenerationParameter the termGenerationParameter to set
	 */
	public void setTermGenerationParameter(JsonTermGenerationParameter termGenerationParameter) {
		this.termGenerationParameter = termGenerationParameter;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonTermGenerationInput [");
		if (termTemplate != null) {
			builder.append("termTemplate=");
			builder.append(termTemplate);
			builder.append(", ");
		}
		if (termGenerationParameter != null) {
			builder.append("termGenerationParameter=");
			builder.append(termGenerationParameter);
		}
		builder.append("]");
		return builder.toString();
	}
}
