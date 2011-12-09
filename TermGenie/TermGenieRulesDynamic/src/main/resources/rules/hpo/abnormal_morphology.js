// @requires rules/common.js

function abnormal_morphology() {
  var a = getSingleTerm("target", FMA);
  if (a === null) {
    error("The specified term does not correspond to the pattern");
    return;
  }
  var label = "Abnormal "+termname(a, FMA) + " morphology";
  var definition = "Any morphological abnormality of a "+termname(a, FMA)+".";
  var synonyms = null; // TODO
  var mdef = createMDef("PATO_0000051 and 'inheres_in' some ?X");
  mdef.addParameter('X', a, FMA);
  createTerm(label, definition, synonyms, mdef);
}
