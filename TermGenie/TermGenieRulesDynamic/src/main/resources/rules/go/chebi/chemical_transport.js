// @requires rules/common.js

function chemical_transport() {
	var x = getSingleTerm("target", CHEBI);
	var label = termname(x, CHEBI) + " transport";
	var definition = "The directed movement of "
			+ refname(x, CHEBI)
			+ " acetate into, out of or within a cell, or between cells, by means of some agent such as a transporter or pore.";
	var synonyms = null; // No synonyms
	var mdef = createMDef("GO_0006810 and 'results_in_transport_of' some ?X");
	mdef.addParameter('X', x, CHEBI);
	createTerm(label, definition, synonyms, mdef);
}
