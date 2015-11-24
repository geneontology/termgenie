// @requires rules/common.js

function eq_simple() {
	var e = getSingleTerm("entity", TO);
	var q = getSingleTerm("quality", TO);

	var label = termname(e, TO) + ' ' + termname(q, TO);
	var definition = "A trait which is associated with the "
			+ termname(q, TO) + " of " + refname(e, TO) + ".";

	var synonyms = termgenie.synonyms(null, e, TO, " ", q, TO, "", null, label);
	
	var mdef = createMDef("?Q and 'inheres in' some ?E");
	mdef.addParameter('E', e, TO);
	mdef.addParameter('Q', q, TO);
	createTerm(label, definition, synonyms, mdef);
}
