package org.bbop.termgenie.rules;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.OntologyAware.Relation;
import org.bbop.termgenie.core.OntologyAware.Synonym;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class BasicRules extends BasicTools {
	
	protected List<Synonym> synonyms(String prefix, OWLObject x, OWLGraphWrapper ontology, String suffix, String label) {
		List<Synonym> synonyms = getSynonyms(x, ontology);
		if (synonyms == null || synonyms.isEmpty()) {
			return null;
		}
		List<Synonym> results = new ArrayList<Synonym>();
		for (Synonym synonym : synonyms) {
			StringBuilder sb = new StringBuilder();
			if (prefix != null) {
				sb.append(prefix);
			}
			sb.append(synonym.getLabel());
			if (suffix != null) {
				sb.append(suffix);
			}
			addSynonym(results, synonym, sb.toString(), label);
		}
		return results;
	}

	protected List<Synonym> synonyms(String prefix, OWLObject x1, OWLGraphWrapper ontology1, String middle, OWLObject x2, OWLGraphWrapper ontology2, String suffix, String label) {
		List<Synonym> synonyms1 = getSynonyms(x1, ontology1);
		List<Synonym> synonyms2 = getSynonyms(x2, ontology2);
		boolean empty1 = synonyms1 == null || synonyms1.isEmpty();
		boolean empty2 = synonyms2 == null || synonyms2.isEmpty();
		if (empty1 && empty2) {
			// do nothing, as both do not have any synonyms
			return null;
		}
		synonyms1 = addLabel(x1, ontology1, synonyms1);
		synonyms2 = addLabel(x2, ontology2, synonyms2);
		
		List<Synonym> results = new ArrayList<Synonym>();
		for (Synonym synonym1 : synonyms1) {
			for (Synonym synonym2 : synonyms2) {
				if (equalScope(synonym1, synonym2)) {
					StringBuilder sb = new StringBuilder();
					if (prefix != null) {
						sb.append(prefix);
					}
					sb.append(synonym1.getLabel());
					if (middle != null) {
						sb.append(middle);
					}
					sb.append(synonym2.getLabel());
					if (suffix != null) {
						sb.append(suffix);
					}
					addSynonym(results, synonym1, sb.toString(), label);
				}
			}
		}
		return results;
	}
	
	protected boolean equalScope(Synonym s1, Synonym s2) {
		String scope1 = s1.getScope();
		String scope2 = s2.getScope();
		if (scope1 == scope2) {
			// intented: true if both are null
			return true;
		}
		if (scope1 != null) {
			return scope1.equals(scope2);
		}
		return false;
	}

	private List<Synonym> addLabel(OWLObject x, OWLGraphWrapper ontology, List<Synonym> synonyms) {
		String label = ontology.getLabel(x);
		synonyms.add(new Synonym(label, null, null, null));
		return synonyms;
	}
	
	void addSynonym(List<Synonym> results, Synonym synonym, String newLabel, String label) {
		if (!newLabel.equals(label)) {
			// if by any chance a synonym has the same label it is ignored
			List<String> xrefs = addXref("GOC:TermGenie", synonym.getXrefs());
			results.add(new Synonym(newLabel, synonym.getScope(), synonym.getCategory(), xrefs));
		}
	}
	
	private List<String> addXref(String xref, List<String> xrefs) {
		if (xref == null) {
			return xrefs;
		}
		if (xrefs == null) {
			ArrayList<String> list = new ArrayList<String>(1);
			list.add(xref);
			return list;
		}
		if (!xrefs.contains(xref)) {
			xrefs.add(xref);
			return xrefs;
		}
		return xrefs;
	}

	private List<Synonym> getSynonyms(OWLObject id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			// TODO use scope, category, and xref for synonyms
			String[] synonymStrings = ontology.getSynonymStrings(id);
			if (synonymStrings != null && synonymStrings.length > 0) {
				List<Synonym> result = new ArrayList<Synonym>(synonymStrings.length);
				for (String synonymString : synonymStrings) {
					result.add(new Synonym(synonymString, null, null, null));
				}
				return result;
			}
		}
		return null;
	}
	
	protected String createCDef(String prefix, OWLObject x, OWLGraphWrapper ontology, String infix, String suffix) {
		return createCDef(prefix, Collections.singletonList(x), ontology, infix, null, suffix);
	}
	
	protected String createCDef(String prefix, List<OWLObject> list, OWLGraphWrapper ontology, String type, String separator, String suffix) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		for (int i = 0; i < list.size(); i++) {
			OWLObject x = list.get(i);
			if (i > 0) {
				sb.append(separator);
			}
			if (type != null) {
				sb.append(type);
			}
			sb.append(ontology.getIdentifier(x));
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}
	
	protected String createDefinition(String prefix, List<OWLObject> list, OWLGraphWrapper ontology, String infix, String suffix, TermGenerationInput input) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		for (int i = 0; i < list.size(); i++) {
			OWLObject x = list.get(i);
			if (i > 0 && infix != null) {
				sb.append(infix);
			}
			sb.append(refname(x, ontology));
		}
		
		if (suffix != null) {
			sb.append(suffix);
		}
		return createDefinition(sb.toString(), input);
	}
	
	protected String createDefinition(String definition, TermGenerationInput input) {
		String inputDefinition = getFieldSingleString(input, "Definition");
		if (inputDefinition != null) {
			inputDefinition = inputDefinition.trim(); 
			if (inputDefinition.length() > 1) {
				return inputDefinition;
			}
		}
		return definition;
	}
	
	protected String createName(String name, TermGenerationInput input) {
		String inputName = getFieldSingleString(input, "Name");
		if (inputName != null) {
			inputName = inputName.trim(); 
			if (inputName.length() > 1) {
				return inputName;
			}
		}
		return name;
	}
	
	protected static class CDef {
		final OWLObject genus;
		final OWLGraphWrapper ontology;
		
		final List<String> differentiumRelations = new ArrayList<String>();
		final List<OWLObject[]> differentiumTerms = new ArrayList<OWLObject[]>();
		final List<OWLGraphWrapper[]> differentiumOntologies = new ArrayList<OWLGraphWrapper[]>();
		
		final List<String> properties = new ArrayList<String>();
		/**
		 * @param genus
		 * @param ontology
		 */
		protected CDef(OWLObject genus, OWLGraphWrapper ontology) {
			super();
			this.genus = genus;
			this.ontology = ontology;
		}
		
		protected void addDifferentium(String rel, OWLObject term, OWLGraphWrapper...ontologies) {
			addDifferentium(rel, Collections.singletonList(term), ontologies);
		}
		
		protected void addDifferentium(String rel, List<OWLObject> terms, OWLGraphWrapper...ontologies) {
			differentiumRelations.add(rel);
			differentiumTerms.add(terms.toArray(new OWLObject[terms.size()]));
			differentiumOntologies.add(ontologies);
		}
		
		protected void addProperty(String property) {
			properties.add(property);
		}
	}
	
	protected List<TermGenerationOutput> createTermList(String label, String definition, List<Synonym> synonyms, CDef logicalDefinition, TermGenerationInput input, OWLGraphWrapper ontology) {
		List<TermGenerationOutput> output = new ArrayList<TermGenerationOutput>(1);
		createTermList(label, definition, synonyms, logicalDefinition, input, ontology, output);
		return output;
	}
	
	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");
	
	protected void createTermList(String label, String definition, List<Synonym> synonyms, CDef logicalDefinition, TermGenerationInput input, OWLGraphWrapper ontology, List<TermGenerationOutput> output) {
		// Fact Checking
		// check label
		OWLObject sameName = ontology.getOWLObjectByLabel(label);
		if (sameName != null) {
			output.add(singleError("The term "+ontology.getIdentifier(sameName)+" with the same label already exists", input));
			return;
		}
		
		// def xref
		List<String> defxrefs = getDefXref(input);
		if (defxrefs != null) {
			// check xref conformity
			boolean hasXRef = false;
			for (String defxref : defxrefs) {
				// check if the termgenie def_xref is already in the list
				hasXRef = hasXRef || defxref.equals("GOC:TermGenie");
				
				// simple defxref check, TODO use a centralized qc check.
				if (defxref.length() < 3) {
					output.add(singleError("The Def_Xref "+defxref+" is too short. A Def_Xref consists of a prefix and suffix with a colon (:) as separator", input));
					continue;
				}
				if(!def_xref_Pattern.matcher(defxref).matches()) {
					output.add(singleError("The Def_Xref "+defxref+" does not conform to the expected pattern. A Def_Xref consists of a prefix and suffix with a colon (:) as separator and contains no whitespaces.", input));
				}
			}
			if (!hasXRef) {
				// add the termgenie def_xref
				ArrayList<String> newlist = new ArrayList<String>(defxrefs.size() + 1);
				newlist.addAll(defxrefs);
				newlist.add("GOC:TermGenie");
				defxrefs = newlist;
			}
		}
		else {
			defxrefs = Collections.singletonList("GOC:TermGenie");
		}
		
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put("creation_date", getDate());
		metaData.put("created_by", "TermGenie");
		metaData.put("resource", ontology.getOntologyId());
		metaData.put("comment", getComment(input));

		// TODO use cdef to create relationships (differentium, intersection, restriction, ...)
		List<Relation> relations = null;
		
		DefaultOntologyTerm term = new DefaultOntologyTerm(null, label, definition, synonyms, defxrefs, metaData, relations);
		
		output.add(success(term, input));
	}
	
	private final static DateFormat df = new ISO8601DateFormat();
	
	private String getDate() {
		return df.format(new Date());
	}
	
}