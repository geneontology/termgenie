// @requires rules/common.js

function chemical_transport_from_to() {
	var go = GeneOntology;
	
	// required
	var chemical = getSingleTerm("chemical", go)
	
	var prefixes = getInputs("chemical");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no transport type was selected");
		return;
	}
	
	var ccCount = 0;
	var from = getSingleTerm("from", go);
	var hasFrom = from && from !== null;
	
	if (hasFrom) {
		var check = checkGenus(from, "GO:0005575", go); // celluar_component
		if (check.isGenus() !== true) {
			error(check.error());
			return;
		}
		ccCount += 1;
	}
	
	var to = getSingleTerm("to", go);
	var hasTo = to && to !== null;
	
	if (hasTo) {
		check = checkGenus(to, "GO:0005575", go); // cellular_component
		if (check.isGenus() !== true) {
			error(check.error());
			return;
		}
		ccCount += 1;
	}
	
	// okay, we require at least one cellular component
	if (ccCount === 0) {
		error("Could not create a term, at least one cellular location ('from' or 'to') is required.");
		return;
	}
  
	var termCount = 0;
	var chemicalName = termname(chemical, go);
	
	var fromName = null;
	if (hasFrom) {
		fromName = termname(from, go);
	}
	
	var toName = null;
	if (hasTo) {
		toName = termname(to, go);
	}
  
	// simple transport
	if (termgenie.contains(prefixes, "transport")) {
		var label = chemicalName + " transport";
		var definition = "The directed movement of "+chemicalName;
		var mdefString = "GO_0006810 and 'transports or maintains localization of' some ?X";
		
		if (hasFrom) {
			label += " from " + fromName;
			definition += " from " + fromName;
			mdefString += " and 'has target start location' some ?F";
		}
		if (hasTo) {
			label += " to " + toName;
			definition += " to " + toName;
			mdefString += " and 'has target end location' some ?T";
		}
		definition += ".";

		var synonyms = null; // no synonyms
		
		var mdef = createMDef(mdefString);
		mdef.addParameter('X', chemical, go);
		
		if (hasFrom) {
			mdef.addParameter('F', from, go);
		}
		if (hasTo) {
			mdef.addParameter('T', to, go);
		}
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			termCount += 1;
		}
		else {
			return;
		}
	}
  
	// vesicle-mediated transport
	if (termgenie.contains(prefixes, "vesicle-mediated transport")) {
		var label = "vesicle-mediated " + chemicalName + " transport";
		var definition = "The vesicle-mediated and directed movement of "+chemicalName;
		var mdefString = "GO_0016192 and 'transports or maintains localization of' some ?X";
		
		if (hasFrom) {
			label += " from " + fromName;
			definition += " from " + fromName;
			mdefString += " and 'has target start location' some ?F";
		}
		if (hasTo) {
			label += " to " + toName;
			definition += " to " + toName;
			mdefString += " and 'has target end location' some ?T";
		}
		definition += ".";

		var synonyms = null; // no synonyms
		
		var mdef = createMDef(mdefString);
		mdef.addParameter('X', chemical, go);
		
		if (hasFrom) {
			mdef.addParameter('F', from, go);
		}
		if (hasTo) {
			mdef.addParameter('T', to, go);
		}
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			termCount += 1;
		}
		else {
			return;
		}
	}
	if (termCount === 0) {
		error("Could not create a term for X, as no known transport type was selected");
	}
	
}