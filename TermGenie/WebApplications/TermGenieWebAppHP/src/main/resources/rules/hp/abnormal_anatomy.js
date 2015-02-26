// @requires rules/common.js

function abnormal_anatomy() {
	var e = getSingleTerm("entity", HP);
	var label = "Abnormality of " + termname(e, HP);
	var definition = "An abnormality of " + refname(e, HP) + ".";
	var synonyms = null;
	// 'has part' some (quality and ('inheres in' some '[UBERON class]') and ('has modifier' some abnormal))
	var mdef = createMDef("'has part' some (PATO_0000001 and ('inheres in' some ?E) and ('has modifier' some PATO_0000460))");
	mdef.addParameter('E', e, HP);
	createTerm(label, definition, synonyms, mdef);
}