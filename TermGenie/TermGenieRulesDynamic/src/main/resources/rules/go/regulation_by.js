// @requires rules/common.js

function regulation_by() {
  var go = GeneOntology;
  var p = getSingleTerm("process", go);
  var r = getSingleTerm("regulator", go);
  var check = checkGenus(r, "GO:0008150", go); // check r is_a 'biological_process'
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  check = checkGenus(p, "GO:0050789", go); // check p is_a 'regulation of biological process'
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  subrun(r);
  
//  var negative_r = go.getOWLObjectByLabel("negative "+termname(r, go));
//  if (negative_r !== null) {
//    subrun(negative_r);
//  }
//  
//  var positive_r = go.getOWLObjectByLabel("positive "+termname(r, go));
//  if (positive_r !== null) {
//    subrun(positive_r);
//  }
  
  function subrun(regulator) {
    var label = termname(p, go) + " by " + termname(regulator, go);
    var definition = "Any process that modulates the frequency, rate or extent of "+termname(p, go)+", by "+termname(regulator, go)+".";
    var synonyms = termgenie.synonyms(null, p, go, " by ", regulator, go, null, null, label);
    
    var mdef = createMDef("?R and 'results_in' some ?P");
    mdef.addParameter('P', p, go);
    mdef.addParameter('R', regulator, go);
    termgenie.createTerm(label, definition, synonyms, mdef);
  };
}
