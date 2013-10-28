// @requires rules/common.js

function protein_complex_by_activity() {
	var go = GeneOntology;
	var mf = getSingleTerm("activity", go);
	var check = checkGenus(mf, "GO:0003674", go);
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}


	var termnameMf = termname(mf, go);
	if (termnameMf.endsWith(' activity') === false) {
		error('Unexpected label - the molecular function term label '+termnameMf+' does not end in "activity".');
		return;
	}
	var index = termnameMf.length() - 9; // length of the  string ' activity'
	var termnameTrimmed = termnameMf.substring(0, index); 

	var label = termnameTrimmed + ' complex';
	var definition = "A protein complex which is capable of executing the molecular function "+termnameMf+".";

	synonyms = null;
	// 'protein complex' and 'capable_of' some 'molecular function'
	var mdef = createMDef("GO_0043234 and 'capable_of' some ?A");
	mdef.addParameter('A', mf, go);
	return createTerm(label, definition, synonyms, mdef);
}
