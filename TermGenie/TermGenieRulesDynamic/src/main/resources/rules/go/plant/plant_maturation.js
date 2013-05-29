// @requires rules/common.js
// @requires rules/go/x_maturation.js

function plant_maturation() {
	// exclude synonyms, which have synonym type
	// This removes the Japanese, Spanish, and Plurals from PO
	termgenie.setSynonymFilters(false, true, null);
	x_maturation(GeneOntology, "PO:0025131", false);
	termgenie.resetSynonymFilters();
}
