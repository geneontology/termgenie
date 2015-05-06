// @requires rules/common.js

function cc_organization() {
	var go = GeneOntology;
	var c = getSingleTerm("component", go);
	
	var check = checkGenus(c, "GO:0005575", go); // cellular_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var name = termname(c, go);
	var cRefName = refname(c, go);

	var label = name + " organization";
	var definition = "A process that is carried out at the cellular level which results in the assembly, arrangement of constituent parts, or disassembly of " + cRefName + "."; 

	var synonyms = termgenie.synonyms(null, ['EXACT'], c, go, [' organization'], label);
	var mdef = createMDef("GO_0016043 and 'results_in_organization_of' some ?C");
	mdef.addParameter('C', c, go);
	createTerm(label, definition, synonyms, mdef);
}