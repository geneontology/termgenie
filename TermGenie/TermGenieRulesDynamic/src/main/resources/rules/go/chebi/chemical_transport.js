// @requires rules/common.js

function chemical_transport() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var name = termname(x, ont);
	var label = name + " transport";
	var definition = "The directed movement of "
			+ refname(x, ont)
			+ " into, out of or within a cell, or between cells, by means of some agent such as a transporter or pore.";
//	var synonyms = termgenie.synonyms(null, x, ont, " transport", null, label);
	var synonyms = null;
	var mdef = createMDef("GO_0006810 and 'transports or maintains localization of' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}
