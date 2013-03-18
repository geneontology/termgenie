// @requires rules/common.js

function envenomation() {
	var go = GeneOntology;
	var p = getSingleTerm("process", go);
	
	var check = checkGenus(p, "GO:0035821", go); // modification of morphology or physiology of other organism
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var preferredTermName =  termname(p, go);
	var label = "envenomation resulting in " + preferredTermName;
	var definition = "A process that begins with venom being forced into an organism by the bite or sting of another organism, and ends with the resultant "
				+ preferredTermName
				+ ".";
	var synonyms = termgenie.synonyms("envenomation resulting in ", p, go, null, null, label);
	var mdef = createMDef("GO_0035738 and 'has part' some ?P"); // GO:0035738 ! envenomation resulting in modification of morphology or physiology of other organism
	mdef.addParameter('P', p, go);
	createTerm(label, definition, synonyms, mdef);

}
