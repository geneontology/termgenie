// @requires rules/common.js

function regulation_triad(x, parent) {
	var go = GeneOntology;
	var check = checkGenus(x, parent, go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	if (containsClassInEquivalenceAxioms(x, getEquivalentClasses("GO:0065007", go), go)) {
		// biological regulation
		error("Cannot create 'regulation of regulation of X' terms. The term "
				+ getTermShortInfo("GO:0065007", go) + " is a used in the definition of "
				+ getTermShortInfo(x, go) + ".");
		return;
	}
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no prefix was selected");
		return;
	}
	var count = 0;
	if (termgenie.contains(prefixes, "regulation")) {
		var label = "regulation of " + termname(x, go);
		var definition = "Any process that modulates the frequency, rate or extent of "
				+ termname(x, go) + ".";
		var synonyms = termgenie.synonyms("regulation of ", x, go, null, null, label);
		var mdef = createMDef("GO_0065007 and 'regulates' some ?X");
		mdef.addParameter('X', x, go);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "negative_regulation")) {
		var label = "negative regulation of " + termname(x, go);
		var definition = "Any process that stops, prevents or reduces the frequency, rate or extent of "
				+ termname(x, go) + ".";
		var synonyms = termgenie.synonyms([ "negative regulation of ",
				"down regulation of ", "down-regulation of ", "downregulation of ", "inhibition of " ],
				["EXACT", "EXACT", "EXACT", "EXACT", "NARROW" ],
				x, go, [], label);
		var mdef = createMDef("GO_0065007 and 'negatively_regulates' some ?X");
		mdef.addParameter('X', x, go);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (termgenie.contains(prefixes, "positive_regulation")) {
		var label = "positive regulation of " + termname(x, go);
		var definition = "Any process that activates or increases the frequency, rate or extent of "
				+ termname(x, go) + ".";
		var synonyms = termgenie.synonyms([ "positive regulation of ",
				"up regulation of ", "up-regulation of ", "upregulation of ", "activation of " ],
				["EXACT", "EXACT", "EXACT", "EXACT", "NARROW" ],
				x, go, [], label);
		var mdef = createMDef("GO_0065007 and 'positively_regulates' some ?X");
		mdef.addParameter('X', x, go);
		createTerm(label, definition, synonyms, mdef);
		count += 1;
	}
	if (count === 0) {
		error("Could not create a term for X, as no known prefix was selected");
	}
}
