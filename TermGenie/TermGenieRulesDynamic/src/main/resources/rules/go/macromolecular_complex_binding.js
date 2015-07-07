// @requires rules/common.js

function macromolecular_complex_binding() {
	var go = GeneOntology;
	var c = getSingleTerm("complex", go);
	
	var check = checkGenus(c, "GO:0032991", go); // macromolecular complex
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var name = termname(c, go);
	var cRefName = refname(c, go);

	var label = name + " binding";
	var definition = "Interacting selectively and non-covalently with " + cRefName + "."; 

	var synonyms = termgenie.synonyms(null, ['EXACT'], c, go, [' binding'], label);
	var mdef = createMDef("GO_0005488 and 'has input' some ?C");
	mdef.addParameter('C', c, go);
	createTerm(label, definition, synonyms, mdef);
}