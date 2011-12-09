// @requires rules/common.js

function regulation_by() {
  var go = GeneOntology;
  var p = getSingleTerm("process", go);
  var r = getSingleTerm("regulator", go);
  var check = checkGenus(p, "GO:0008150", go);
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  check = checkGenus(r, "GO:0050789", go); // regulation of biological process
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  subrun(r);
  
  var negative_r = go.getOWLObjectByLabel("negative "+termname(r, go));
  if (negative_r !== null) {
    subrun(negative_r);
  }
  
  var positive_r = go.getOWLObjectByLabel("positive "+termname(r, go));
  if (positive_r !== null) {
    subrun(positive_r);
  }
  
  function subrun(regulator) {
    var label = "regulation of "+termname(p, go) + " by " + termname(regulator, go);
    var definition = "Any process that modulates the frequency, rate or extent of "+termname(p, go)+", by "+termname(regulator, go)+".";
    var synonyms = termgenie.synonyms("regulation of ", p, go, " by ", regulator, go, null, label);
    
    // intentionally null, as the intended relations do not fit the pattern
    // but the synonyms, might be of some use
    var mdef = [];
    // var mdef = createMDef("GO_0065007 and is_a some ?P and is_a some ?R");
    // mdef.addParameter('P', p, go);
    // mdef.addParameter('P', regulator, go);
    termgenie.createTerm(label, definition, synonyms, mdef);
  };
}
