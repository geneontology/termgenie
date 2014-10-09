// @requires rules/common.js

function cell_migration() {
  var go = GeneOntology;
  var c = getSingleTerm("celltype", go);
  
  var label = termname(c, go) + " migration";
  var definition = "The orderly movement of "+refname(c, go)+" from one site to another.";
  var synonyms = null;
  // TODO unify relation with GO, wait for appropriate relation in RO 'results in movement of'?
  var mdef = createMDef("'cell migration' and 'alters location of' some ?C"); 
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
  
}
