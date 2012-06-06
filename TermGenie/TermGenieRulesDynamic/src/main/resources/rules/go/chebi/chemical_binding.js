// @requires rules/common.js

function chemical_binding() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var name = termname(x, ont);
	var label = name + " binding";
	var definition = "Interacting selectively and non-covalently with "
			+ name + ".";
//	var synonyms = termgenie.synonyms(null, x, ont, " binding", null, label);
	var synonyms = null;
	
	var mdef = createMDef("GO_0005488 and 'has input' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}
