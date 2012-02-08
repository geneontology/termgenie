// @requires rules/common.js

function metabolism_catabolism_biosynthesis() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var x = getSingleTerm("target", ont);
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no subtemplate was selected");
		return;
	}
	var count = 0;
	if (termgenie.contains(prefixes, "metabolism")) {
		var label = termname(x, ont) + " metabolic process";
		var definition = "The chemical reactions and pathways involving "
				+ termname(x, ont) + ".";
		var synonyms = termgenie.synonyms("", x, ont, [ " metabolism",
				" metabolic process" ], null, label);
		var mdef = createMDef("GO_0008152 and 'has participant' some ?X");
		mdef.addParameter('X', x, ont);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "catabolism")) {
		var label = termname(x, ont) + " catabolic process";
		var definition = "The chemical reactions and pathways resulting in the breakdown of "
				+ termname(x, ont) + ".";
		var synonyms = termgenie.synonyms("", x, ont, [ " catabolism",
				" catabolic process" ], null, label);
		var mdef = createMDef("GO_0009056 and 'has input' some ?X");
		mdef.addParameter('X', x, ont);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "biosynthesis")) {
		var label = termname(x, ont) + " biosynthetic process";
		var definition = "The chemical reactions and pathways resulting in the formation of "
				+ termname(x, ont) + ".";
		var synonyms = termgenie.synonyms("", x, ont, [ " biosynthesis",
				" biosynthetic process" ], null, label);
		var mdef = createMDef("GO_0009058 and 'has output' some ?X");
		mdef.addParameter('X', x, ont);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (count === 0) {
		error("Could not create a term for X, as no known subtemplate was selected");
	}
}
