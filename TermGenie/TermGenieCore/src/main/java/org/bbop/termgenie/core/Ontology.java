package org.bbop.termgenie.core;

import java.util.List;
import java.util.Map;

/**
 * Wrapper of an ontology, provides additional methods for identifying the
 * ontology.
 */
public class Ontology {

	protected String name;
	protected String subOntologyName;
	protected List<String> roots;
	protected String dlQuery;
	protected Map<String, String> importRewrites;

	/**
	 * @param name
	 * @param subOntologyName
	 * @param roots
	 */
	protected Ontology(String name, String subOntologyName, List<String> roots, Map<String, String> importRewrites) {
		super();
		this.name = name;
		this.subOntologyName = subOntologyName;
		this.roots = roots;
		this.importRewrites = importRewrites;
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

	public Map<String, String> getImportRewrites() {
		return importRewrites;
	}

}
