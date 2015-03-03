// @requires rules/common.js

function generic_entity_quality() {
	var e = getSingleTerm("entity", MP);
	var q = getSingleTerm("quality", MP);
 	var label = termname(q, MP) + " " + termname(e, MP);
 	var definition = termgenie.firstToUpperCase(refname(q, MP)) + " in " + refname(e, MP) + " compared to normal.";
 	var synonyms = null;
 	var mdef = createMDef("('has part' some (?Q and 'inheres in' some ?E and 'has modifier' some PATO_0000460))");
 	mdef.addParameter('E', e, MP);
 	mdef.addParameter('Q', q, MP);
 	createTerm(label, definition, synonyms, mdef);
}
