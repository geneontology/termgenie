package org.bbop.termgenie.core;

import java.util.List;

/**
 * Wrapper of an ontology, provides additional methods for identifying the
 * ontology.
 */
public class Ontology {

	protected String name;
	protected String subOntologyName;
	protected List<String> roots;
	protected String dlQuery;

	/**
	 * @param name
	 * @param subOntologyName
	 * @param roots
	 */
	protected Ontology(String name, String subOntologyName, List<String> roots) {
		super();
		this.name = name;
		this.subOntologyName = subOntologyName;
		this.roots = roots;
	}

	public String getUniqueName() {
		return name;
	}

	public String getBranch() {
		return subOntologyName;
	}

	public List<String> getRoots() {
		return roots;
	}
	
	public String getDLQuery() {
		return dlQuery;
	}

	protected void setBranch(String subOntologyName, List<String> roots) {
		this.subOntologyName = subOntologyName;
		this.roots = roots;
	}
	
	protected void setBranch(String subOntologyName, String dlQuery) {
		this.subOntologyName = subOntologyName;
		this.dlQuery = dlQuery;
	}
}
