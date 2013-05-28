// @requires rules/common.js

function x_morphogenesis(ontology, parent) {
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " morphogenesis";
	var definition = "The developmental process by which " + refname(x, ontology)
			+ " is generated and organized.";
	var synonyms = termgenie.synonyms(null, x, ontology, " morphogenesis", null, label);
	var mdef = createMDef("'anatomical structure morphogenesis' and 'results in morphogenesis of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
