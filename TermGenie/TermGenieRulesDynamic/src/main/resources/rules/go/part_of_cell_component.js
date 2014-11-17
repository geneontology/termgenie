// @requires rules/common.js

function part_of_cell_component() {
	var go = GeneOntology;
	var p = getSingleTerm("part", go);
	var w = getSingleTerm("whole", go);
	var check = checkGenus(p, "GO:0005575", go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	check = checkGenus(w, "GO:0005575", go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	var label = termname(w, go) + " " + termname(p, go);
	var definition = "Any " + termname(p, go) + " that is part of a "
			+ termname(w, go) + ".";
	var synonyms = termgenie.synonyms(null, p, go, " of ", w, go, null, null, label);
	var mdef = createMDef("?P and 'part of' some ?W");
	mdef.addParameter('P', p, go);
	mdef.addParameter('W', w, go);
	createTerm(label, definition, synonyms, mdef);
}
