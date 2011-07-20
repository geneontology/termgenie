package org.bbop.termgenie.core.io;

import java.util.List;

import org.bbop.termgenie.core.Ontology;

/**
 * Tool for the association of Ontologies to a string. 
 * To be used only for term templates.
 */
public interface TemplateOntologyHelper {

	/**
	 * Parse the serialized representation of ontologies.
	 * 
	 * @param serializedNames
	 * @return list of ontologies
	 */
	public List<Ontology> readOntologies(String serializedNames);
	
	/**
	 * Create the serialized representation for a list of ontologies.
	 * 
	 * @param ontologies
	 * @return String serialized representation
	 */
	public String serializeOntologies(List<Ontology> ontologies);
}
