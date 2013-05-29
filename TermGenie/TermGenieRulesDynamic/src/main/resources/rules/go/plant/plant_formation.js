// @requires rules/common.js
// @requires rules/go/x_formation.js

function plant_formation() {
	// exclude synonyms, which have synonym type
	// This removes the Japanese, Spanish, and Plurals from PO
	termgenie.setSynonymFilters(false, true, null);
	x_formation(GeneOntology, "PO:0025131", false);
	termgenie.resetSynonymFilters();
}
