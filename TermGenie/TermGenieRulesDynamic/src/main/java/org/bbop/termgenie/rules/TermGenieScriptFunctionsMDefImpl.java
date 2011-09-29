package org.bbop.termgenie.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsMDef.MDef;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;


public class TermGenieScriptFunctionsMDefImpl extends AbstractTermGenieScriptFunctionsImpl<List<MDef>> implements
		TermGenieScriptFunctionsMDef
{

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	TermGenieScriptFunctionsMDefImpl(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, tempIdPrefix, patternID, factory);
	}

	
	@Override
	protected AbstractTermCreationTools<List<MDef>> createTermCreationTool(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		return new TermCreationToolsMDef(input, targetOntology, tempIdPrefix, patternID, factory);
	}
	
	private class MDefImpl implements MDef {
		
		private final String expression;
		private final Map<String, String> parameters;
		
		/**
		 * @param expression
		 */
		private MDefImpl(String expression) {
			super();
			this.expression = expression;
			this.parameters = Collections.synchronizedMap(new HashMap<String, String>());
		}

		@Override
		public void addParameter(String name, String value) {
			parameters.put(name, value);
		}

		@Override
		public void addParameter(String name, OWLObject x, OWLGraphWrapper ontology) {
			parameters.put(name, ontology.getIdentifier(x));
		}

		@Override
		public void addParameter(String name, OWLObject x, OWLGraphWrapper[] ontologies) {
			for (OWLGraphWrapper ontology : ontologies) {
				String identifier = ontology.getIdentifier(x);
				if (identifier != null) {
					parameters.put(name, identifier);
					break;
				}
			}
		}

		@Override
		public String getExpression() {
			return expression;
		}
		
		@Override
		public Map<String, String> getParameters() {
			return Collections.unmodifiableMap(parameters);
		}
	}
	
	@Override
	public MDef createMDef(String string) {
		return new MDefImpl(string);
	}

	@Override
	public void createTerm(String label,
			String definition,
			List<Synonym> synonyms,
			MDef logicalDefinition)
	{
		tools.addTerm(label, definition, synonyms, Collections.singletonList(logicalDefinition), getResultList());
	}

	@Override
	public void createTerm(String label,
			String definition,
			List<Synonym> synonyms,
			MDef[] logicalDefinitions)
	{
		List<MDef> logicalDefinitionList = Arrays.asList(logicalDefinitions);
		tools.addTerm(label, definition, synonyms, logicalDefinitionList, getResultList());
	}

}
