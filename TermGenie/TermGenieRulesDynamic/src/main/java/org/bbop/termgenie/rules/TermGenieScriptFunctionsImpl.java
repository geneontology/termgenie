package org.bbop.termgenie.rules;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.helpers.ISO8601DateFormat;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.OntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * An implementation of functions for the termgenie scripting environment. 
 * Hiding the results in an internal variable, allows type safe retrieval.
 */
public class TermGenieScriptFunctionsImpl implements TermGenieScriptFunctions {

	private final TermGenerationInput input;
	private List<TermGenerationOutput> result;
	
	/**
	 * @param input
	 */
	TermGenieScriptFunctionsImpl(TermGenerationInput input) {
		super();
		this.input = input;
	}

	@Override
	public OWLObject getSingleTerm(String name, OWLGraphWrapper ontology) {
		return getSingleTerm(name, new OWLGraphWrapper[]{ontology});
	}
	
	@Override
	public OWLObject getSingleTerm(String name, OWLGraphWrapper[] ontologies) {
		String id = getFieldSingleTerm(name).getId(); 
		for (OWLGraphWrapper ontology : ontologies) {
			if (ontology != null) {
				OWLObject x = getTermSimple(id, ontology);
				if (x != null) {
					return x;
				}
			}
		}
		return null;
	}
	
	private OWLObject getTermSimple(String id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getOWLObjectByIdentifier(id);
		}
		return null;
	}
	
	private OntologyTerm getFieldSingleTerm(String name) {
		OntologyTerm[] terms = getFieldTerms(name);
		if (terms == null || terms.length < 1) {
			return null;
		}
		return terms[0];
	}
	
	private OntologyTerm[] getFieldTerms(String name) {
		int pos = getFieldPos(name);
		if (pos < 0) {
			return null;
		}
		OntologyTerm[][] matrix = input.getParameters().getTerms();
		if (matrix.length <= pos) {
			return null;
		}
		return matrix[pos];
	}
	
	private int getFieldPos(String name) {
		return input.getTermTemplate().getFieldPos(name);
	}
	
	@Override
	public OWLObject[] getTerms(String name, OWLGraphWrapper ontology) {
		OntologyTerm[] terms = getFieldTerms(name);
		if (terms == null || terms.length == 0) {
			return new OWLObject[0];
		}
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (OntologyTerm term : terms) {
			if (term != null) {
				OWLObject x = getTermSimple(term.getId(), ontology);
				if (x != null) {
					result.add(x);
				}
			}
		}
		return result.toArray(new OWLObject[result.size()]);
	}

	private static final CheckResult okay = new CheckResult() {
		
		@Override
		public boolean isGenus() {
			return true;
		}
		
		@Override
		public String error() {
			return null;
		}
	};
	
	@Override
	public CheckResult checkGenus(OWLObject x, String parentId, OWLGraphWrapper ontology) {
		OWLObject parent = ontology.getOWLObjectByIdentifier(parentId);
		return checkGenus(x, parent, ontology);
	}
	
	@Override
	public CheckResult checkGenus(final OWLObject x, final OWLObject parent, final OWLGraphWrapper ontology) {
		if (!genus(x, parent, ontology)) {
			// check branch
			
			StringBuilder sb = new StringBuilder();
			sb.append("The specified term does not correspond to the patterns  The term ");
			sb.append(getTermShortInfo(parent, ontology));
			sb.append(" is not a parent of ");
			sb.append(getTermShortInfo(x, ontology));
			final String error = sb.toString();
			
			return new CheckResult(){

				@Override
				public boolean isGenus() {
					return false;
				}

				@Override
				public String error() {
					return error;
				}
			};
		}
		return okay;
	}
	
	@Override
	public boolean genus(OWLObject x, String parent, OWLGraphWrapper ontology) {
		return genus(x, ontology.getOWLObjectByIdentifier(parent), ontology);
	}

	@Override
	public boolean genus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology) {
		if (parent == null) {
			// TODO check if the term is in the ontology
			return true;
		}
		if (x.equals(parent)) {
			return true;
		}
		if (ontology != null) {
			ReasonerTaskManager manager = ReasonerFactory.getDefaultTaskManager(ontology);
			Collection<OWLObject> ancestors = manager.getAncestors(x, ontology);
			if (ancestors != null) {
				return ancestors.contains(parent);
			}
		}
		return false;
	}
	
	private String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology) {
		return "\""+ontology.getLabel(x)+"\" ("+ontology.getIdentifier(x)+")";
	}

	@Override
	public String[] getInputs(String name) {
		String[] strings = getFieldStrings(name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return strings;
	}

	@Override
	public String getInput(String name) {
		String[] strings = getFieldStrings(name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return strings[0];
	}

	private String[] getFieldStrings(String name) {
		int pos = getFieldPos(name);
		if (pos < 0) {
			return null;
		}
		String[][] matrix = input.getParameters().getStrings();
		if (matrix.length <= pos) {
			return null;
		}
		return matrix[pos];
	}
	
	@Override
	public String name(OWLObject x, OWLGraphWrapper ontology) {
		return name(x, new OWLGraphWrapper[]{ontology});
	}
	
	@Override
	public String name(OWLObject x, OWLGraphWrapper[] ontologies) {
		for (OWLGraphWrapper ontology : ontologies) {
			if (ontology != null) {
				String label = ontology.getLabel(x);
				if (label != null) {
					return label;
				}
			}	
		}
		return null;
	}

	@Override
	public String definition(String prefix, OWLObject[] terms, OWLGraphWrapper ontology, String infix, String suffix) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		for (int i = 0; i < terms.length; i++) {
			OWLObject x = terms[i];
			if (i > 0 && infix != null) {
				sb.append(infix);
			}
			sb.append(refname(x, ontology));
		}
		
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}
	
	@Override
	public String refname(OWLObject x, OWLGraphWrapper ontology) {
		return refname(x, new OWLGraphWrapper[]{ontology});
	}
	
	@Override
	public String refname(OWLObject x, OWLGraphWrapper[] ontologies) {
		String name = name(x, ontologies);
		return starts_with_vowl(name) ? "an "+name : "a "+name;
	}
	
	private boolean starts_with_vowl(String name) {
		char c = Character.toLowerCase(name.charAt(0));
		switch (c) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return true;
		}
		return false;
	}
	
	@Override
	public List<Synonym> synonyms(String prefix, OWLObject x, OWLGraphWrapper ontology,
			String suffix, String label) {
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

	@Override
	public List<Synonym> synonyms(String prefix, OWLObject x1, OWLGraphWrapper ontology1,
			String infix, OWLObject x2, OWLGraphWrapper ontology2, String suffix, String label) {
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
					if (infix != null) {
						sb.append(infix);
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
			Set<String> xrefs = addXref("GOC:TermGenie", synonym.getXrefs());
			results.add(new Synonym(newLabel, synonym.getScope(), synonym.getCategory(), xrefs));
		}
	}
	
	private Set<String> addXref(String xref, Set<String> xrefs) {
		if (xref == null) {
			return xrefs;
		}
		if (xrefs == null) {
			HashSet<String> list = new HashSet<String>(1);
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
			List<Synonym> oboSynonyms = ontology.getOBOSynonyms(id);
			if (oboSynonyms != null && !oboSynonyms.isEmpty()) {
				// defensive copy
				oboSynonyms = new ArrayList<Synonym>(oboSynonyms);
			}
			return oboSynonyms;
		}
		return null;
	}
	
	private static class CDefImpl implements CDef {
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
		protected CDefImpl(OWLObject genus, OWLGraphWrapper ontology) {
			super();
			this.genus = genus;
			this.ontology = ontology;
		}
		
		@Override
		public void differentium(String rel, OWLObject term, OWLGraphWrapper[] ontologies) {
			differentium(rel, new OWLObject[]{term}, ontologies);
		}
		
		@Override
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper[] ontologies) {
			differentiumRelations.add(rel);
			differentiumTerms.add(terms);
			differentiumOntologies.add(ontologies);
		}
		
		@Override
		public void differentium(String rel, OWLObject term, OWLGraphWrapper ontology) {
			differentium(rel, new OWLObject[]{term}, new OWLGraphWrapper[]{ontology});
		}

		@Override
		public void differentium(String rel, OWLObject[] terms, OWLGraphWrapper ontology) {
			differentium(rel, terms, new OWLGraphWrapper[]{ontology});
		}

		@Override
		public void property(String property) {
			properties.add(property);
		}
	}
	
	@Override
	public CDef cdef(OWLObject genus, OWLGraphWrapper ontology) {
		return new CDefImpl(genus, ontology);
	}
	
	@Override
	public CDef cdef(String id, OWLGraphWrapper ontology) {
		OWLObject genus = ontology.getOWLObjectByIdentifier(id);
		return new CDefImpl(genus, ontology);
	}

	@Override
	public synchronized void createTerm(String label, String definition,
			List<Synonym> synonyms, CDef logicalDefinition, OWLGraphWrapper ontology) {
		if (result == null) {
			result = new ArrayList<TermGenerationOutput>(1);	
		}
		addTerm(label, definition, synonyms, logicalDefinition, ontology, result);
	}

	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");
	
	private void addTerm(String label, String definition, List<Synonym> synonyms,
			CDef logicalDefinition, OWLGraphWrapper ontology, List<TermGenerationOutput> output) {
		
		// get overwrites from GUI
		String inputName = getInput("Name");
		if (inputName != null) {
			inputName = inputName.trim(); 
			if (inputName.length() > 1) {
				label = inputName;
			}
		}
		
		String inputDefinition = getInput("Definition");
		if (inputDefinition != null) {
			inputDefinition = inputDefinition.trim(); 
			if (inputDefinition.length() > 1) {
				definition = inputDefinition;
			}
		}
		
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
		metaData.put("comment", getInput("Comment"));

		// TODO use cdef to create relationships (differentium, intersection, restriction, ...)
		List<Relation> relations = null;
		
		DefaultOntologyTerm term = new DefaultOntologyTerm(null, label, definition, synonyms, defxrefs, metaData, relations);
		
		output.add(success(term, input));

	}
	
	private List<String> getDefXref(TermGenerationInput input) {
		String[] strings =  getInputs("DefX_Ref");
		if (strings == null || strings.length == 0) {
			return null;
		}
		return Arrays.asList(strings);
	}
	
	private final static DateFormat df = new ISO8601DateFormat();
	
	private String getDate() {
		return df.format(new Date());
	}
	
	private static TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return new TermGenerationOutput(null, input, false, message);
	}

	private static TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}
	
	@Override
	public boolean contains(String[] array, String value) {
		if (array != null && array.length > 0) {
			for (String string : array) {
				if (value == null) {
					if (string == null) {
						return true;
					}
				} else if (value.equals(string)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized void error(String message) {
		TermGenerationOutput error = createError(message);
		if (result == null) {
			result = new ArrayList<TermGenerationOutput>(1);
		}
		result.add(error);
	}

	protected TermGenerationOutput createError(String message) {
		TermGenerationOutput error = new TermGenerationOutput(null, input, false, message);
		return error;
	}
	
	public List<TermGenerationOutput> getResult() {
		return result;
	}

}
