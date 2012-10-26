package org.bbop.termgenie.freeform;

import java.util.List;

import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Fields expected for a free form request.
 */
public interface FreeFormTermRequest {

	/**
	 * @return the label
	 */
	public String getLabel();

	/**
	 * @return the namespace
	 */
	public String getNamespace();

	/**
	 * @return the definition
	 */
	public String getDefinition();

	/**
	 * @return the dbxrefs
	 */
	public List<String> getDbxrefs();

	/**
	 * @return the isA
	 */
	public List<String> getIsA();

	/**
	 * @return the partOf
	 */
	public List<String> getPartOf();

	/**
	 * @return the synonyms
	 */
	public List<? extends ISynonym> getISynonyms();

}
