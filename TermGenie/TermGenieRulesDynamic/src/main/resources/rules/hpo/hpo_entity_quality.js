// @requires rules/common.js

function hpo_entity_quality() {
  var e = getSingleTerm("entity", FMA);
  var q = getSingleTerm("quality", PATO);
  if (e === null || q === null) {
    error("The specified terms do not correspond to the pattern");
    return;
  }
  var label = termname(q, PATO) + " " + termname(e, FMA);
  var definition = "Any "+termname(e, FMA)+" that is "+termname(q, PATO)+".";
  var synonyms = null; // TODO
  var mdef = createMDef("?Q and 'inheres_in' some ?E");
  mdef.addParameter('Q', q, PATO);
  mdef.addParameter('E', e, FMA);
  createTerm(label, definition, synonyms, mdef);
}
