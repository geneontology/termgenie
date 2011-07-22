package org.bbop.termgenie.rules;

import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

public interface TermGenieScriptFunctions {

	/**
	 * Retrieve a term given the template field name.
	 * 
	 * @param name of template field
	 * @param ontologies the ontologies to search in for the id extracted from the field
	 * @return term or null
	 */
	public OWLObject getSingleTerm(String name, OWLGraphWrapper...ontologies);
	
	/**
	 * Retrieve all terms as list from given the template field.
	 * 
	 * @param name of template field
	 * @param ontologies the ontologies to search in for the id extracted from the field
	 * @return term or null
	 */
	public OWLObject[] getTerms(String name, OWLGraphWrapper ontology);
	
	/**
	 * @param x the term
	 * @param parent the parent term
	 * @param ontology the ontology used for relations
	 * @return check result
	 * 
	 * @see CheckResult
	 */
	public CheckResult checkGenus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology);
	
	/**
	 * @param x the term
	 * @param parent the parent id
	 * @param ontology the ontology used for relations
	 * @return check result
	 * 
	 * @see CheckResult
	 */
	public CheckResult checkGenus(OWLObject x, String parent, OWLGraphWrapper ontology);
	
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
		public List<TermGenerationOutput> error();
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
	 * @param ontologies the ontologies to look the name up
	 * @return name
	 */
	public String name(OWLObject x, OWLGraphWrapper...ontologies);
	
	
	/**
	 * create the ref name of a term: prepend 'a ' or 'an ' to the label
	 * 
	 * @param x
	 * @param ontology
	 * @return refname
	 */
	public String refname(OWLObject x, OWLGraphWrapper ontology);
	
	/**
	 * Create new synonyms for a given term with a prefix and suffix.
	 * The new label is required as it is used to prevent accidential 
	 * creation of a synonym with the same label.
	 *  
	 * @param prefix the prefix, may be null
	 * @param x the term
	 * @param ontology the ontology to retrieve existing synonyms
	 * @param suffix the suffix, may be null
	 * @param label the label of the new term
	 * @return synonyms
	 */
	public List<Synonym> synonyms(String prefix, OWLObject x, OWLGraphWrapper ontology, String suffix, String label);

	/**
	 * Create new synonyms for two terms with a prefix, infix, and suffix.
	 * The new label is required as it is used to prevent accidential 
	 * creation of a synonym with the same label.
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
	public List<Synonym> synonyms(String prefix, OWLObject x1, OWLGraphWrapper ontology1, String infix, OWLObject x2, OWLGraphWrapper ontology2, String suffix, String label);
	
	/**
	 * Create a new definition by concatenation of term names using 
	 * a prefix, infix, and suffix. 
	 * 
	 * @param prefix
	 * @param terms
	 * @param ontology
	 * @param infix
	 * @param suffix
	 * @return definition string
	 */
	public String definition(String prefix, OWLObject[] terms, OWLGraphWrapper ontology, String infix, String suffix);
	
	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus
	 * @param ontology
	 * @return cdef
	 * 
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(OWLObject genus, OWLGraphWrapper ontology);
	
	/**
	 * Create a logical definition for a term in form of a {@link CDef}.
	 * 
	 * @param genus id
	 * @param ontology
	 * @return cdef
	 * 
	 * @see CDef for adding differentia and properties
	 */
	public CDef cdef(String genus, OWLGraphWrapper ontology);
	
	/**
	 * Locigal defintion used for relation generation in term genie.
	 */
	public static interface CDef {
		
		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param term term
		 * @param ontologies the ontologies to look the terms up
		 */
		public void differentium(String rel, OWLObject term, OWLGraphWrapper...ontologies);
		
		/**
		 * Add a differentium to this cdef.
		 * 
		 * @param rel relation
		 * @param terms terms
		 * @param ontologies the ontologies to look the terms up
		 */
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper...ontologies);
		
		/**
		 * Add a property to this cdef.
		 * 
		 * @param property
		 */
		public void property(String property);
	}
	
	
	//--------------------------
	
	/**
	 * Create a new term and provide output which can directly be returned.
	 * 
	 * @param label
	 * @param definition
	 * @param synonyms
	 * @param logicalDefinition
	 * @param ontology
	 * @return output the output which can be returned.
	 */
	public List<TermGenerationOutput> createTerm(String label, String definition, List<Synonym> synonyms, CDef logicalDefinition, OWLGraphWrapper ontology);
	
	/**
	 * Create a new term and add it to the result output.
	 * Create the output list with {@link #createList()}.
	 * 
	 * @param label
	 * @param definition
	 * @param synonyms
	 * @param logicalDefinition
	 * @param ontology
	 * @param output the list the new term is added to.
	 */
	public void addTerm(String label, String definition, List<Synonym> synonyms, CDef logicalDefinition, OWLGraphWrapper ontology, List<TermGenerationOutput> output);
	
	/**
	 * Create an empty list to filled with results.
	 * Use in conjunction with {@link #addTerm(String, String, List, CDef, OWLGraphWrapper, List)}.
	 * 
	 * @return result list
	 */
	public List<TermGenerationOutput> createList();
	
	/**
	 * Check if a value is in the collection.
	 * 
	 * @param collection
	 * @param value
	 * @return true if hte collection contains the value
	 */
	public boolean contains(Collection<String> collection, String value);
	
	/**
	 * Create an error in the expected return format.
	 * 
	 * @param message
	 * @return error message in the proper format
	 */
	public List<TermGenerationOutput> error(String message);
	
}
