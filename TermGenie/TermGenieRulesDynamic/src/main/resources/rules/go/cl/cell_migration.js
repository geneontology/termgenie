// @requires rules/common.js

function cell_migration() {
  var go = GeneOntology;
  var c = getSingleTerm("celltype", go);
  
  var label = termname(c, go) + " migration";
  var definition = "The orderly movement of "+refname(c, go)+" from one site to another.";
  var synonyms = null;
  var mdef = createMDef("'cell migration' and 'results in movement of' some ?C"); 
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
  
}
