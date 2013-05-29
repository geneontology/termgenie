// @requires rules/common.js
// @requires rules/go/x_structural_organization.js

function plant_structural_organization() {
	// exclude synonyms, which have synonym type
	// This removes the Japanese, Spanish, and Plurals from PO
	termgenie.setSynonymFilters(false, true, null);
	x_structural_organization(GeneOntology, "PO:0025131", true);
	termgenie.resetSynonymFilters();
}
