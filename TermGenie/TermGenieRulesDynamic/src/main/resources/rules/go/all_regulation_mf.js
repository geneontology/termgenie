// @requires rules/common.js
// @requires rules/go/x_regulation_triad.js

function all_regulation_mf() {
  var x = getSingleTerm("target", GeneOntology);
  
  regulation_triad(x, "GO:0003674"); // molecular function
}
