// @requires rules/common.js

function chemical_transporter_activity() {
	var ont = GeneOntology;
	
	var x = getSingleTerm("target", ont);
	var name = termname(x, ont);
	var label = name + " transporter activity";
	var definition = "Enables the directed movement of "
			+ name
			+ " into, out of or within a cell, or between cells.";
	var synonyms = null;
	var mdef = createMDef("GO:0005215 and 'transports or maintains localization of' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}
