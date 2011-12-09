// @requires rules/common.js

function chemical_binding() {
	var x = getSingleTerm("target", CHEBI);
	var label = termname(x, CHEBI) + " binding";
	var definition = "Interacting selectively and non-covalently with "
			+ termname(x, CHEBI) + ".";
	var synonyms = null; // No synonyms
	var mdef = createMDef("GO_0005488 and 'has_input' some ?X");
	mdef.addParameter('X', x, CHEBI);
	createTerm(label, definition, synonyms, mdef);
}
