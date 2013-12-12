// @requires rules/common.js

function eq_simple() {
	var e = getSingleTerm("entity", OBA);
	var q = getSingleTerm("quality", OBA);

	var label = termname(e, OBA) + ' ' + termname(q, OBA);
	var definition = "Any measurable or observable characteristic related to the "
			+ termname(q, OBA) + " of " + refname(e, OBA) + ".";

	var synonyms = null;
	// String label, List<ISynonym> results, String prefix, String infix, String suffix, String scope
	synonyms = termgenie.addSynonym(label, synonyms, null, label, ' trait', 'EXACT');
	
	var mdef = createMDef("'biological attribute' and affects_quality some ?Q and attribute_of some ?E");
	mdef.addParameter('E', e, OBA);
	mdef.addParameter('Q', q, OBA);
	createTerm(label, definition, synonyms, mdef);
}