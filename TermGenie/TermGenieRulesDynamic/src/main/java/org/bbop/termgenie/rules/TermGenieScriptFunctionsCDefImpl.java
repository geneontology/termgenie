package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsCDef.CDef;
import org.bbop.termgenie.tools.Pair;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * Complete implementation of functions for the TermGenie scripting environment.
 * Uses CDef to generate relations.
 */
public class TermGenieScriptFunctionsCDefImpl extends AbstractTermGenieScriptFunctionsImpl<CDef> implements TermGenieScriptFunctionsCDef {
	
	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	TermGenieScriptFunctionsCDefImpl(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, tempIdPrefix, patternID, factory);
	}

	@Override
	protected AbstractTermCreationTools<CDef> createTermCreationTool(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		return new TermCreationToolsCDef(input, targetOntology, tempIdPrefix, patternID, factory);
	}

	private static class CDefImpl implements CDef {

		final OWLObject genus;
		final OWLGraphWrapper ontology;

		final List<Differentium> differentia = new ArrayList<Differentium>();

		final List<String> properties = new ArrayList<String>();

		/**
		 * @param genus
		 * @param ontology
		 */
		protected CDefImpl(OWLObject genus, OWLGraphWrapper ontology) {
			super();
			this.genus = genus;
			this.ontology = ontology;
		}

		@Override
		public void differentium(String rel, OWLObject term, OWLGraphWrapper[] ontologies) {
			differentium(rel, Collections.singletonList(term), Arrays.asList(ontologies));
		}

		@Override
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper[] ontologies) {
			differentium(rel, Arrays.asList(terms), Arrays.asList(ontologies));
		}

		private void differentium(String rel,
				List<OWLObject> terms,
				List<OWLGraphWrapper> ontologies)
		{
			differentia.add(new Differentium(rel, terms, ontologies));
		}

		@Override
		public void differentium(String rel, OWLObject term, OWLGraphWrapper ontology) {
			differentium(rel, new OWLObject[] { term }, new OWLGraphWrapper[] { ontology });
		}

		@Override
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper ontology) {
			differentium(rel, terms, new OWLGraphWrapper[] { ontology });
		}

		@Override
		public void property(String property) {
			properties.add(property);
		}

		@Override
		public Pair<OWLObject, OWLGraphWrapper> getBase() {
			return new Pair<OWLObject, OWLGraphWrapper>(genus, ontology);
		}

		@Override
		public List<String> getProperties() {
			return Collections.unmodifiableList(properties);
		}

		@Override
		public List<Differentium> getDifferentia() {
			return Collections.unmodifiableList(differentia);
		}
	}

	@Override
	public CDef cdef(OWLObject genus) {
		return cdef(genus, tools.targetOntology);
	}

	@Override
	public CDef cdef(String id) {
		return cdef(id, tools.targetOntology);
	}

	@Override
	public CDef cdef(OWLObject genus, OWLGraphWrapper ontology) {
		return new CDefImpl(genus, ontology);
	}

	@Override
	public CDef cdef(String id, OWLGraphWrapper ontology) {
		OWLObject genus = ontology.getOWLObjectByIdentifier(id);
		return cdef(genus);
	}

	@Override
	public void createTerm(String label,
			String definition,
			List<Synonym> synonyms,
			CDef logicalDefinition)
	{
		tools.addTerm(label, definition, synonyms, logicalDefinition, getResultList());
	}
	
}
