// @requires rules/common.js

function eq_simple() {
	var e = getSingleTerm("entity", TO);
	var q = getSingleTerm("quality", TO);

	var label = termname(e, TO) + ' ' + termname(q, TO);
	var definition = "Any measurable or observable characteristic related to the "
			+ termname(q, TO) + " of " + refname(e, TO) + ".";

	var synonyms = null;
	// String label, List<ISynonym> results, String prefix, String infix, String suffix, String scope
	synonyms = termgenie.addSynonym(label, synonyms, null, label, ' trait', 'EXACT');
	
	var mdef = createMDef("'biological attribute' and affects_quality some ?Q and attribute_of some ?E");
	mdef.addParameter('E', e, TO);
	mdef.addParameter('Q', q, TO);
	createTerm(label, definition, synonyms, mdef);
}
