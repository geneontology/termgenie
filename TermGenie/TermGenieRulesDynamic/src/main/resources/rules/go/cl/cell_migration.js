// @requires rules/common.js

function cell_migration() {
  var go = GeneOntology;
  var c = getSingleTerm("celltype", go);
  
  var label = termname(c, go) + " migration";
  var definition = "The movement of "+refname(c, go)+" within or between different tissues and organs of the body.";
  var synonyms = null;
  var mdef = createMDef("'cell migration' and 'alters location of' some ?C");
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
  
}
