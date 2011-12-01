package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsMDef.MDef;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;


public class TermGenieScriptFunctionsMDefImpl extends AbstractTermGenieScriptFunctionsImpl<List<MDef>> implements
		TermGenieScriptFunctionsMDef
{
	private ManchesterSyntaxTool syntaxTool;

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 * @param auxiliaryOntologies 
	 */
	TermGenieScriptFunctionsMDefImpl(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, auxiliaryOntologies, tempIdPrefix, patternID, factory);
	}

	
	@Override
	protected AbstractTermCreationTools<List<MDef>> createTermCreationTool(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		List<OWLOntology> ontologies = new ArrayList<OWLOntology>(auxiliaryOntologies.size());
		for (OWLGraphWrapper wrapper : auxiliaryOntologies) {
			ontologies.add(wrapper.getSourceOntology());
		}
		syntaxTool = new ManchesterSyntaxTool(targetOntology.getSourceOntology(), ontologies);
		return new TermCreationToolsMDef(input, targetOntology, tempIdPrefix, patternID, factory, syntaxTool);
	}
	
	class MDefImpl implements MDef {
		
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
			if (x instanceof OWLEntity) {
				parameters.put(name, syntaxTool.getId((OWLEntity) x));
			}
		}
		
		@Override
		public void addParameter(String name, OWLObject x, OWLGraphWrapper[] ontologies) {
			// find corresponding ontology via label.
			for (OWLGraphWrapper ontology : ontologies) {
				String label = ontology.getLabel(x);
				if (label != null) {
					addParameter(name, x, ontology);
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
			List<ISynonym> synonyms,
			MDef logicalDefinition)
	{
		try {
			List<MDef> defs;
			if (logicalDefinition != null) {
				defs = Collections.singletonList(logicalDefinition);
			}
			else {
				defs = Collections.emptyList();
			}
			tools.addTerm(label, definition, synonyms, defs, getResultList());
		} catch (NullPointerException exception) {
			Logger.getLogger(getClass()).error("NPE", exception);
			throw exception;
		}
	}

	@Override
	public void createTerm(String label,
			String definition,
			List<ISynonym> synonyms,
			MDef[] logicalDefinitions)
	{
		try {
			List<MDef> logicalDefinitionList;
			if (logicalDefinitions != null && logicalDefinitions.length > 0) {
				logicalDefinitionList = Arrays.asList(logicalDefinitions);
			}
			else {
				logicalDefinitionList = Collections.emptyList();
			}
			tools.addTerm(label, definition, synonyms, logicalDefinitionList, getResultList());
		} catch (NullPointerException exception) {
			Logger.getLogger(getClass()).error("NPE", exception);
			throw exception;
		}
	}

}
