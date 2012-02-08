// @requires rules/common.js

function metazoan_development() {
	
	var ontology = GeneOntology;
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, "UBERON:0001062", ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " development";
	var definition = "The process whose specific outcome is the progression of "
			+ refname(x, ontology)
			+ " over time, from its formation to the mature structure.";
	var synonyms = termgenie.synonyms(null, x, ontology, " development", null, label);
	var mdef = createMDef("GO_0032502 and 'results in development of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
