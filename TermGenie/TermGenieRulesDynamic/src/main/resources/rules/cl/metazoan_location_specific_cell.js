// @requires rules/common.js

function metazoan_location_specific_cell() {
  var c = getSingleTerm("cell", CL);
  var a = getSingleTerm("location", Uberon);
  if (c == null || a == null) {
    // check branch
    error("The specified terms do not correspond to the pattern");
    return;
  }
  var label = termname(a, Uberon) + " " + termname(c, CL);
  var definition = "Any "+termname(c, CL)+" that is part of a "+termname(a, Uberon)+".";
  var synonyms = null; // TODO
  var mdef = createMDef("?C and 'part_of' some ?A");
  mdef.addParameter('C', c, CL);
  mdef.addParameter('A', a, Uberon);
  createTerm(label, definition, synonyms, mdef);
}
