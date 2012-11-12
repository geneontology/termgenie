// @requires rules/common.js

function chemical_response_to() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var name = termname(x, ont);
	var label = "response to " + name;
	var definition = "Any process that results in a change in state or activity of a cell or an organism (in terms of movement, secretion, enzyme production, gene expression, etc.) as a result of "
			+ refname(x, ont)
			+ " stimulus.";
	var synonyms = null;
	var mdef = createMDef("GO_0050896 and 'has input' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}