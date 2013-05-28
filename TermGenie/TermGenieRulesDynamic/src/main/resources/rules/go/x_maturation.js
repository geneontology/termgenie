// @requires rules/common.js

function x_maturation(ontology, parent, createSynoyms) {
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " maturation";
	var definition = "A developmental process, independent of morphogenetic (shape) change, that is required for the "
			+ termname(x, ontology)
			+ " to attain its fully functional state.";
	
	var synonyms;
	if (createSynoyms === false) {
		synonyms = null;
	}
	else {
		synonyms = termgenie.synonyms(null, x, ontology, " maturation", null, label);
	}
	var mdef = createMDef("'developmental maturation' and 'results in developmental progression of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
