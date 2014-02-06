// @requires rules/common.js

function catabolism_to() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var source = getSingleTerm("source", ont);
	var to = getSingleTerm("to", ont);

	var sourcename = termname(source, ont);
	var toname = termname(to, ont);
	var label = sourcename + " catabolic process to " + toname;
	var definition = "The chemical reactions and pathways resulting in the breakdown of "
					+ sourcename + " to "+ toname + ".";

	var synonyms = null;
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' catabolism to ', toname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' catabolic process to ', toname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' breakdown to ', toname, 'EXACT');
	synonyms = termgenie.addSynonym(label, synonyms, sourcename, ' degradation to ', toname, 'EXACT');
		
	var mdef = createMDef("GO_0009056 and 'has input' some ?S and 'has output' some ?T");
	mdef.addParameter('S', source, ont);
	mdef.addParameter('T', to, ont);
	var success = createTerm(label, definition, synonyms, mdef);
}
