package org.bbop.termgenie.rules;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public class CellOntologyPatterns extends Patterns {

	private final OWLGraphWrapper cell;
	private final OWLGraphWrapper uberon;
	private final OWLGraphWrapper pro;
	private final OWLGraphWrapper go;

	private final OWLObject GO0032991;

	protected CellOntologyPatterns(List<OWLGraphWrapper> wrappers,
			DefaultTermTemplates templates,
			ReasonerFactory factory)
	{
		super(factory, templates.metazoan_location_specific_cell, templates.cell_by_surface_marker);
		this.cell = wrappers.get(0);
		this.uberon = wrappers.get(1);
		this.pro = wrappers.get(2);
		this.go = wrappers.get(3);
		GO0032991 = getTerm("GO:0032991", go);
	}

	protected OWLObject getTerm(String id, OWLGraphWrapper ontology) {
		OWLObject term = getTermSimple(id, ontology);
		if (term == null) {
			throw new RuntimeException("No term found for id: " + id);
		}
		return term;
	}

	@ToMatch
	protected List<TermGenerationOutput> metazoan_location_specific_cell(TermGenerationInput input,
			Map<String, OntologyTerm> pending)
	{
		OWLObject c = getSingleTerm(input, "cell", cell);
		OWLObject a = getSingleTerm(input, "location", uberon);
		if (c == null || a == null) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String label = createName(name(a, uberon) + " " + name(c, cell), input);
		String definition = createDefinition("Any " + name(c, cell) + " that is part of a " + name(a,
				uberon) + ".",
				input);
		List<Synonym> synonyms = null; // TODO
		CDef cdef = new CDef(c, cell);
		cdef.addDifferentium("part_of", a, uberon);
		return createTermList(label, definition, synonyms, cdef, input, cell);
	}

	@ToMatch
	protected List<TermGenerationOutput> cell_by_surface_marker(TermGenerationInput input,
			Map<String, OntologyTerm> pending)
	{
		OWLObject c = getSingleTerm(input, "cell", cell);
		OWLObject p = getSingleTerm(input, "membrane_part", pro, go);
		if (c == null || p == null || !(genus(p, GO0032991, go) || genus(p, null, pro))) {
			// check branch
			return error("The specified terms do not correspond to the pattern", input);
		}
		String label = createName(name(p, pro, go) + " " + name(c, cell), input);
		String definition = createDefinition("Any " + name(c, cell) + " that has " + name(p,
				pro,
				go) + " on the plasma membrane.",
				input);
		List<Synonym> synonyms = null; // TODO
		CDef cdef = new CDef(c, cell);
		cdef.addDifferentium("has_plasma_membrane_part", p, pro, go);
		cdef.addProperty("multivalued(has_plasma_membrane_part)");
		cdef.addProperty("any_cardinality(has_plasma_membrane_part)");
		return createTermList(label, definition, synonyms, cdef, input, cell);
	}
}
