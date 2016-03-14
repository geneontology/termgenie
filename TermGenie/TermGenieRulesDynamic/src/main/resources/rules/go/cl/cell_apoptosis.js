// @requires rules/common.js

function cell_apoptosis() {
  var go = GeneOntology;
  var c = getSingleTerm("celltype", go);
  
  var label = termname(c, go) + " apoptotic process";
  var definition = "Any apoptotic process in "+refname(c, go)+".";
  var synonyms = termgenie.synonyms(null, ['EXACT', 'NARROW'], c, go, [" apoptotic process", " apoptosis"], label);
  var mdef = createMDef("GO_0006915 and 'occurs in' some ?C");
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
  
}
