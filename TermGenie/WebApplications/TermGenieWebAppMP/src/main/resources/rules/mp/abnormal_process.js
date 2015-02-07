// @requires rules/common.js

function abnormal_process() {
	var e = getSingleTerm("entity", MP);
	var label = "abnormal " + termname(e, MP);
	var definition = "Anomaly in the process of " + refname(e, MP) + ".";
	var synonyms = termgenie.synonyms("abnormal ", e, MP, null, null, label);
	// 'has part' some (quality and (‘inheres in part of’) some ‘process) and (‘has component’ some abnormal))
	var mdef = createMDef("('has part' some (PATO_0000001 and 'inheres in part of' some ?E and 'has component' some PATO_0000460))");
	mdef.addParameter('E', e, MP);
	createTerm(label, definition, synonyms, mdef);
}

function abnormal_process_in_location() {
	var p = getSingleTerm("process", MP);
	var l = getSingleTerm("location", MP);
	var label = "abnormal " + termname(p, MP) + " in "+ termname(l, MP);
	var definition = "Anomaly in the process of " + refname(p, MP) + " in "+ refname(l, MP) + ".";
	var synonyms = null;
	// 'has part' some (quality and ('inheres in' some 'process' and ('occurs in' some 'anatomy’)) and ('has component' some abnormal))
	var mdef = createMDef("'has part' some (PATO_0000001 and ('inheres in' some ?process and ('occurs in' some ?location)) and ('has component' some PATO_0000460))");
	mdef.addParameter('process', p, MP);
	mdef.addParameter('location', l, MP);
	createTerm(label, definition, synonyms, mdef);
}