// @requires rules/common.js

function protein_localization_to() {
	var go = GeneOntology;
	var c = getSingleTerm("component", go);
	var check = checkGenus(c, "GO:0005575", go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var prefixes = getInputs("component");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no prefix was selected");
		return;
	}
	var termnamec = termname(c, go);
	var count = 0;

	if (termgenie.contains(prefixes, "protein localization")) {
		
		var label = "protein localization to " + termnamec;
		var definition = "A process in which a protein is transported to, or maintained in, a location within "+refname(c, go)+".";
		
		// BE spelling
		var synonyms = termgenie.addSynonym(label, null, 'protein localisation to ', '', termnamec, 'EXACT');
		
		// 'in' variation
		synonyms = termgenie.addSynonym(label, synonyms, 'protein localization in ', '', termnamec, 'EXACT');
		
		//BE spelling for 'in' variation
		synonyms = termgenie.addSynonym(label, synonyms, 'protein localisation in ', '', termnamec, 'EXACT');
		var mdef = createMDef("GO_0008104 and 'has target end location' some ?C");
		mdef.addParameter('C', c, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, "establishment of protein localization")) {
		var label = "establishment of protein localization to " + termnamec;
		var definition = "The directed movement of a protein to a specific location in "+refname(c, go)+".";
		
		// BE spelling
		var synonyms = termgenie.addSynonym(label, null, 'establishment of protein localisation to ', '', termnamec, 'EXACT');
		
		// 'in' variation
		synonyms = termgenie.addSynonym(label, synonyms, 'establishment of protein localization in ', '', termnamec, 'EXACT');
		
		//BE spelling for 'in' variation
		synonyms = termgenie.addSynonym(label, synonyms, 'establishment of protein localisation in ', '', termnamec, 'EXACT');
		var mdef = createMDef("GO_0045184 and 'has target end location' some ?C");
		mdef.addParameter('C', c, go);
		
		var partOfExpression = createMDef("GO_0008104 and 'has target end location' some ?C");
		partOfExpression.addParameter('C', c, go);
		
		var success = termgenie.createTerm(label, definition, synonyms, [mdef], [partOfExpression]);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	if (count === 0) {
		error("Could not create a term for X, as no known prefix was selected");
	}
}
