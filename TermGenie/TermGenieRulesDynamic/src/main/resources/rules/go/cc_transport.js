// @requires rules/common.js

function cc_transport() {
	var go = GeneOntology;
	var c = getSingleTerm("component", go);
	var check = checkGenus(c, "GO:0005575", go); // cellular_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
  
	var name = termname(c, go);
  
	var label = name + " transport";
	var definition = "The directed movement of " + name 
					+ " into, out of or within a cell, or between cells, "
					+ "by means of some agent such as a transporter or pore.";
	
	var synonyms = termgenie.synonyms(null, c, go, " transport", null, label);
	var mdef = createMDef("GO_0006810 and 'transports or maintains localization of' some ?C");
	mdef.addParameter('C', c, go);
	createTerm(label, definition, synonyms, mdef);
  
}