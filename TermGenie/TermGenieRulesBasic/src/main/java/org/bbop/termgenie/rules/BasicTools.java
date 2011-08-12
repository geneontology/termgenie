package org.bbop.termgenie.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class BasicTools {

	private final ReasonerFactory factory;

	/**
	 * @param factory
	 */
	protected BasicTools(ReasonerFactory factory) {
		super();
		this.factory = factory;
	}

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
		return starts_with_vowl(name) ? "an " + name : "a " + name;
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

	protected boolean genus(OWLObject x, OWLObject parent, OWLGraphWrapper ontology) {
		if (parent == null) {
			// TODO check if the term is in the ontology
			return true;
		}
		if (x.equals(parent)) {
			return true;
		}
		if (ontology != null) {
			ReasonerTaskManager manager = factory.getDefaultTaskManager(ontology);
			Collection<OWLObject> ancestors = manager.getAncestors(x, ontology);
			if (ancestors != null) {
				return ancestors.contains(parent);
			}
		}
		return false;
	}

	protected static List<TermGenerationOutput> error(String message, TermGenerationInput input) {
		TermGenerationOutput output = new TermGenerationOutput(null, input, false, message);
		return Collections.singletonList(output);
	}

	protected static TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return new TermGenerationOutput(null, input, false, message);
	}

	protected static TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}

	protected boolean equals(TermTemplate t1, TermTemplate t2) {
		return t1.getName().equals(t2.getName());
	}

	protected List<String> getDefXref(TermGenerationInput input) {
		String[] strings = getFieldStrings(input, "DefX_Ref");
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

	protected String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology) {
		return "\"" + ontology.getLabel(x) + "\" (" + ontology.getIdentifier(x) + ")";
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

	protected CheckResult checkGenus(OWLObject x,
			OWLObject branch,
			OWLGraphWrapper ontology,
			TermGenerationInput input)
	{
		if (!genus(x, branch, ontology)) {
			// check branch
			return new CheckResult(error("The specified term does not correspond to the patterns  The term " + getTermShortInfo(branch,
					ontology) + " is not a parent of " + getTermShortInfo(x, ontology),
					input));
		}
		return okay;
	}
}
