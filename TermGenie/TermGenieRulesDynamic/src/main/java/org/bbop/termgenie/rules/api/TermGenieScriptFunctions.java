package org.bbop.termgenie.rules.api;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

public interface TermGenieScriptFunctions extends TermGenieScriptFunctionsSynonyms {

	/**
	 * Retrieve a term given the template field name.
	 * 
	 * @param name of template field
	 * @param ontology the ontology to search in for the id extracted from the
	 *            field
	 * @return term or null
	 */
	public OWLObject getSingleTerm(String name, OWLGraphWrapper ontology);

	/**
	 * Retrieve a term given the template field name.
	 * 
	 * @param name of template field
	 * @param ontologies the ontologies to search in for the id extracted from
	 *            the field
	 * @return term or null
	 */
	public OWLObject getSingleTerm(String name, OWLGraphWrapper[] ontologies);

	/**
	 * Retrieve all terms as list from given the template field.
	 * 
	 * @param name of template field
	 * @param ontology the ontology to search in for the id extracted from the
	 *            field
	 * @return term or null
	 */
	public OWLObject[] getTerms(String name, OWLGraphWrapper ontology);

	/**
	 * @param x the term
	 * @param parent the parent term
	 * @param ontology the ontology used for relations
	 * @return check result
	 * @see CheckResult
	 */
	public CheckResult checkGenus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology);

	/**
	 * @param x the term
	 * @param parent the parent id
	 * @param ontology the ontology used for relations
	 * @return check result
	 * @see CheckResult
	 */
	public CheckResult checkGenus(OWLObject x, String parent, OWLGraphWrapper ontology);

	/**
	 * @param x
	 * @param parent
	 * @param ontology
	 * @return true if x is a genus of parent
	 */
	public boolean genus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology);

	/**
	 * @param x
	 * @param parent
	 * @param ontology
	 * @return true if x is a genus of parent
	 */
	public boolean genus(OWLObject x, String parent, OWLGraphWrapper ontology);
	
	/**
	 * Check if the target class has equivalence axioms, which has any of checkedFor classes in its signature.
	 * 
	 * @param targetClass
	 * @param checkedForClasses
	 * @param ontology
	 * @return true if there is an equivalence axiom, which has any of checkedFor classes in its signature.
	 */
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass, Set<OWLClass> checkedForClasses, OWLGraphWrapper ontology);

	/**
	 * Check if the target class has equivalence axioms, which use the checkedFor class in its signature.
	 * 
	 * @param targetClass
	 * @param checkedFor
	 * @param ontology
	 * @return true if there is an equivalence axiom which has the checkedFor class in its signature
	 */
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass, OWLClass checkedFor, OWLGraphWrapper ontology);
	
	/**
	 * Check if the target class has equivalence axioms, which use the checkedFor class in its signature.
	 * 
	 * @param targetClass
	 * @param checkedForId identifier
	 * @param ontology
	 * @return true if there is an equivalence axiom which has the checkedFor class in its signature
	 */
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass, String checkedForId, OWLGraphWrapper ontology);
	
	/**
	 * Check if the target class has equivalence axioms, which use the checkedFor class in its signature.
	 * 
	 * @param targetClassId identifier
	 * @param checkedForId identifier
	 * @param ontology
	 * @return true if there is an equivalence axiom which has the checkedFor class in its signature
	 */
	public boolean containsClassInEquivalenceAxioms(String targetClassId, String checkedForId, OWLGraphWrapper ontology);
	
	/**
	 * Retrieve a set of equivalent classes for the given {@link OWLClass}.
	 * 
	 * @param cls
	 * @param ontology
	 * @return set of equivalent classes (never null)
	 */
	public Set<OWLClass> getEquivalentClasses(OWLClass cls, OWLGraphWrapper ontology);
	
	/**
	 * Retrieve a set of equivalent classes for the given identifier. 
	 * If no class can be found for the id, an empty set is returned.
	 * 
	 * @param id
	 * @param ontology
	 * @return set of equivalent classes (never null)
	 */
	public Set<OWLClass> getEquivalentClasses(String id, OWLGraphWrapper ontology);
	
	public static interface CheckResult {

		/**
		 * Status whether the check was successful.
		 * 
		 * @return boolean
		 */
		public boolean isGenus();

		/**
		 * Error, which can be returned in case of failure.
		 * 
		 * @return error or null
		 */
		public String error();
	}

	/**
	 * Retrieve the values for a template field
	 * 
	 * @param name name of the field
	 * @return values or null if not exists
	 */
	public String[] getInputs(String name);

	/**
	 * Retrieve the single value for a template field
	 * 
	 * @param name name of the field
	 * @return value or null if not exists
	 */
	public String getInput(String name);

	/**
	 * retrieve the name of a term
	 * 
	 * @param x term
	 * @param ontology the ontology to look the name up
	 * @return name
	 */
	public String name(OWLObject x, OWLGraphWrapper ontology);

	/**
	 * retrieve the name of a term
	 * 
	 * @param x term
	 * @param ontologies the ontologies to look the name up
	 * @return name
	 */
	public String name(OWLObject x, OWLGraphWrapper[] ontologies);

	/**
	 * create the ref name of a term: prepend 'a ' or 'an ' to the label
	 * 
	 * @param x
	 * @param ontology
	 * @return refname
	 */
	public String refname(OWLObject x, OWLGraphWrapper ontology);

	/**
	 * create the ref name of a term: prepend 'a ' or 'an ' to the label
	 * 
	 * @param x
	 * @param ontologies
	 * @return refname
	 */
	public String refname(OWLObject x, OWLGraphWrapper[] ontologies);

	/**
	 * Create a short info string for a term containing the label and id.
	 * 
	 * @param x
	 * @param ontology
	 * @return short info for a term
	 */
	public String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology);
	
	/**
	 * Create a short info string for a term id containing the label and id.
	 * 
	 * @param x
	 * @param ontology
	 * @return short info for a term
	 */
	public String getTermShortInfo(String x, OWLGraphWrapper ontology);
	
	/**
	 * Create a new definition by concatenation of term names using a prefix,
	 * infix, and suffix.
	 * 
	 * @param prefix
	 * @param terms
	 * @param ontology
	 * @param infix
	 * @param suffix
	 * @return definition string
	 */
	public String definition(String prefix,
			OWLObject[] terms,
			OWLGraphWrapper ontology,
			String infix,
			String suffix);

	/**
	 * Check if a value is in the array.
	 * 
	 * @param array
	 * @param value
	 * @return true if the collection contains the value
	 */
	public boolean contains(String[] array, String value);

	/**
	 * Convert the first character to upper case.
	 * 
	 * @param s
	 * @return string
	 */
	public String firstToUpperCase(String s);

	/**
	 * Concatenate two lists of the same type. Checks for null 
	 * values and ignores them.
	 * 
	 * @param <T>
	 * @param l1
	 * @param l2
	 * @return list
	 */
	public <T> List<T> concat(List<T> l1, List<T> l2);
	
	/**
	 * Create an error in the expected return format.
	 * 
	 * @param message
	 */
	public void error(String message);

}
