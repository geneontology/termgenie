package org.bbop.termgenie.services;

import org.bbop.termgenie.data.JsonTermSuggestion;

public interface OntologyService {

	/**
	 * Check the current ontology name and ontology state of the loaded
	 * ontology. Will only return true, if all ontologies have been loaded and
	 * no unsatisfiable classes are present.
	 * 
	 * @return status message
	 */
	public JsonOntologyStatus getOntologyStatus();
	
	/**
	 * Return message for {@link OntologyService#getOntologyStatus()}
	 */
	public static class JsonOntologyStatus {
		public String ontology;
		public boolean okay;
		public String[] messages;
		
		/**
		 * @return the ontology
		 */
		public String getOntology() {
			return ontology;
		}

		/**
		 * @param ontology the ontology to set
		 */
		public void setOntology(String ontology) {
			this.ontology = ontology;
		}

		/**
		 * @return the okay
		 */
		public boolean isOkay() {
			return okay;
		}
		
		/**
		 * @param okay the okay to set
		 */
		public void setOkay(boolean okay) {
			this.okay = okay;
		}
		
		/**
		 * @return the messages
		 */
		public String[] getMessages() {
			return messages;
		}
		
		/**
		 * @param messages the messages to set
		 */
		public void setMessages(String[] messages) {
			this.messages = messages;
		}
	}
	
	/**
	 * Auto complete the query with terms in the specified ontology. Return
	 * only max number of results.
	 * 
	 * @param sessionId an id which can be used to retrieve the session object.
	 * @param query
	 * @param ontology
	 * @param max
	 * @return term suggestions
	 */
	public JsonTermSuggestion[] autocomplete(String sessionId,
			String query,
			String ontology,
			int max);
}
