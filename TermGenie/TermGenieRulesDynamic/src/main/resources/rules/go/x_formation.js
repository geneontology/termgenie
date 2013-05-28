// @requires rules/common.js

function x_formation(ontology, parent) {
	var x = getSingleTerm("target", ontology);
	var check = checkGenus(x, parent, ontology);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, ontology) + " formation";
	var definition = "The process that gives rise to the " + name(x, ontology)
			+ ". This process pertains to the initial formation of a structure from unspecified parts.";
	var synonyms = termgenie.synonyms(null, x, ontology, " formation", null, label);
	var mdef = createMDef("'anatomical structure formation involved in morphogenesis' and 'results in formation of' some ?X");
	mdef.addParameter('X', x, ontology);
	createTerm(label, definition, synonyms, mdef);
}
