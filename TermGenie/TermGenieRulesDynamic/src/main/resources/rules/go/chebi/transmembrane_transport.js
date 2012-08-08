// @requires rules/common.js


function chemical_transmembrane_transport() {
	var ont = GeneOntology;
	
	// transport subject: chebi
	var x = getSingleTerm("subject", ont);
	
	// list of requested genus
	var geni = getInputs("subject");
	if (!geni || geni === null || geni.length === 0) {
		error("Could not create a term for X, as no genus was selected");
		return;
	}
	
	var count = 0;
	var name = termname(x, ont);
	
	// GO:0022857 ! transmembrane transporter activity
	if (termgenie.contains(geni, "GO:0022857")) {
		var label = name + " transmembrane transporter activity";
		var definition = "Catalysis of the transfer of "+name+" from one side of the membrane to the other.";
		var synonyms = null;
		var mdef = createMDef("GO_0022857 and 'transports or maintains localization of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	// GO:0015291 ! secondary active transmembrane transporter activity
	if (termgenie.contains(geni, "GO:0015291")) {
		var label = name + " secondary active transmembrane transporter activity";
		// TODO improve/specialize definition
		var definition = "Catalysis of the transfer of "+name+" from one side of the membrane to the other, up its concentration gradient. The transporter binds the solute and undergoes a series of conformational changes. Transport works equally well in either direction and is driven by a chemiosmotic source of energy. Chemiosmotic sources of energy include uniport, symport or antiport.";
		var synonyms = null;
		var mdef = createMDef("GO_0015291 and 'transports or maintains localization of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	// GO:0015563 ! uptake transmembrane transporter activity
	if (termgenie.contains(geni, "GO:0015563")) {
		var label = name + " uptake transmembrane transporter activity";
		// TODO improve/specialize definition
		var definition = "Catalysis of the transfer of "+name+" from the outside of a cell to the inside across a membrane.";
		var synonyms = null;
		var mdef = createMDef("GO_0015563 and 'transports or maintains localization of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}

	// GO:0042626 ! ATPase activity, coupled to transmembrane movement of substances
	if (termgenie.contains(geni, "GO:0042626")) {
		var label = name + " transmembrane-transporting ATPase activity";
		var definition = "Catalysis of the transfer of a solute or solutes from one side of a membrane to the other according to the reaction: ATP + H2O + "+name+"(in) = ADP + phosphate + "+name+"(out).";
		var synonyms = null;
		var mdef = createMDef("GO_0042626 and 'transports or maintains localization of' some ?X");
		mdef.addParameter('X', x, ont);
		
		var success = createTerm(label, definition, synonyms, mdef);
		if (success === true) {
			count += 1;
		}
		else {
			return;
		}
	}
	
	if (count === 0) {
		error("Could not create a term for X, as no known genus was selected");
	}
}
