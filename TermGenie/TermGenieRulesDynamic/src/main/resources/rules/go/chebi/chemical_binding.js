// @requires rules/common.js

function chemical_binding() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var label = termname(x, ont) + " binding";
	var definition = "Interacting selectively and non-covalently with "
			+ termname(x, ont) + ".";
	var synonyms = termgenie.synonyms(null, x, ont, " binding", null, label);
	var mdef = createMDef("GO_0005488 and 'has input' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}
