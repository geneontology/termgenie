// @requires rules/common.js

function protein_binding() {
	var go = GeneOntology;
	var pro = ProteinOntology;
	var x = getSingleTerm("target", pro);
	var check = checkGenus(x, "PR:000000001", pro);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(x, pro) + " binding";
	var definition = "Interacting selectively and non-covalently with  "
			+ termname(x, pro) + ".";
	var synonyms = termgenie.synonyms(null, x, pro, " binding", label);
	var mdef = createMDef("GO_0005488 and 'results_in_binding_of' some ?X");
	mdef.addParameter('X', x, pro);
	createTerm(label, definition, synonyms, mdef);
}
