// @requires rules/common.js

function metabolism_catabolism_biosynthesis() {
	var x = getSingleTerm("target", CHEBI);
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no subtemplate was selected");
		return;
	}
	var count = 0;
	if (termgenie.contains(prefixes, "metabolism")) {
		var label = termname(x, CHEBI) + " metabolic process";
		var definition = "The chemical reactions and pathways involving "
				+ termname(x, CHEBI) + ".";
		var synonyms = termgenie.synonyms("", x, CHEBI, [ " metabolism",
				" metabolic process" ], null, label);
		var mdef = createMDef("GO_0008152 and 'has_participant' some ?X");
		mdef.addParameter('X', x, CHEBI);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "catabolism")) {
		var label = termname(x, CHEBI) + " catabolic process";
		var definition = "The chemical reactions and pathways resulting in the breakdown of "
				+ termname(x, CHEBI) + ".";
		var synonyms = termgenie.synonyms("", x, CHEBI, [ " catabolism",
				" catabolic process" ], null, label);
		var mdef = createMDef("GO_0009056 and 'has_input' some ?X");
		mdef.addParameter('X', x, CHEBI);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "biosynthesis")) {
		var label = termname(x, CHEBI) + " biosynthetic process";
		var definition = "The chemical reactions and pathways resulting in the formation of "
				+ termname(x, CHEBI) + ".";
		var synonyms = termgenie.synonyms("", x, CHEBI, [ " biosynthesis",
				" biosynthetic process" ], null, label);
		var mdef = createMDef("GO_0009058 and 'has_output' some ?X");
		mdef.addParameter('X', x, CHEBI);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (count === 0) {
		error("Could not create a term for X, as no known subtemplate was selected");
	}
}
