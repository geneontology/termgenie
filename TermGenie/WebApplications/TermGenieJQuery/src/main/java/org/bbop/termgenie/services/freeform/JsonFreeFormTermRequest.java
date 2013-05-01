package org.bbop.termgenie.services.freeform;

import java.util.List;

import org.bbop.termgenie.data.JsonOntologyTerm.JsonSynonym;
import org.bbop.termgenie.freeform.FreeFormTermRequest;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class JsonFreeFormTermRequest implements FreeFormTermRequest {

	private String label;
	private String namespace;

	private String definition;
	private List<String> dbxrefs;

	private List<String> isA;
	private List<String> partOf;

	private List<JsonSynonym> synonyms;
	
	private List<Xref> xrefs;

	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	@Override
	public List<String> getDbxrefs() {
		return dbxrefs;
	}

	/**
	 * @param dbxrefs the dbxrefs to set
	 */
	public void setDbxrefs(List<String> dbxrefs) {
		this.dbxrefs = dbxrefs;
	}

	@Override
	public List<String> getIsA() {
		return isA;
	}

	/**
	 * @param isA the isA to set
	 */
	public void setIsA(List<String> isA) {
		this.isA = isA;
	}

	@Override
	public List<String> getPartOf() {
		return partOf;
	}

	/**
	 * @param partOf the partOf to set
	 */
	public void setPartOf(List<String> partOf) {
		this.partOf = partOf;
	}

	/**
	 * @return the synonyms
	 */
	public List<JsonSynonym> getSynonyms() {
		return synonyms;
	}
	
	@Override
	public List<? extends ISynonym> getISynonyms() {
		return synonyms;
	}

	/**
	 * @param synonyms the synonyms to set
	 */
	public void setSynonyms(List<JsonSynonym> synonyms) {
		this.synonyms = synonyms;
	}
	
	/**
	 * @return the xrefs
	 */
	@Override
	public List<Xref> getXrefs() {
		return xrefs;
	}
	
	/**
	 * @param xrefs the xrefs to set
	 */
	public void setXrefs(List<Xref> xrefs) {
		this.xrefs = xrefs;
	}

}
