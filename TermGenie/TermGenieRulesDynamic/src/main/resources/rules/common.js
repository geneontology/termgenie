/**
 * Helper functions to shorten the scripts
 */

/**
 * report an error message via TermGenie.
 * 
 * @param msg {String} 
 */
function error(msg) {
	termgenie.error(msg);
}

/**
 * Create a new term and provide output which can directly be returned.
 * 
 * @param label
 * @param definition
 * @param synonyms
 * @param logicalDefinition
 */
function createTerm(label, definition, synonyms, logicalDefinition) {
	termgenie.createTerm(label, definition, synonyms, logicalDefinition);
}

/**
 * Retrieve a term given the template field name.
 * 
 * @param name of template field
 * @param ontology the ontology to search in for the id extracted from the
 *            field
 * @return term or null
 */
function getSingleTerm(name, ontology) {
	return termgenie.getSingleTerm(name, ontology)
}

/**
 * Retrieve all terms as list from given the template field.
 * 
 * @param name of template field
 * @param ontology the ontology to search in for the id extracted from the
 *            field
 * @return terms array or null
 */
function getTerms(name, ontology) {
	return termgenie.getTerms(name, ontology);
}

/**
 * @param x the term
 * @param parent the parent term
 * @param ontology the ontology used for relations
 * @return check result
 * @see CheckResult
 */
function checkGenus(x, parent, ontology) {
	return termgenie.checkGenus(x, parent, ontology);
}

/**
 * @param x
 * @param parent
 * @param ontology
 * @return true if x is a genus of parent
 */
function genus(x, parent, ontology) {
	return termgenie.genus(x, parent, ontology);
}

/**
 * Check if there is an equivalence axiom for x, which has the checkedFor class in its signature.
 * 
 * @param x
 * @param checkedFor
 * @param ontology
 * @returns boolean
 */
function containsClassInEquivalenceAxioms(x, checkedFor, ontology) {
	return termgenie.containsClassInEquivalenceAxioms(x, checkedFor, ontology);
}

/**
 * Retrieve the set of equivalent classes for the given class. 
 * The set will contain the class itself.
 * 
 * @param x
 * @param ontology
 * @returns
 */
function getEquivalentClasses(x, ontology) {
	return termgenie.getEquivalentClasses(x, ontology);
}

/**
 * Retrieve the values for a template field
 * 
 * @param name name of the field
 * @return value array or null if not exists
 */
function getInputs(name) {
	return termgenie.getInputs(name);
}

/**
 * Retrieve the single value for a template field
 * 
 * @param name name of the field
 * @return value or null if not exists
 */
function getInput(name) {
	return termgenie.getInput(name);
}

/**
 * retrieve the name of a term
 * 
 * @param x term
 * @param ontology the ontology to look the name up
 * @return name
 */
function termname(x, ontology) {
	return termgenie.name(x, ontology);
}

/**
 * create the ref name of a term: prepend 'a ' or 'an ' to the label
 * 
 * @param x
 * @param ontology
 * @return ref name string
 */
function refname(x, ontology) {
	return termgenie.refname(x, ontology);
}

/**
 * Create a short info string for a term containing the label and id.
 * 
 * @param x
 * @param ontology
 * @return short info for a term
 */
function getTermShortInfo(x, ontology) {
	return termgenie.getTermShortInfo(x, ontology);
}

/**
 * Create a new MDef instance for the given definition in Manchester OWLs
 * syntax.
 * 
 * @param string
 * @return {@link MDef}
 */
function createMDef(string) {
	return termgenie.createMDef(string);
}
