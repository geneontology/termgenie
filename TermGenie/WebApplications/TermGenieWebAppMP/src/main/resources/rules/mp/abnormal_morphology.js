// @requires rules/common.js

function abnormal_morphology() {
  var e = getSingleTerm("entity", MP);
  var label = "abnormal " + termname(e, MP) + " morphology";
  var definition = "Any structural anomaly of " + refname(e, MP) + ".";
  var synonyms = null;
  // ('has part' some (pato:morphology and 'inheres in' some E and 'has modifier' some abnormal))
  var mdef = createMDef("('has part' some (PATO_0000051 and 'inheres in' some ?E and 'has modifier' some PATO_0000460))");
  mdef.addParameter('E', e, MP);
  createTerm(label, definition, synonyms, mdef);
}