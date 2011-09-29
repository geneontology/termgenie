package org.bbop.termgenie.rules;

import java.util.List;

import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public interface TermGenieScriptFunctions extends TermGenieScriptFunctionsCDef {

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
	 * Create new synonyms for a given term with a prefix and suffix. The new
	 * label is required as it is used to prevent accidental creation of a
	 * synonym with the same label.
	 * 
	 * @param prefix the prefix, may be null
	 * @param x the term
	 * @param ontology the ontology to retrieve existing synonyms
	 * @param suffix the suffix, may be null
	 * @param label the label of the new term
	 * @return synonyms
	 */
	public List<Synonym> synonyms(String prefix,
			OWLObject x,
			OWLGraphWrapper ontology,
			String suffix,
			String label);

	/**
	 * Create new synonyms for two terms with a prefix, infix, and suffix. The
	 * new label is required as it is used to prevent accidental creation of a
	 * synonym with the same label.
	 * 
	 * @param prefix
	 * @param x1
	 * @param ontology1
	 * @param infix
	 * @param x2
	 * @param ontology2
	 * @param suffix
	 * @param label
	 * @return synonyms
	 */
	public List<Synonym> synonyms(String prefix,
			OWLObject x1,
			OWLGraphWrapper ontology1,
			String infix,
			OWLObject x2,
			OWLGraphWrapper ontology2,
			String suffix,
			String label);

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
