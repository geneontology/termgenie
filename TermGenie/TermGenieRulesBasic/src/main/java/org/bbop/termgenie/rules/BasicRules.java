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
	
	protected final OWLGraphWrapper ontology;
	
	/**
	 * @param ontology
	 */
	protected BasicRules(OWLGraphWrapper ontology) {
		super();
		this.ontology = ontology;
	}

	protected OWLObject getTerm(String id) {
		return ontology.getOWLObjectByIdentifier(id);
	}
	
	protected String name(OWLObject x) {
		return ontology.getLabel(x);
	}
	
	protected String refname(OWLObject x) {
		String name = name(x);
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
	
	protected String id(OWLObject x) {
		return ontology.getIdentifier(x);
	}
	
	protected Set<String> synonyms(String prefix, OWLObject x, String suffix) {
		String[] synonymStrings = ontology.getSynonymStrings(x);
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
	
	protected Set<String> synonyms(String prefix, OWLObject x1, String middle, OWLObject x2, String suffix) {
		String[] synonymStrings1 = ontology.getSynonymStrings(x1);
		if (synonymStrings1 == null || synonymStrings1.length == 0) {
			return null;
		}
		String[] synonymStrings2 = ontology.getSynonymStrings(x2);
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