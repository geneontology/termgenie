// @requires rules/common.js

function late_early_onset_process() {
	var process = getSingleTerm('process', MP);
	var prefixes = getInputs('process');
	var count = 0;
	if (termgenie.contains(prefixes, 'delayed')) {
		var late = MP.getOWLClassByIdentifier('PATO:0000502');
		if (late === null) {
			error('Could not find class PATO:0000502');
			return;
		}
		var success = internal_abnormal_onset_process(process, late, 'delayed');
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	if (termgenie.contains(prefixes, 'early')) {
		var early = MP.getOWLClassByIdentifier('PATO:0000694');
		if (early === null) {
			error('Could not find class PATO:0000694');
			return;
		}
		var success = internal_abnormal_onset_process(process, early, 'early');
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

function abnormal_onset_process() {
	var process = getSingleTerm('process', MP);
	var onsetQuality = getSingleTerm('onset', MP);
	var onsetLabel = MP.getLabel(onsetQuality);
	if (onsetLabel === null) {
		error('Could not find a label for onset quality: '+onsetQuality);
		return;
	}
	return internal_abnormal_onset_process(process, onsetQuality, onsetLabel);
}

function internal_abnormal_onset_process(process, onsetQuality, onsetLabel) {
	var label = onsetLabel+ " onset of " + termname(process, MP);
	var definition = "The "+onsetLabel+" onset of the process of " + refname(process, MP) + ".";
	var synonyms = null;
	// 'has part' some ('onset quality' and 'inheres in' some 'process') and ('has modifier' some abnormal))
	var mdef = createMDef("('has part' some (?Q and 'inheres in' some ?P and 'has modifier' some PATO_0000460))");
	mdef.addParameter('Q', onsetQuality, MP);
	mdef.addParameter('P', process, MP);
	return createTerm(label, definition, synonyms, mdef);
}