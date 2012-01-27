// @requires rules/common.js

function occurs_in() {
  var go = GeneOntology;
  var p = getSingleTerm("process", go);
  var c = getSingleTerm("location", go);
  var check = checkGenus(p, "GO:0008150", go);
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  check = checkGenus(c, "GO:0005575", go);
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  var label = termname(p, go) + " in " + termname(c, go);
  var definition = "Any "+termname(p, go)+" that takes place in "+termname(c, go)+".";
  var synonyms = termgenie.synonyms(null, p, go, " in ", c, go, null, null, label);
  var mdef = createMDef("?P and 'occurs in' some ?C");
  mdef.addParameter('P', p, go);
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
}
