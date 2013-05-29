// @requires rules/common.js
// @requires rules/go/x_development.js

function plant_development() {
	// exclude synonyms, which have synonym type
	// This removes the Japanese, Spanish, and Plurals from PO
	termgenie.setSynonymFilters(false, true, null);
	x_development(GeneOntology, "PO:0025131");
	termgenie.resetSynonymFilters();
}
