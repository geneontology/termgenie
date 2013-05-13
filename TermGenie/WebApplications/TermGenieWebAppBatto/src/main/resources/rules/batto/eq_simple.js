// @requires rules/common.js

function eq_simple() {
	var e = getSingleTerm("entity", Batto);
	var q = getSingleTerm("quality", Batto);

	var label = termname(e, Batto) + ' ' + termname(q, Batto);
	var definition = "Any measurable or observable characteristic related to the "
			+ termname(q, Batto) + " of " + refname(e, Batto) + ".";

	var synonyms = null;
	// String label, List<ISynonym> results, String prefix, String infix, String suffix, String scope
	synonyms = termgenie.addSynonym(label, synonyms, null, label, ' trait', 'EXACT');
	
	var mdef = createMDef("'biological attribute' and affects_quality some ?Q and attribute_of some ?E");
	mdef.addParameter('E', e, Batto);
	mdef.addParameter('Q', q, Batto);
	createTerm(label, definition, synonyms, mdef);
}