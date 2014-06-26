// @requires rules/common.js

function abnormal_morphology() {
  var e = getSingleTerm("entity", HP);
  var label = "abnormal " + termname(e, HP) + " morphology";
  var definition = "Any structural anomaly of " + refname(e, HP) + ".";
  var synonyms = null;
  // ('has part' some (pato:morphology and 'inheres in' some E and qualifier some abnormal))
  // TODO qualifier is currently unknown, it should be a relation
  var mdef = createMDef("('has part' some (PATO_0000051 and 'inheres in' some ?E and 'has component' some PATO_0000460))");
  mdef.addParameter('E', e, HP);
  createTerm(label, definition, synonyms, mdef);
}