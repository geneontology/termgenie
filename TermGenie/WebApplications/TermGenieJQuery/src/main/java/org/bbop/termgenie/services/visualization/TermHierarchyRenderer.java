package org.bbop.termgenie.services.visualization;

import java.util.List;

import javax.servlet.ServletContext;

import org.bbop.termgenie.data.JsonResult;
import org.json.rpc.server.ServletContextAware;


public interface TermHierarchyRenderer {

	/**
	 * Render a graph hierarchy for the given ontology
	 * 
	 * @param ids
	 * @param ontology ontology name
	 * @param servletContext (do not send in request, will be added by server)
	 * @return location (file or url) to the image file.
	 */
	@ServletContextAware
	public JsonResult renderHierarchy(List<String> ids, String ontology, ServletContext servletContext);
}
