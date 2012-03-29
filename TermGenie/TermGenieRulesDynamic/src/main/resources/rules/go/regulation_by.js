// @requires rules/common.js

function regulation_by() {
  var go = GeneOntology;
  var p = getSingleTerm("process", go);
  var r = getSingleTerm("regulator", go);
  var check = checkGenus(r, "GO:0008150", go); // check r is_a 'biological_process'
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  check = checkGenus(p, "GO:0050789", go); // check p is_a 'regulation of biological process'
  if (check.isGenus() !== true) {
    error(check.error());
    return;
  }
  subrun(r);
  
//  var negative_r = go.getOWLObjectByLabel("negative "+termname(r, go));
//  if (negative_r !== null) {
//    subrun(negative_r);
//  }
//  
//  var positive_r = go.getOWLObjectByLabel("positive "+termname(r, go));
//  if (positive_r !== null) {
//    subrun(positive_r);
//  }
  
  function subrun(regulator) {
	var processName = termname(p, go);
	var regulatorName = termname(regulator, go);
	
	// special rule to reduce the number of proposed prefixes for the term
	// more details see: http://wiki.geneontology.org/index.php/Ontology_meeting_2012-03-21
	var requiredPrefixLeft = checkRequiredPrefix(processName);
	var ignoreSynonymsRight = genus(regulator, 'GO:0050789', go); // if regulator is_a 'regulation of biological process', ignore synonyms 

    var label = processName + " by " + regulatorName;
    
    var definitionVerb = 'modulates';
    if (genus(regulator, 'GO:0048518', go) === true) {
    	// if it is_a 'positive regulation of biological process'
    	definitionVerb = 'increases';
    } 
    else if (genus(regulator, 'GO:0048519', go) === true) { 
    	// if it is_a 'negative regulation of biological process'
    	definitionVerb = 'decreases';
    }
    
    var definition = "Any process that "+definitionVerb+" the frequency, rate or extent of "+processName+", by "+regulatorName+".";
    var synonyms = termgenie.synonyms(null, p, go, " by ", regulator, go, null, null, label, requiredPrefixLeft, ignoreSynonymsRight);
    
    var mdef = createMDef("?R and 'results_in' some ?P");
    mdef.addParameter('P', p, go);
    mdef.addParameter('R', regulator, go);
    termgenie.createTerm(label, definition, synonyms, mdef);
  };
  
  function checkRequiredPrefix(s) {
    var prefix = null;
    if (s.indexOf('regulation of ') === 0) {
    	prefix = 'regulation of ';
	}
	else if (s.indexOf('negative regulation of ') === 0) {
		prefix = 'negative regulation of ';
	}
	else if (s.indexOf('regulation of ') === 0) {
		prefix = 'positive regulation of ';
	}
    return prefix;
  }
}
