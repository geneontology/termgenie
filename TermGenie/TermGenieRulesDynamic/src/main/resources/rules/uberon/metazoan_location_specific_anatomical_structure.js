// @requires rules/common.js

function metazoan_location_specific_anatomical_structure() {
  var p = getSingleTerm("part", Uberon);
  var w = getSingleTerm("whole", Uberon);
  if (p == null || w == null) {
    // check branch
    error("The specified terms do not correspond to the pattern");
    return;
  }
  var label = termname(w, Uberon) + " " + termname(p, Uberon);
  var definition = "Any "+termname(p, Uberon)+" that is part of a "+termname(w, Uberon)+".";
  var synonyms = termgenie.synonyms(null, p, Uberon, " of ", w, Uberon, null, null, label);
  var mdef = createMDef("?P and 'part of' some ?W");
  mdef.addParameter('P', c, Uberon);
  mdef.addParameter('W', w, Uberon);
  createTerm(label, definition, synonyms, mdef);
}
