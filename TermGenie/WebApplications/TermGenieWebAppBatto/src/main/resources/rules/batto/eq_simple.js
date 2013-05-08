// @requires rules/common.js

function occurs_in() {
	var e = getSingleTerm("entity", batto);
	var q = getSingleTerm("quality", batto);

	var label = termname(e, batto) + ' ' + termname(q, batto);
	var definition = "Any measurable or observable characteristic related to the "
			+ termname(q, batto) + " of " + refname(e, batto) + ".";

	// TODO synonyms: E Q 'trait'
	var synonyms = null;
	
	var mdef = createMDef("'biological attribute' and affects_quality some ?Q and attribute_of some ?E");
	mdef.addParameter('E', e, batto);
	mdef.addParameter('Q', q, batto);
	createTerm(label, definition, synonyms, mdef);
}