package org.bbop.termgenie.rules;

import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsMDef.MDef;

import owltools.graph.OWLGraphWrapper;


public class TermCreationToolsMDef extends AbstractTermCreationTools<List<MDef>>  {

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	TermCreationToolsMDef(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, tempIdPrefix, patternID, factory);
	}

	@Override
	protected List<IRelation> createRelations(List<MDef> logicalDefinitions,
			String newId,
			OWLChangeTracker changeTracker)
	{
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}
}
