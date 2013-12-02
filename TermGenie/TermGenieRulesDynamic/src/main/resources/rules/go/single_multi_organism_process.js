// @requires rules/common.js

function single_multi_organism_process() {
	var go = GeneOntology;
	var p = getSingleTerm("process", go);
	
	var prefixes = getInputs("process");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as neither single-organism nor multi-organism was selected");
		return;
	}
	
	var check = checkGenus(p, "GO:0008150", go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var count = 0;
	var name = termname(p, go);
	var cRefName = capitalizeFirstLetter(termgenie.refname(p, go));
	// single-organism process
	if (termgenie.contains(prefixes, "single-organism")) {
		var label = "single-organism " + name;
		var definition = cRefName+" which involves only one organism.";
		var synonyms = termgenie.addSynonym(label, null, "single organism ", name, "", "EXACT");
		var mdef = createMDef("?P and 'bearer_of' some PATO_0002487");
		mdef.addParameter('P', p, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	// multi-organism process
	if (termgenie.contains(prefixes, "multi-organism")) {
		var label = "multi-organism " + name;
		var definition = cRefName + " which involves another organism.";
		var synonyms = termgenie.addSynonym(label, null, "multi organism ", name, "", "EXACT");
		var mdef = createMDef("?P and 'bearer_of' some PATO_0002486");
		mdef.addParameter('P', p, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
  
	if (count === 0) {
		error("Could not create a term for X, as neither single-organism nor multi-organism was selected");
	}
	
	
	
	function capitalizeFirstLetter(string)
	{
	    return string.substring(0,1).toUpperCase() + string.substring(1);
	}
}