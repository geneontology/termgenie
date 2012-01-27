// @requires rules/common.js

function biosynthesis_from() {
	var x = getSingleTerm("target", CHEBI);
	var label = termname(x, CHEBI) + " biosynthetic process";
	var definition = "The chemical reactions and pathways resulting in the formation of "
			+ termname(x, CHEBI) + ".";
	var synonyms = termgenie.synonyms("", x, CHEBI, 
			[ " biosynthesis", " biosynthetic process" ], null, label);
	var mdef = createMDef("GO_0009058 and 'has_output' some ?X");
	mdef.addParameter('X', x, CHEBI);
	createTerm(label, definition, synonyms, mdef);
}
