package org.bbop.termgenie.ontology;

import java.net.URL;

import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

public interface IRIMapper extends OWLOntologyIRIMapper {

	/**
	 * Map an url to a new url. Can be used to redirect loads to a local copy of
	 * a resource.
	 * 
	 * @param url
	 * @return URL
	 */
	public URL mapUrl(String url);

}
