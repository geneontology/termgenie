// @requires rules/common.js

function cc_transport() {
	var go = GeneOntology;
	var c = getSingleTerm("component", go);
	
	var prefixes = getInputs("component");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no transport type was selected");
		return;
	}
	
	var check = checkGenus(c, "GO:0005575", go); // cellular_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var count = 0;
	var name = termname(c, go);
	// simple transport
	if (termgenie.contains(prefixes, "transport")) {
  
		var label = name + " transport";
		var definition = "The directed movement of " + name 
						+ " into, out of or within a cell, or between cells, "
						+ "by means of some agent such as a transporter or pore.";
		
		var synonyms = termgenie.synonyms(null, c, go, " transport", null, label);
		var mdef = createMDef("GO_0006810 and 'transports or maintains localization of' some ?C");
		mdef.addParameter('C', c, go);
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
		var label = name + " vesicle-mediated transport";
		var definition = "The directed movement of " + name 
				+ " into, out of or within a cell, or between cells, mediated by small transport vesicles.";
		
		var synonyms = termgenie.synonyms(null, c, go, " vesicle-mediated transport", null, label);
		var mdef = createMDef("GO_0016192 and 'transports or maintains localization of' some ?C");
		mdef.addParameter('C', c, go);
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