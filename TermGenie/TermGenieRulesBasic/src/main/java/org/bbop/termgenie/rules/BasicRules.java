package org.bbop.termgenie.rules;

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
	
	protected OWLObject getTerm(String id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getOWLObjectByIdentifier(id);
		}
		return null;
	}
	
	protected String name(OWLObject x, OWLGraphWrapper ontology) {
		if (ontology != null) {
			return ontology.getLabel(x);
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
	
	protected String id(OWLObject x, OWLGraphWrapper ontology) {
		return ontology.getIdentifier(x);
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
	
	protected String createNewId() {
		return "GO:Random";
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
		TermGenerationOutput output = new TermGenerationOutput(null, input, false, "Cannot create 'regulation of non biological process X' term");
		return Collections.singletonList(output);
	}

	protected static TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}
	
	protected boolean equals(TermTemplate t1, TermTemplate t2) {
		return t1.getName().equals(t2.getName());
	}
}