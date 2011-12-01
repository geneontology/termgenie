package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

public interface TermGenieScriptFunctionsMDef extends TermGenieScriptFunctions {

	/**
	 * Create a new MDef instance for the given definition in Manchester OWLs
	 * syntax.
	 * 
	 * @param string
	 * @return {@link MDef}
	 */
	public MDef createMDef(String string);

	/**
	 * Data container for OWL expression in Manchester OWL syntax.
	 */
	public static interface MDef {

		/**
		 * Bind the given parameter name with the given String value.
		 * 
		 * @param name parameter name
		 * @param value parameter value
		 */
		public void addParameter(String name, String value);

		/**
		 * Bind the given parameter name with the given {@link OWLObject}.
		 * 
		 * @param name parameter name
		 * @param x {@link OWLObject} to be bound as parameter value
		 * @param ontology corresponding ontology for the {@link OWLObject}
		 */
		public void addParameter(String name, OWLObject x, OWLGraphWrapper ontology);

		/**
		 * Bind the given parameter name with the given {@link OWLObject}.
		 * 
		 * @param name parameter name
		 * @param x {@link OWLObject} to be bound as parameter value
		 * @param ontologies corresponding ontologies for the {@link OWLObject}
		 */
		public void addParameter(String name, OWLObject x, OWLGraphWrapper[] ontologies);

		public String getExpression();

		public Map<String, String> getParameters();
	}

	/**
	 * Create a new term and provide output which can directly be returned.
	 * 
	 * @param label
	 * @param definition
	 * @param synonyms
	 * @param logicalDefinition
	 */
	public void createTerm(String label,
			String definition,
			List<ISynonym> synonyms,
			MDef logicalDefinition);

	/**
	 * Create a new term and provide output which can directly be returned.
	 * 
	 * @param label
	 * @param definition
	 * @param synonyms
	 * @param logicalDefinitions
	 */
	public void createTerm(String label,
			String definition,
			List<ISynonym> synonyms,
			MDef[] logicalDefinitions);

}
