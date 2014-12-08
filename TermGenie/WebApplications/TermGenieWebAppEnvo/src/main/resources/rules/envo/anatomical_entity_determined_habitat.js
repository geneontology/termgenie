// @requires rules/common.js

function anatomical_entity_determined_habitat() {
  var e = getSingleTerm("entity", ENVO);
  var label = termname(e, ENVO) + " environment";
  var definition = "An environment that is determined by " + refname(e, ENVO) + ".";
  var synonyms = null;
  var mdef = createMDef("'environmental system' and 'determined by' some ?E");
  mdef.addParameter('E', e, ENVO);
  createTerm(label, definition, synonyms, mdef);
}