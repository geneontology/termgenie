// @requires rules/common.js

function cell_by_surface_marker() {
  var go = GeneOntology;
  var pro = ProteinOntology;
  var c = getSingleTerm("cell", CL);
  var two = [pro, go];
  var p = getSingleTerm("membrane_part", two);
  if (c == null || p == null || !(genus(p, "GO:0032991", go) || genus(p, "PR:000000001", pro))) {
    // check branch
    error("The specified terms do not correspond to the pattern");
    return;
  }
  var label = termname(p, two) + " " + termname(c, CL);
  var definition = "Any "+termname(c, CL)+" that has "+termname(p, two)+" on the plasma membrane.";
  var synonyms = null; // TODO
  var mdef = createMDef("?C and 'has_plasma_membrane_part' some ?P");
  mdef.addParameter('C', c, CL);
  mdef.addParameter('P', p, two);
  // cdef.property("multivalued(has_plasma_membrane_part)");
  // cdef.property("any_cardinality(has_plasma_membrane_part)");
  createTerm(label, definition, synonyms, mdef);
}
