// @requires rules/common.js

function chemical_import_into() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var subject = getSingleTerm("subject", ont);
	var target = getSingleTerm("target", ont);
	
	// check that target is a cellular_component
	var check = checkGenus(target, "GO:0005575", ont); // celluar_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	
	var subjectName = termname(subject, ont);
	var targetName = termname(target, ont);
	var label = subjectName + " import into " + targetName;
	
	var definition = "The directed movement of "
			+ subjectName
			+ " into " + refname(target, ont) + ".";
	var synonyms = null;
	
	var mdef = createMDef("GO_0006810 and 'has_target_end_location' some ?T and 'imports' some ?S");
	mdef.addParameter('S', subject, ont);
	mdef.addParameter('T', target, ont);
	createTerm(label, definition, synonyms, mdef);
}