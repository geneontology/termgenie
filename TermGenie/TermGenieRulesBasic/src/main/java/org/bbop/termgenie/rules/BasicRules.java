package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
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
	
	protected Set<String> synonyms(String prefix, OWLObject x, OWLGraphWrapper ontology, String suffix) {
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
		return synonyms;
	}
	
	protected Set<String> synonyms(String prefix, OWLObject x1, OWLGraphWrapper ontology1, String middle, OWLObject x2, OWLGraphWrapper ontology2, String suffix) {
		String[] synonymStrings1 = getSynonyms(x1, ontology1);
		if (synonymStrings1 == null || synonymStrings1.length == 0) {
			return null;
		}
		String[] synonymStrings2 = getSynonyms(x2, ontology2);
		if (synonymStrings2 == null || synonymStrings2.length == 0) {
			return null;
		}
		Set<String> synonyms = new HashSet<String>();
		for (String synonym1 : synonymStrings1) {
			for (String synonym2 : synonymStrings2) {
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
		return synonyms;
	}
	
	@SuppressWarnings("deprecation")
	private String[] getSynonyms(OWLObject id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getSynonymStrings(id);
		}
		return null;
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
		return input.getParameters().getStrings().getValues("DefX_Ref");
	}
	
	protected String getComment(TermGenerationInput input) {
		return input.getParameters().getStrings().getValue("Comment", 0);
	}

	private String createNewId(TermGenerationInput input, OWLGraphWrapper ontology) {
		// TODO use range, may be user specific
		return "GO:Random";
	}
	
	protected String createDefinition(String definition, TermGenerationInput input) {
		String inputDefinition = input.getParameters().getStrings().getValue("Definition", 0);
		if (inputDefinition != null) {
			inputDefinition = inputDefinition.trim(); 
			if (inputDefinition.length() > 1) {
				return inputDefinition;
			}
		}
		return definition;
	}
	
	protected String createName(String name, TermGenerationInput input) {
		String inputName = input.getParameters().getStrings().getValue("Name", 0);
		if (inputName != null) {
			inputName = inputName.trim(); 
			if (inputName.length() > 1) {
				return inputName;
			}
		}
		return name;
	}
	
	protected List<TermGenerationOutput> createTermList(String label, String definition, Set<String> synonyms, String logicalDefinition, TermGenerationInput input, OWLGraphWrapper ontology) {
		List<TermGenerationOutput> output = new ArrayList<TermGenerationOutput>(1);
		createTermList(label, definition, synonyms, logicalDefinition, input, ontology, output);
		return output;
	}
	
	protected void createTermList(String label, String definition, Set<String> synonyms, String logicalDefinition, TermGenerationInput input, OWLGraphWrapper ontology, List<TermGenerationOutput> output) {
		List<String> defxrefs = getDefXref(input);
		String comment = getComment(input);
		// Fact Checking
		// check label
		OWLObject sameName = ontology.getOWLObjectByLabel(label);
		if (sameName != null) {
			output.add(singleError("The term "+ontology.getIdentifier(sameName)+" with the same label already exists", input));
			return;
		}
		// check xref conformity  
		for (String defxref : defxrefs) {
			// TODO add pattern check here for xrefs
		}

		// try to create new id
		String id = createNewId(input, ontology);
		output.add(success(new OntologyTerm.DefaultOntologyTerm(id, label, definition, synonyms, logicalDefinition, defxrefs, comment), input));
	}
}