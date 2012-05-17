// @requires rules/common.js

function chemical_transport() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var label = termname(x, ont) + " transport";
	var definition = "The directed movement of "
			+ refname(x, ont)
			+ " into, out of or within a cell, or between cells, by means of some agent such as a transporter or pore.";
	var synonyms = null; // No synonyms
	var mdef = createMDef("GO_0006810 and 'results in transport of' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}
