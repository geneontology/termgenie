// @requires rules/common.js

function x_morphogenesis(ontology, parent) {
	var go = GeneOntology;
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " morphogenesis";
	var definition = "The developmental process by which " + refname(x, ontology)
			+ " is generated and organized.";
	var synonyms = termgenie.synonyms(null, x, ontology, " morphogenesis", label);
	var mdef = createMDef("GO_0009653 and 'results_in_morphogenesis_of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
