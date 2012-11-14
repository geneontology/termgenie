// @requires rules/common.js

function chemical_response_to() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no subtemplate was selected");
		return;
	}
	var count = 0;
	var name = termname(x, ont);
	if (termgenie.contains(prefixes, "GO:0050896")) {
		var label = "response to " + name;
		var definition = "Any process that results in a change in state or activity of a cell or an organism (in terms of movement, secretion, enzyme production, gene expression, etc.) as a result of "
				+ refname(x, ont)
				+ " stimulus.";
		var synonyms = null;
		var mdef = createMDef("GO_0050896 and 'has input' some ?X");
		mdef.addParameter('X', x, ont);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, "GO:0070887")) {
		var label = "cellular response to " + name;
		var definition = "Any process that results in a change in state or activity of a cell (in terms of movement, secretion, enzyme production, gene expression, etc.) as a result of "
				+ refname(x, ont)
				+ " stimulus.";
		var synonyms = null;
		var mdef = createMDef("GO_0070887 and 'has input' some ?X");
		mdef.addParameter('X', x, ont);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error("Could not create a term for X, as no known subtemplate was selected");
	}
}