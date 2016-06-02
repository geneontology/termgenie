// @requires rules/common.js

function omp_entity_quality() {
  var go = GeneOntology;
  var e = getSingleTerm("entity", go);
  var q = getSingleTerm("quality", PATO);
  if (e == null ||  q == null) {
    // check branch
    error("The specified terms do not correspond to the pattern");
    return;
  }
  var label = termname(q, PATO) + " of " + termname(e, go);
  var definition = "Any "+termname(q, PATO)+" of "+termname(e, go)+".";
  var synonyms = null; // TODO
  var mdef = createMDef("?Q and 'inheres in' some ?E");
  mdef.addParameter('Q', q, PATO);
  mdef.addParameter('E', e, go);
  createTerm(label, definition, synonyms, mdef);
}
