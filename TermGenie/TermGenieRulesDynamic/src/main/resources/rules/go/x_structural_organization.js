// @requires rules/common.js

function x_structural_organization(ontology, parent) {
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " structural organization";
	var definition = "The process that contributes to the act of creating the structural organization of the " + name(x, ontology)
			+ ". This process pertains to the physical shaping of a rudimentary structure.";
	var synonyms = termgenie.synonyms(null, x, ontology, "  structural organization", null, label);
	var mdef = createMDef("'anatomical structure arrangement' and 'results in structural organization of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
