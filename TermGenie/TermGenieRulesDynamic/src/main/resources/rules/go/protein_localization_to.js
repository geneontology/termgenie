// @requires rules/common.js

function protein_localization_to() {
  var go = GeneOntology;
  var c = getSingleTerm("component", go);
  var check = checkGenus(c, "GO:0005575", go);
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  var termnamec = termname(c, go);
  var label = "protein localization to " + termnamec;
  var definition = "A process in which a protein is transported to, or maintained in, a location within the "+termnamec+".";
  
  // BE spelling
  var synonyms = termgenie.addSynonym(label, null, 'protein localisation to ', '', termnamec, 'EXACT');
  
  // 'in' variation
  synonyms = termgenie.addSynonym(label, synonyms, 'protein localization in ', '', termnamec, 'EXACT');
  
  //BE spelling for 'in' variation
  synonyms = termgenie.addSynonym(label, synonyms, 'protein localisation in ', '', termnamec, 'EXACT');
  
  var mdef = createMDef("GO_0008104 and 'has_target_end_location' some ?C");
  mdef.addParameter('C', c, go);
  createTerm(label, definition, synonyms, mdef);
}
