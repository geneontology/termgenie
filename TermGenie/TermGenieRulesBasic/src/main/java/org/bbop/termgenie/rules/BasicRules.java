package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.OntologyAware.Relation;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class BasicRules {
	
	protected OWLObject getTermSimple(String id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getOWLObjectByIdentifier(id);
		}
		return null;
	}
	
	protected String name(OWLObject x, OWLGraphWrapper...ontologies) {
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
	
	protected String refname(OWLObject x, OWLGraphWrapper ontology) {
		String name = name(x, ontology);
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
	
	protected String id(OWLObject x, OWLGraphWrapper...ontologies) {
		for (OWLGraphWrapper ontology : ontologies) {
			if (ontology != null) {
				String identifier = ontology.getIdentifier(x);
				if (identifier != null) {
					return identifier;
				}
			}
		}
		return null;
	}
	
	protected Set<String> synonyms(String prefix, OWLObject x, OWLGraphWrapper ontology, String suffix, String label) {
		String[] synonymStrings = getSynonyms(x, ontology);
		if (synonymStrings == null || synonymStrings.length == 0) {
			return null;
		}
		Set<String> synonyms = new HashSet<String>();
		for (String synonym : synonymStrings) {
			if (prefix != null) {
				synonym = prefix + synonym;
			}
			if (suffix != null) {
				synonym = synonym + suffix;
			}
			synonyms.add(synonym);
		}
		synonyms.remove(label);
		return synonyms;
	}
	
	protected Set<String> synonyms(String prefix, OWLObject x1, OWLGraphWrapper ontology1, String middle, OWLObject x2, OWLGraphWrapper ontology2, String suffix, String label) {
		String[] synonymStrings1 = getSynonyms(x1, ontology1);
		String[] synonymStrings2 = getSynonyms(x2, ontology2);
		boolean empty1 = synonymStrings1 == null || synonymStrings1.length == 0;
		boolean empty2 = synonymStrings2 == null || synonymStrings2.length == 0;
		if (empty1 && empty2) {
			// both do not have any synonyms
			return null;
		}
		
		List<String> synonyms1 = createSynList(x1, ontology1, synonymStrings1, empty1);
		List<String> synonyms2 = createSynList(x2, ontology2, synonymStrings2, empty2);
		
		Set<String> synonyms = new HashSet<String>();
		for (String synonym1 : synonyms1) {
			for (String synonym2 : synonyms2) {
				StringBuilder sb = new StringBuilder();
				if (prefix != null) {
					sb.append(prefix);
				}
				sb.append(synonym1);
				if (middle != null) {
					sb.append(middle);
				}
				sb.append(synonym2);
				if (suffix != null) {
					sb.append(suffix);
				}
				synonyms.add(sb.toString());
			}
		}
		synonyms.remove(label);
		return synonyms;
	}

	private List<String> createSynList(OWLObject x, OWLGraphWrapper ontology,
			String[] synonymStrings, boolean empty) {
		List<String> synonyms;
		
		String label = ontology.getLabel(x);
		
		if (empty) {
			// use label for synonym generation
			synonyms = Collections.singletonList(label);
		}
		else {
			synonyms = new ArrayList<String>(synonymStrings.length + 1);
			synonyms.addAll(Arrays.asList(synonymStrings));
			synonyms.add(label);
		}
		return synonyms;
	}
	
	@SuppressWarnings("deprecation")
	private String[] getSynonyms(OWLObject id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getSynonymStrings(id);
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
	
	protected boolean genus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology) {
		if (parent == null) {
			// TODO check if the term is in the ontology
			return true;
		}
		if (x.equals(parent)) {
			return true;
		}
		if (ontology != null) {
			Set<OWLObject> descendants = ontology.getDescendants(parent);
			if (descendants != null) {
				return descendants.contains(x);
			}
		}
		return false;
	}
	
	protected Set<OWLObject> getDescendants(OWLObject parent, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getDescendants(parent);
		}
		return null;
	}
	
	protected static List<TermGenerationOutput> error(String message, TermGenerationInput input) {
		TermGenerationOutput output = new TermGenerationOutput(null, input, false, message);
		return Collections.singletonList(output);
	}
	
	private static TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return new TermGenerationOutput(null, input, false, message);
	}

	protected static TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}
	
	protected boolean equals(TermTemplate t1, TermTemplate t2) {
		return t1.getName().equals(t2.getName());
	}
	
	protected List<String> getDefXref(TermGenerationInput input) {
		String[] strings =  getFieldStrings(input, "DefX_Ref");
		if (strings == null || strings.length == 0) {
			return null;
		}
		return Arrays.asList(strings);
	}

	private int getFieldPos(TermGenerationInput input, String name) {
		return input.getTermTemplate().getFieldPos(name);
	}
	
	private String[] getFieldStrings(TermGenerationInput input, String name) {
		int pos = getFieldPos(input, name);
		if (pos < 0) {
			return null;
		}
		String[][] matrix = input.getParameters().getStrings();
		if (matrix.length <= pos) {
			return null;
		}
		return matrix[pos];
	}
	
	private OntologyTerm[] getFieldTerms(TermGenerationInput input, String name) {
		int pos = getFieldPos(input, name);
		if (pos < 0) {
			return null;
		}
		OntologyTerm[][] matrix = input.getParameters().getTerms();
		if (matrix.length <= pos) {
			return null;
		}
		return matrix[pos];
	}
	
	protected String getFieldSingleString(TermGenerationInput input, String name) {
		String[] strings = getFieldStrings(input, name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return strings[0];
	}
	
	protected List<String> getFieldStringList(TermGenerationInput input, String name) {
		String[] strings = getFieldStrings(input, name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return Arrays.asList(strings);
	}
	
	protected OntologyTerm getFieldSingleTerm(TermGenerationInput input, String name) {
		OntologyTerm[] terms = getFieldTerms(input, name);
		if (terms == null || terms.length < 1) {
			return null;
		}
		return terms[0];
	}
	
	protected List<OntologyTerm> getFieldTermList(TermGenerationInput input, String name) {
		OntologyTerm[] terms = getFieldTerms(input, name);
		if (terms == null || terms.length < 1) {
			return null;
		}
		return Arrays.asList(terms);
	}
	
	
	protected String getComment(TermGenerationInput input) {
		return getFieldSingleString(input, "Comment");
	}

	private String createNewId(TermGenerationInput input, OWLGraphWrapper ontology) {
		// TODO use range, may be user specific
		return "GO:Random";
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
	
	protected List<TermGenerationOutput> createTermList(String label, String definition, Set<String> synonyms, String logicalDefinition, List<Relation> relations, TermGenerationInput input, OWLGraphWrapper ontology) {
		List<TermGenerationOutput> output = new ArrayList<TermGenerationOutput>(1);
		createTermList(label, definition, synonyms, logicalDefinition, relations, input, ontology, output);
		return output;
	}
	
	protected void createTermList(String label, String definition, Set<String> synonyms, String logicalDefinition, List<Relation> relations, TermGenerationInput input, OWLGraphWrapper ontology, List<TermGenerationOutput> output) {
		List<String> defxrefs = getDefXref(input);
		String comment = getComment(input);
		// Fact Checking
		// check label
		OWLObject sameName = ontology.getOWLObjectByLabel(label);
		if (sameName != null) {
			output.add(singleError("The term "+ontology.getIdentifier(sameName)+" with the same label already exists", input));
			return;
		}
		if (defxrefs != null) {
			// check xref conformity  
			for (String defxref : defxrefs) {
				// TODO add pattern check here for xrefs
			}
		}
		// try to create new id
		String id = createNewId(input, ontology);
		output.add(success(new OntologyTerm.DefaultOntologyTerm(id, label, definition, synonyms, logicalDefinition, defxrefs, comment, relations), input));
	}
	
	protected String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology) {
		return "\""+ontology.getLabel(x)+"\" ("+ontology.getIdentifier(x)+")";
	}
	
	protected static class CheckResult {
		protected boolean isGenus;
		List<TermGenerationOutput> error;
		
		/**
		 * @param error
		 */
		protected CheckResult(List<TermGenerationOutput> error) {
			this.isGenus = false;
			this.error = error;
		}

		protected CheckResult() {
			this.isGenus = true;
			this.error = null;
		}
		
		
	}
	
	private static CheckResult okay = new CheckResult();
	
	protected CheckResult checkGenus(OWLObject x, OWLObject branch, OWLGraphWrapper ontology, TermGenerationInput input) {
		if (!genus(x, branch, ontology)) {
			// check branch
			return new CheckResult(error("The specified term does not correspond to the patterns  The term "+getTermShortInfo(x, ontology)+" is not a descendant of "+getTermShortInfo(branch, ontology), input));
		}
		return okay;
	}
}