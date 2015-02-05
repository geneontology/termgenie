// @requires rules/common.js

function abnormal_levels() {
	var chemical = getSingleTerm('chemical', MP);
	var location = getSingleTerm('location', MP);
	var prefixes = getInputs('chemical');
	var count = 0;
	if (termgenie.contains(prefixes, 'unspecified')) {
		var success = abnormal_levels_unspecific(chemical, location);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, 'greater')) {
		var greater = MP.getOWLClassByIdentifier('PATO:0000470');
		if (greater === null) {
			error('Could not find class PATO:0000470');
			return;
		}
		var success = abnormal_levels_directed(chemical, location, greater, 'greater');
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, 'reduced')) {
		var reduced = MP.getOWLClassByIdentifier('PATO:0001997');
		if (reduced === null) {
			error('Could not find class PATO:0001997');
			return;
		}
		var success = abnormal_levels_directed(chemical, location, reduced, 'reduced');
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (count === 0) {
		error('Could not create a term for X, as no known prefix was selected');
	}
}

function abnormal_levels_unspecific(chemical, location) {

	var label = "abnormal " + termname(location, MP) + " "+ termname(chemical, MP) + " level";
	var definition = "Any change amount of " + termname(chemical, MP) + 
					" in the " + termname(location, MP) + ".";
	var synonyms = null;
	// has_part some ('quality' and inheres_in some (?chemical and part_of some ?location) and 'has component' some 'abnormal')
	var mdef = createMDef("'has part' some ('quality' and 'inheres in' some (?chemical and 'part of' some ?location) and 'has component' some 'abnormal')");
	mdef.addParameter('chemical', chemical, MP);
	mdef.addParameter('location', location, MP);
	return createTerm(label, definition, synonyms, mdef);
}


function abnormal_levels_directed(chemical, location, quality, qualityLabel) {

	var label = qualityLabel+ " " + termname(location, MP) + " " + termname(chemical, MP) + " level";
	var definition = termgenie.firstToUpperCase(qualityLabel)+ " amount of " + termname(chemical, MP) + 
					" in the " + termname(location, MP) + ".";
	var synonyms = null;
	// has_part some (?quality and inheres_in some (?chemical and part_of some ?location))
	var mdef = createMDef("'has part' some (?quality and 'inheres in' some (?chemical and 'part of' some ?location))");
	mdef.addParameter('chemical', chemical, MP);
	mdef.addParameter('location', location, MP);
	mdef.addParameter('quality', quality, MP);
	return createTerm(label, definition, synonyms, mdef);
}