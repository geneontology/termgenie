package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;

/**
 * Abstract implementation of functions for the TermGenie scripting environment.
 * Hiding the results in an internal variable, allows type safe retrieval.
 * 
 * @param <T>
 */
public abstract class AbstractTermGenieScriptFunctionsImpl<T> extends SynonymGenerationTools implements
		TermGenieScriptFunctions,
		ChangeTracker
{

	protected final AbstractTermCreationTools<T> tools;
	private List<TermGenerationOutput> result;

	/**
	 * @param input
	 * @param targetOntology
	 * @param auxiliaryOntologies
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 * @param state 
	 */
	AbstractTermGenieScriptFunctionsImpl(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory,
			ProcessState state)
	{
		super();
		tools = createTermCreationTool(input,
				targetOntology,
				auxiliaryOntologies,
				tempIdPrefix,
				patternID,
				factory,
				state);
	}

	protected abstract AbstractTermCreationTools<T> createTermCreationTool(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory,
			ProcessState state);

	protected synchronized List<TermGenerationOutput> getResultList() {
		if (result == null) {
			result = new ArrayList<TermGenerationOutput>(3);
		}
		return result;
	}

	@Override
	public OWLObject getSingleTerm(String name, OWLGraphWrapper ontology) {
		return getSingleTerm(name, new OWLGraphWrapper[] { ontology });
	}

	@Override
	public OWLObject getSingleTerm(String name, OWLGraphWrapper[] ontologies) {
		String id = getFieldSingleTerm(name);
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
			OWLObject x = ontology.getOWLObjectByIdentifier(id);
			if (ontology.getLabel(x) != null) {
				return x;
			}
		}
		return null;
	}

	private String getFieldSingleTerm(String name) {
		List<String> terms = getFieldTerms(name);
		if (terms == null || terms.isEmpty()) {
			return null;
		}
		return terms.get(0);
	}

	private List<String> getFieldTerms(String name) {
		Map<String, List<String>> terms = tools.input.getParameters().getTerms();
		if (terms != null) {
			return terms.get(name);
		}
		return null;
	}

	@Override
	public OWLObject[] getTerms(String name, OWLGraphWrapper ontology) {
		List<String> terms = getFieldTerms(name);
		if (terms == null || terms.isEmpty()) {
			return new OWLObject[0];
		}
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (String term : terms) {
			if (term != null) {
				OWLObject x = getTermSimple(term, ontology);
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
	public CheckResult checkGenus(final OWLObject x,
			final OWLObject parent,
			final OWLGraphWrapper ontology)
	{
		if (!genus(x, parent, ontology)) {
			// check branch

			StringBuilder sb = new StringBuilder();
			sb.append("The specified term does not correspond to the patterns  The term ");
			sb.append(getTermShortInfo(parent, ontology));
			sb.append(" is not a parent of ");
			sb.append(getTermShortInfo(x, ontology));
			final String error = sb.toString();

			return new CheckResult() {

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
			ReasonerTaskManager manager = tools.factory.getDefaultTaskManager(ontology);
			Collection<OWLObject> ancestors = manager.getAncestors(x, ontology);
			if (ancestors != null) {
				return ancestors.contains(parent);
			}
		}
		return false;
	}

	@Override
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass,
			Set<OWLClass> checkedForClasses,
			OWLGraphWrapper ontology)
	{
		return RuleHelper.containsClassInEquivalenceAxioms(targetClass, checkedForClasses, ontology.getSourceOntology());
	}

	@Override
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass,
			OWLClass checkedFor,
			OWLGraphWrapper ontology)
	{
		return containsClassInEquivalenceAxioms(targetClass, Collections.singleton(checkedFor), ontology);
	}

	@Override
	public boolean containsClassInEquivalenceAxioms(OWLClass targetClass,
			String checkedForId,
			OWLGraphWrapper ontology)
	{
		OWLClass checkedFor = ontology.getOWLClassByIdentifier(checkedForId);
		return containsClassInEquivalenceAxioms(targetClass, checkedFor, ontology);
	}

	@Override
	public boolean containsClassInEquivalenceAxioms(String targetClassId,
			String checkedForId,
			OWLGraphWrapper ontology)
	{
		OWLClass targetClass = ontology.getOWLClassByIdentifier(targetClassId);
		OWLClass checkedFor = ontology.getOWLClassByIdentifier(checkedForId);
		return containsClassInEquivalenceAxioms(targetClass, checkedFor, ontology);
	}

	@Override
	public Set<OWLClass> getEquivalentClasses(final OWLClass cls, OWLGraphWrapper ontology) {
		ReasonerTaskManager manager = this.tools.factory.getDefaultTaskManager(ontology);
		final Set<OWLClass> result = new HashSet<OWLClass>();
		ManagedTask<OWLReasoner> task = new ManagedTask<OWLReasoner>(){

			@Override
			public Modified run(OWLReasoner managed)
			{
				Node<OWLClass> classes = managed.getEquivalentClasses(cls);
				if (classes != null) {
					result.addAll(classes.getEntitiesMinusBottom());
				}
				return Modified.no;
			}
			
		};
		manager.runManagedTask(task);
		return result;
	}

	@Override
	public Set<OWLClass> getEquivalentClasses(String id, OWLGraphWrapper ontology) {
		if (id == null) {
			return Collections.emptySet();
		}
		OWLClass cls = ontology.getOWLClassByIdentifier(id);
		if (cls == null ) {
			return Collections.emptySet();
		}
		return getEquivalentClasses(cls, ontology);
	}

	@Override
	public String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology) {
		return "\"" + ontology.getLabel(x) + "\" (" + ontology.getIdentifier(x) + ")";
	}
	
	@Override
	public String getTermShortInfo(String x, OWLGraphWrapper ontology) {
		return getTermShortInfo(getTermSimple(x, ontology), ontology);
	}

	@Override
	public String[] getInputs(String name) {
		List<String> inputs = tools.getInputs(name);
		if (inputs != null && !inputs.isEmpty()) {
			return inputs.toArray(new String[inputs.size()]);
		}
		return null;
	}

	@Override
	public String getInput(String name) {
		return tools.getInput(name);
	}

	@Override
	public String name(OWLObject x, OWLGraphWrapper ontology) {
		return name(x, new OWLGraphWrapper[] { ontology });
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
	public String definition(String prefix,
			OWLObject[] terms,
			OWLGraphWrapper ontology,
			String infix,
			String suffix)
	{
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
		return refname(x, new OWLGraphWrapper[] { ontology });
	}

	@Override
	public String refname(OWLObject x, OWLGraphWrapper[] ontologies) {
		String name = name(x, ontologies);
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

	@Override
	public boolean contains(String[] array, String value) {
		if (array != null && array.length > 0) {
			for (String string : array) {
				if (value == null) {
					if (string == null) {
						return true;
					}
				}
				else if (value.equals(string)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized void error(String message) {
		TermGenerationOutput error = createError(message);
		getResultList().add(error);
	}

	protected TermGenerationOutput createError(String message) {
		return TermGenerationOutput.error(tools.input, message);
	}

	public List<TermGenerationOutput> getResult() {
		return getResultList();
	}

	@Override
	public <A> List<A> concat(List<A> l1, List<A> l2) {
		if (l1 == null || l1.isEmpty()) {
			return l2;
		}
		if (l2 == null || l2.isEmpty()) {
			return l1;
		}
		List<A> resultList = new ArrayList<A>(l1.size() + l2.size());
		resultList.addAll(l1);
		resultList.addAll(l2);
		return resultList;
	}

	@Override
	public boolean hasChanges() {
		return tools.hasChanges();
	}

}
