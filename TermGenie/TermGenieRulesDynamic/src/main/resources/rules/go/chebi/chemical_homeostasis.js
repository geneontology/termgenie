// @requires rules/common.js


function chemical_homeostasis() {
	var ont = GeneOntology;
	
	var x = getSingleTerm("target", ont);
	
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no subtemplate was selected");
		return;
	}
	var count = 0;
	var name = termname(x, ont);
	if (termgenie.contains(prefixes, "GO:0048878")) {
		var label = name + " homeostasis";
		var definition = "Any process involved in the maintenance of an internal steady state of "
				+ name + " within an organism or cell.";
		
		var synonyms = null;
		
		var mdef = createMDef("GO_0048878 and 'regulates level of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, "GO:0055082")) {
		var label = "cellular " + name + " homeostasis";
		var definition = "Any biological process involved in the maintenance of an internal steady state of "
			+ name + " at the level of the cell.";
		
		var synonyms = null;
		
		var mdef = createMDef("GO_0055082 and 'regulates level of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error("Could not create a term for X, as no known subtemplate was selected");
	}
}