// @requires rules/common.js

function metabolism_catabolism_biosynthesis() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var x = getSingleTerm("target", ont);
	var prefixes = getInputs("target");
	if (!prefixes || prefixes === null || prefixes.length === 0) {
		error("Could not create a term for X, as no subtemplate was selected");
		return;
	}
	var count = 0;
	var name = termname(x, ont);
	if (termgenie.contains(prefixes, "metabolism")) {
		var label = name + " metabolic process";
		var definition = "The chemical reactions and pathways involving "
				+ name + ".";
		
		var synonyms = termgenie.addSynonym(label, null, null, name, ' metabolism', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' metabolic process', 'EXACT');
		
		var mdef = createMDef("GO_0008152 and 'has participant' some ?X");
		mdef.addParameter('X', x, ont);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, "catabolism")) {
		var label = name + " catabolic process";
		var definition = "The chemical reactions and pathways resulting in the breakdown of "
				+ name + ".";

		var synonyms = termgenie.addSynonym(label, null, null, name, ' catabolism', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' catabolic process', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' breakdown', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' degradation', 'EXACT');
		
		var mdef = createMDef("GO_0009056 and 'has input' some ?X");
		mdef.addParameter('X', x, ont);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, "biosynthesis")) {
		var label = name + " biosynthetic process";
		var definition = "The chemical reactions and pathways resulting in the formation of "
				+ name + ".";

		var synonyms = termgenie.addSynonym(label, null, null, name, ' biosynthesis', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' biosynthetic process', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' anabolism', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' formation', 'EXACT');
		synonyms = termgenie.addSynonym(label, synonyms, null, name, ' synthesis', 'EXACT');
		
		var mdef = createMDef("GO_0009058 and 'has output' some ?X");
		mdef.addParameter('X', x, ont);
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error("Could not create a term for X, as no known subtemplate was selected");
	}
}
