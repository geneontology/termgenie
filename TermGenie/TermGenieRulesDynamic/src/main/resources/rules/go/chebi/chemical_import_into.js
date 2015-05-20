// @requires rules/common.js

function chemical_import_into() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	
	var subject = getSingleTerm("subject", ont);
	var target = getSingleTerm("target", ont);
	
	// check that target is a cellular_component
	var check = checkGenus(target, "GO:0005575", ont); // celluar_component
	if (check.isGenus() !== true) {
		error(check.error());
		return;
	}
	
	var subjectName = termname(subject, ont);
	var targetName = termname(target, ont);
	var label = subjectName + " import into " + targetName;
	
	var definition = "The directed movement of "
			+ subjectName
			+ " into " + refname(target, ont) + ".";
	var synonyms = null;
	
	var mdef = createMDef("GO_0006810 " +
			"and ('has target end location' some ?T) " +
			"and ('imports' some ?S)");
	mdef.addParameter('S', subject, ont);
	mdef.addParameter('T', target, ont);
	createTerm(label, definition, synonyms, mdef);
}


function import_across_plasma_membrane() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var c = getSingleTerm("imported", ont);
	
	var label = termname(c, ont) + " import across plasma membrane";
	
	var definition = "The directed movement of "
			+ termname(c, ont)
			+ " from outside of a cell into the cytoplasmic compartment.";
	var synonyms = null;
	
	var mdef = createMDef("GO_0006810 " +
			"and ('has target start location' some GO_0005576) " +
			"and ('has target end location' some GO_0005829) " +
			"and ('results in transport across' some GO_0005886) " +
			"and ('imports' some ?C)");
	mdef.addParameter('C', c, ont);
	createTerm(label, definition, synonyms, mdef);
}

function import_across_membrane() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var cargo = getSingleTerm("imported", ont);
	var membrane = getSingleTerm("membrane", ont);
	var start = getSingleTerm("start", ont);
	var end = getSingleTerm("end", ont);
	
	var label = termname(cargo, ont) + " import across "+ termname(membrane, ont);
	var definition = "The directed import of "
		+ termname(cargo, ont)
		+ " from the " + termname(start, ont) 
		+ " across the " + termname(membrane, ont)
		+ " into the "+ termname(end, ont) + ".";
	var synonyms = null;
	
	var comment = 'This term covers ' + termname(cargo, ont)
				+ ' import *across* the ' + termname(membrane, ont)
				+ ' through a channel or pore. It does not cover import via vesicle fusion with '
				+ termname(membrane, ont)
				+', as in this case transport does not involve crossing the membrane.';
	
	var mdef = createMDef("GO_0006810 "+
		    "and ('has target start location' some ?S)"+
		    "and ('has target end location' some ?E)"+
		    "and ('imports' some ?C)"+
		    "and ('results in transport across' some ?M)");
	mdef.addParameter('C', cargo, ont);
	mdef.addParameter('S', start, ont);
	mdef.addParameter('E', end, ont);
	mdef.addParameter('M', membrane, ont);
	termgenie.createTerm(label, definition, synonyms, [mdef], null, comment);
}

function import_into_cell() {
	var ont = GeneOntology; // the graph wrapper contains all info, including CHEBI
	var c = getSingleTerm("imported", ont);
	
	var label = termname(c, ont) + " import into cell";
	
	var definition = "The directed movement of "
			+ termname(c, ont)
			+ " from outside of a cell, across the plasma membrane and into the cytoplasmic compartment. This may occur via transport across the plasma membrane or via endocytosis.";
	var synonyms = null;
	
	var mdef = createMDef("GO_0006810 " +
			"and ('has target start location' some GO_0005576) " +
			"and ('has target end location' some GO_0044424) " +
			"and ('imports' some ?C)");
	mdef.addParameter('C', c, ont);
	createTerm(label, definition, synonyms, mdef);
}