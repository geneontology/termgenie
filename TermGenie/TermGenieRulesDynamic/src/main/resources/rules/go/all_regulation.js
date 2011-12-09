// @requires rules/common.js
// @requires rules/go/x_regulation_triad.js

function all_regulation() {
  var x = getSingleTerm("target", GeneOntology);
  
  regulation_triad(x, "GO:0008150"); // biological_process
}
