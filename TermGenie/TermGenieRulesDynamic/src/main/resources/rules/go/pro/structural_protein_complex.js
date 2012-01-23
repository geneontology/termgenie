// @requires rules/common.js

function structural_protein_complex() {
	var go = GeneOntology;
	var pro = ProteinOntology;
	var terms = getTerms("unit", pro);
	var label = "";
	var mdefString = 'GO_0043234';
	for ( var i = 0; i < terms.length; i += 1) {
		var check = checkGenus(terms[i], "PR:000000001", pro);
		if (check.isGenus() !== true) {
			error(check.error());
			return;
		}
		if (i > 0) {
			label += "-"
		}
		label += termname(terms[i], pro);
		mdefString += " and ('has_part' some ?P" + i + ' )';
	}
	label += " complex";
	var definition = termgenie.definition("Any protein complex consisting of ",
			terms, pro, ", ", ".");
	var synonyms = termgenie.synonyms(null, terms, pro, "-", " complex", label);
	var mdef = createMDef(mdefString);
	for (i = 0; i < terms.length; i += 1) {
		mdef.addParameter('P' + i, terms[i], pro);
	}
	createTerm(label, definition, synonyms, mdef);
}
