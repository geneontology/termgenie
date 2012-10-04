// @requires rules/common.js

function chemical_transmembrane_transport_bp() {
	
	var ont = GeneOntology;
	
	// transport subject: chebi
	var x = getSingleTerm("subject", ont);
	
	var name = termname(x, ont);
	var label = name + " transmembrane transport";
	var definition = "The directed movement of "+name+" across a membrane.";
	var synonyms = null;
	var mdef = createMDef("GO:0055085 and 'transports or maintains localization of' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}