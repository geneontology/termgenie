// @requires rules/common.js

function regulation_by() {
	var go = GeneOntology;
	var target = getSingleTerm("target", go);
	var source = getSingleTerm("source", go);
	
	// check input
	// check source
	var check = checkGenus(source, "GO:0008150", go); // check source is_a 'biological_process'
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	// check target
	if (containsClassInEquivalenceAxioms(target, getEquivalentClasses("GO:0065007", go), go)) {
		// biological regulation
		error("The target class can't be a regulation class. The class "
				+ getTermShortInfo("GO:0065007", go) + " is used in the definition of "
				+ getTermShortInfo(target, go) + ".");
		return;
	}

	// check target label does *not* contain 'regulation' substring
	var targetName = termname(target, go);
	if (targetName.contains("regulation")) {
		error("The target class can't be a regulation class.");
		return;
	}

	var label = 'regulation of ' + targetName + ' by ' + termname(source, go);
	var definition = refname(source, go) + ' that results in regulation of ' + targetName + '.';
	definition = termgenie.firstToUpperCase(definition);
	var synonyms = termgenie.synonyms('regulation of ', target, go, ' by ', source, go, null, null, label);
	var mdef = createMDef("?S and 'regulates' some ?T");
	mdef.addParameter('T', target, go);
	mdef.addParameter('S', source, go);
	var success = createTerm(label, definition, synonyms, mdef);
	return;
}
