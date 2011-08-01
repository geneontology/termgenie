package org.bbop.termgenie.ontology.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OntologyIdInfo {

	@Id
	private String ontologyName;
	private String pattern;
	private int current;
	private int maximum;

	/**
	 * @return the ontologyName
	 */
	public String getOntologyName() {
		return ontologyName;
	}

	/**
	 * @param ontologyName the ontologyName to set
	 */
	public void setOntologyName(String ontologyName) {
		this.ontologyName = ontologyName;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return the current
	 */
	public int getCurrent() {
		return current;
	}

	/**
	 * @param current the current to set
	 */
	public void setCurrent(int current) {
		this.current = current;
	}

	/**
	 * @return the maximum
	 */
	public int getMaximum() {
		return maximum;
	}

	/**
	 * @param maximum the maximum to set
	 */
	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}
}
