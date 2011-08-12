package org.bbop.termgenie.ontology;

import java.net.URL;

public interface IRIMapper {

	/**
	 * Map an url to a new url. Can be used to redirect loads to a local copy of
	 * a resource.
	 * 
	 * @param url
	 * @return URL
	 */
	public URL mapUrl(String url);

}
