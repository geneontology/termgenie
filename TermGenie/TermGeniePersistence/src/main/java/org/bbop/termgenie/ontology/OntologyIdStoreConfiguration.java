package org.bbop.termgenie.ontology;

import java.util.Map;

import org.bbop.termgenie.ontology.entities.OntologyIdInfo;

/**
 * Configuration for an {@link OntologyIdStore}.
 */
public interface OntologyIdStoreConfiguration {

	/**
	 * Retrieve a map of configurations.
	 * 
	 * @return configuration map
	 */
	public Map<String, OntologyIdInfo> getInfos();
	
}
