// @requires rules/common.js

function chemical_import() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var x = getSingleTerm("target", ont);
	var name = termname(x, ont);
	var label = name + " import";
	var definition = "The directed movement of "
			+ name
			+ " into a cell or organelle.";
	var synonyms = null;
	var mdef = createMDef("GO_0006810 and 'imports' some ?X");
	mdef.addParameter('X', x, ont);
	createTerm(label, definition, synonyms, mdef);
}