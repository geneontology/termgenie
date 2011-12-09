// @requires rules/common.js

function x_development(ontology, parent) {
	var go = GeneOntology;
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " development";
	var definition = "The process whose specific outcome is the progression of "
			+ refname(x, ontology)
			+ " over time, from its formation to the mature structure.";
	var synonyms = termgenie.synonyms(null, x, ontology, " development", label);
	var mdef = createMDef("GO_0032502 and 'results_in_complete_development_of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
