// @requires rules/common.js

function regulation_by() {
	var go = GeneOntology;
	var target = getSingleTerm("target", go);
	var relations = getInputs("target");
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

	// check provided relation
	if (!relations || relations === null || relations.length === 0) {
		error("Could not create a class for X, as no relation was selected");
		return;
	}
	if (relations.length > 1) {
		error("Please select exactly one relation.");
		return;
	}
	
	// now actually create the class
	var count = 0;
	if (termgenie.contains(relations, 'regulation')) {
		var label = 'regulation of ' + targetName + ' by ' + termname(source, go);
		var definition = refname(source, go) + ' that results in regulation of ' + targetName + '.';
		definition = termgenie.firstToUpperCase(definition);
		var synonyms = termgenie.synonyms('regulation of ', target, go, ' by ', source, go, null, null, label);
		var mdef = createMDef("?S and 'regulates' some ?T");
		mdef.addParameter('T', target, go);
		mdef.addParameter('S', source, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(relations, 'negative_regulation')) {
		var label = 'negative regulation of ' + targetName + ' by ' + termname(source, go);
		var definition = refname(source, go) + ' that results in negative regulation of ' + targetName + '.';
		definition = termgenie.firstToUpperCase(definition);
		var synonyms = termgenie.synonyms('negative regulation of ', target, go, ' by ', source, go, null, null, label);
		var mdef = createMDef("?S and 'negatively regulates' some ?T");
		mdef.addParameter('T', target, go);
		mdef.addParameter('S', source, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(relations, 'positive_regulation')) {
		var label = 'positive regulation of ' + targetName + ' by ' + termname(source, go);
		var definition = refname(source, go) + ' that results in positive regulation of ' + targetName + '.';
		definition = termgenie.firstToUpperCase(definition);
		var synonyms = termgenie.synonyms('positive regulation of ', target, go, ' by ', source, go, null, null, label);
		var mdef = createMDef("?S and 'positively regulates' some ?T");
		mdef.addParameter('T', target, go);
		mdef.addParameter('S', source, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error("Could not create a class: An unknown relation was selected");
	}
	return;
	
//	function subrun(regulator) {
//		var processName = termname(p, go);
//
//		// special rule to reduce the number of proposed prefixes for the term
//		// more details see: http://wiki.geneontology.org/index.php/Ontology_meeting_2012-03-21
//		var requiredPrefixLeft = checkRequiredPrefix(processName);
//		var ignoreSynonymsRight = genus(regulator, 'GO:0050789', go); // if regulator is_a 'regulation of biological process', ignore synonyms 
//
//		var label = processName + " by " + termname(regulator, go);
//		var definition = refname(regulator, go) + ' that results in ' + processName + '.';
//		definition = 'A' + definition.substr(1);
//		var synonyms = termgenie.synonyms(null, p, go, " by ", regulator, go, null, null, label, requiredPrefixLeft, ignoreSynonymsRight);
//
//		var mdef = createMDef("?R and 'regulates' some ?P");
//		mdef.addParameter('P', p, go);
//		mdef.addParameter('R', regulator, go);
//		termgenie.createTerm(label, definition, synonyms, mdef);
//	};
//
//	function checkRequiredPrefix(s) {
//		var prefix = null;
//		if (s.indexOf('regulation of ') === 0) {
//			prefix = 'regulation of ';
//		}
//		else if (s.indexOf('negative regulation of ') === 0) {
//			prefix = 'negative regulation of ';
//		}
//		else if (s.indexOf('regulation of ') === 0) {
//			prefix = 'positive regulation of ';
//		}
//		return prefix;
//	}
}
