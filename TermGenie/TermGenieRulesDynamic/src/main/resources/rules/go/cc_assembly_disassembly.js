// @requires rules/common.js

function cc_assembly_disassembly() {
	var go = GeneOntology;
	var c = getSingleTerm("component", go);
	
	var prefixes = getInputs("component");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as neither assembly or disassembly was selected");
		return;
	}
	
	var check = checkGenus(c, "GO:0005575", go); // cellular_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var name = termname(c, go);
	var count = 0;
	if (termgenie.contains(prefixes, "assembly")) {
		var label = name + " assembly";
		var definition = "The aggregation, arrangement and bonding together of a set of components to form the "
			+ name + "."; 
	
		var synonyms = termgenie.addSynonym(label, null, '', name, ' formation', 'EXACT');
		var mdef = createMDef("GO_0022607 and 'results_in_assembly_of' some ?C");
		mdef.addParameter('C', c, go);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	if (termgenie.contains(prefixes, "disassembly")) {
		var label = name + " disassembly";
		var definition = "The disaggregation of "+ refname(c, go) + 
			" into its constituent components."; 
	
		var synonyms = null;
		var mdef = createMDef("GO_0022411 and 'results_in_disassembly_of' some ?C");
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
		error("Could not create a term for X, as neither assembly or disassembly was selected");
	}
}