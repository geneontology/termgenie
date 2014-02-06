// @requires rules/common.js

function catabolism_via() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var source = getSingleTerm("source", ont);
	var via = getSingleTerm("via", ont);

	var sourcename = termname(source, ont);
	var vianame = termname(via, ont);
	var label = sourcename + " catabolic process via " + vianame;
	var definition = "The chemical reactions and pathways resulting in the breakdown of "
					+ sourcename + " via "+ vianame + ".";

	var synonyms = null;
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' catabolism via ', vianame, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' catabolic process via ', vianame, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' breakdown via ', vianame, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' degradation via ', vianame, 'EXACT');
		
	var mdef = createMDef("GO_0009056 and 'has input' some ?X and 'has intermediate' some ?V");
	mdef.addParameter('X', source, ont);
	mdef.addParameter('V', via, ont);
	var success = createTerm(label, definition, synonyms, mdef);
}
