package org.bbop.termgenie.services.visualization;

import java.util.List;

import javax.servlet.ServletContext;

import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonResult;
import org.json.rpc.server.ServletContextAware;

public interface TermHierarchyRenderer {

	/**
	 * Render a graph hierarchy for the given ontology and terms
	 * 
	 * @param ids
	 * @param ontology ontology name
	 * @param servletContext (do not send in request, will be added by server)
	 * @return {@link JsonResult} if successful the message contains the URL to
	 *         the image file, otherwise it contains an error message
	 */
	@ServletContextAware
	public JsonResult renderHierarchy(List<String> ids,
			String ontology,
			ServletContext servletContext);

	/**
	 * Render a graph hierarchy for the generated terms in the given ontology
	 * 
	 * @param generatedTerms
	 * @param ontology
	 * @param servletContext
	 * @return {@link JsonResult} if successful the message contains the URL to
	 *         the image file, otherwise it contains an error message
	 */
	@ServletContextAware
	public JsonResult visualizeGeneratedTerms(List<JsonOntologyTerm> generatedTerms,
			String ontology,
			ServletContext servletContext);
}
