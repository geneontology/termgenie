// @requires rules/common.js

function cell_differentiation() {
  var go = GeneOntology;
  var c = getSingleTerm("celltype", go);
  
  var label = termname(c, go) + " differentiation";
  var definition = "The process in which a relatively unspecialized cell acquires the specialized features of "+refname(c, go)+".";
  var synonyms = null;
  var mdef = createMDef("GO_0030154 and 'results in acquisition of features of' some ?C");
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
  
}
