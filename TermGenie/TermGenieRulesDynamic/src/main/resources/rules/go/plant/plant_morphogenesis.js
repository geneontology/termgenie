// @requires rules/common.js
// @requires rules/go/x_morphogenesis.js

function plant_morphogenesis() {
	// exclude synonyms, which have synonym type
	// This removes the Japanese, Spanish, and Plurals from PO
	termgenie.setSynonymFilters(false, true, null);
	x_morphogenesis(GeneOntology, "PO:0025131", false);
	termgenie.resetSynonymFilters();
}
