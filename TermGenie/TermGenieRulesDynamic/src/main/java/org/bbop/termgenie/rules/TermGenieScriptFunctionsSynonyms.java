package org.bbop.termgenie.rules;

import java.util.List;

import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

public interface TermGenieScriptFunctionsSynonyms {

	/**
	 * Create new synonyms for a given term with a prefix and suffix. The new
	 * label is required as it is used to prevent accidental creation of a
	 * synonym with the same label.
	 * 
	 * @param prefix the prefix, may be null
	 * @param x the term
	 * @param ontology the ontology to retrieve existing synonyms
	 * @param suffix the suffix, may be null
	 * @param defaultScope the scope for the new synonym, may be null.
	 * @param label the label of the new term
	 * @return synonyms
	 */
	public List<ISynonym> synonyms(String prefix,
			OWLObject x,
			OWLGraphWrapper ontology,
			String suffix,
			String defaultScope,
			String label);

	/**
	 * Create new synonyms for a given term with prefixes and suffixes. The new
	 * label is required as it is used to prevent accidental creation of a
	 * synonym with the same label.
	 * 
	 * @param prefixes the array prefixes, may not be null
	 * @param defaultScopes the scopes associated with the synonyms
	 * @param x the term
	 * @param ontology the ontology to retrieve existing synonyms
	 * @param suffixes the array suffixes, may be null
	 * @param label the label of the new term
	 * @return synonyms
	 */
	public List<ISynonym> synonyms(String[] prefixes,
			String[] defaultScopes,
			OWLObject x,
			OWLGraphWrapper ontology,
			String[] suffixes,
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
	 * @param defaultScope the scope for the new synonym, may be null.
	 * @param label
	 * @return synonyms
	 */
	public List<ISynonym> synonyms(String prefix,
			OWLObject x1,
			OWLGraphWrapper ontology1,
			String infix,
			OWLObject x2,
			OWLGraphWrapper ontology2,
			String suffix,
			String defaultScope,
			String label);

	/**
	 * Create new synonyms for two terms with a prefix, infix, and suffix. The
	 * new label is required as it is used to prevent accidental creation of a
	 * synonym with the same label. Special case: for more complex composition
	 * rules for synonyms, i.e. 'regulation_by'
	 * 
	 * @param prefix
	 * @param x1
	 * @param ontology1
	 * @param infix
	 * @param x2
	 * @param ontology2
	 * @param suffix
	 * @param defaultScope the scope for the new synonym, may be null.
	 * @param label
	 * @param requiredPrefixLeft required prefix for synonyms of term x1 (e.g.,
	 *            'regulation of ')
	 * @param ignoreSynonymsRight choose to ignore synonyms of x2 for
	 *            composition
	 * @return synonyms
	 */
	public List<ISynonym> synonyms(String prefix,
			OWLObject x1,
			OWLGraphWrapper ontology1,
			String infix,
			OWLObject x2,
			OWLGraphWrapper ontology2,
			String suffix,
			String defaultScope,
			String label,
			String requiredPrefixLeft,
			boolean ignoreSynonymsRight);

	/**
	 * Create new synonyms for a given list of terms with a prefix and suffix.
	 * The new label is required as it is used to prevent accidental creation of
	 * a synonym with the same label.
	 * 
	 * @param prefix the prefix, may be null
	 * @param terms the term list
	 * @param defaultScopes the default scopes for the new synonym, may be null.
	 * @param ontology the ontology to retrieve existing synonyms
	 * @param infix the infix between the synonym components
	 * @param suffix the suffix, may be null
	 * @param label the label of the new term
	 * @return synonyms
	 */
	public List<ISynonym> synonyms(String prefix,
			OWLObject[] terms,
			String[] defaultScopes,
			OWLGraphWrapper ontology,
			String infix,
			String suffix,
			String label);

	/**
	 * Create a new synonym and add it to the results list. If the result list
	 * is null create a new one.
	 * 
	 * @param label
	 * @param results
	 * @param prefix
	 * @param infix
	 * @param suffix
	 * @param scope
	 * @return modified list of synonyms
	 */
	public List<ISynonym> addSynonym(String label,
			List<ISynonym> results,
			String prefix,
			String infix,
			String suffix,
			String scope);

}
