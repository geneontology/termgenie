// @requires rules/common.js

function biosynthesis_via() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var t = getSingleTerm("target", ont);
	var v = getSingleTerm("via", ont);

	var tname = termname(t, ont);
	var vname = termname(v, ont);

	var label = tname + " biosynthetic process via " + vname;
	var definition = "The chemical reactions and pathways resulting in the formation of "
					+ tname + " via "+ vname + ".";

	var synonyms = null;
	synonyms = termgenie.addSynonym(label, synonyms, tname, ' biosynthesis via ', vname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, tname, ' biosynthetic process via ', vname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, tname, ' anabolism via ', vname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, tname, ' formation via ', vname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, tname, ' synthesis via ', vname, 'EXACT');

	var mdef = createMDef("GO_0009058 and 'has output' some ?T and 'has intermediate' some ?V");
	mdef.addParameter('T', t, ont);
	mdef.addParameter('V', v, ont);
	return createTerm(label, definition, synonyms, mdef);
}
