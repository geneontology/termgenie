// @requires rules/common.js

function cc_transport_from_to() {
	var go = GeneOntology;
	var from = getSingleTerm("from", go);
	var prefixes = getInputs("from");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no transport type was selected");
		return;
	}
	var to = getSingleTerm("to", go);
	var check = checkGenus(from, "GO:0005575", go); // celluar_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	check = checkGenus(to, "GO:0005575", go); // cellular_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var count = 0;
	var fromName = termname(from, go);
	var toName = termname(to, go);
  
	// simple transport
	if (termgenie.contains(prefixes, "transport")) {
		var label = fromName + " to " + toName + " transport";

		var definition = "The directed movement of substances from " 
						+ fromName + " to " + toName + ".";
		var synonyms = termgenie.synonyms(null, from, go, " to ", to, go, " transport", null, label);
		var mdef = createMDef("GO_0006810 and 'has target start location' some ?F and 'has target end location' some ?T");
		mdef.addParameter('F', from, go);
		mdef.addParameter('T', to, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
  
	// vesicle-mediated transport
	if (termgenie.contains(prefixes, "vesicle-mediated transport")) {
		var label = fromName + " to " + toName + " vesicle-mediated transport";

		var definition = "The vesicle-mediated and directed movement of substances from " 
						+ fromName + " to " + toName + ".";
		var synonyms = termgenie.synonyms(null, from, go, " to ", to, go, " vesicle-mediated transport", null, label);
		var mdef = createMDef("GO_0016192 and 'has target start location' some ?F and 'has target end location' some ?T");
		mdef.addParameter('F', from, go);
		mdef.addParameter('T', to, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error("Could not create a term for X, as no known transport type was selected");
	}
}