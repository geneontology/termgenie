package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * Abstract implementation of functions for the TermGenie scripting environment.
 * Hiding the results in an internal variable, allows type safe retrieval.
 * 
 * @param <T>
 */
public abstract class AbstractTermGenieScriptFunctionsImpl<T> implements
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
	 */
	AbstractTermGenieScriptFunctionsImpl(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super();
		tools = createTermCreationTool(input, targetOntology, auxiliaryOntologies, tempIdPrefix, patternID, factory);
	}

	protected abstract AbstractTermCreationTools<T> createTermCreationTool(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			Collection<OWLGraphWrapper> auxiliaryOntologies,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory);

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
			OWLObject x = ontology.getOWLObjectByIdentifier(id);
			if (ontology.getLabel(x) != null) {
				return x;
			}
		}
		return null;
	}

	private OntologyTerm<ISynonym, IRelation> getFieldSingleTerm(String name) {
		List<OntologyTerm<ISynonym, IRelation>> terms = getFieldTerms(name);
		if (terms == null || terms.isEmpty()) {
			return null;
		}
		return terms.get(0);
	}

	private List<OntologyTerm<ISynonym, IRelation>> getFieldTerms(String name) {
		Map<String, List<OntologyTerm<ISynonym, IRelation>>> terms = tools.input.getParameters().getTerms();
		if (terms != null) {
			return terms.get(name);
		}
		return null;
	}

	@Override
	public OWLObject[] getTerms(String name, OWLGraphWrapper ontology) {
		List<OntologyTerm<ISynonym, IRelation>> terms = getFieldTerms(name);
		if (terms == null || terms.isEmpty()) {
			return new OWLObject[0];
		}
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (OntologyTerm<ISynonym, IRelation> term : terms) {
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

	private String getTermShortInfo(OWLObject x, OWLGraphWrapper ontology) {
		return "\"" + ontology.getLabel(x) + "\" (" + ontology.getIdentifier(x) + ")";
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
	public List<ISynonym> synonyms(String prefix,
			OWLObject x,
			OWLGraphWrapper ontology,
			String suffix,
			String label)
	{
		List<ISynonym> synonyms = getSynonyms(x, ontology);
		if (synonyms == null || synonyms.isEmpty()) {
			return null;
		}
		List<ISynonym> results = new ArrayList<ISynonym>();
		for (ISynonym synonym : synonyms) {
			StringBuilder sb = new StringBuilder();
			if (prefix != null) {
				sb.append(prefix);
			}
			sb.append(synonym.getLabel());
			if (suffix != null) {
				sb.append(suffix);
			}
			addSynonym(results, synonym, synonym.getScope(), sb.toString(), label);
		}
		return results;
	}

	@Override
	public List<ISynonym> synonyms(String prefix,
			OWLObject x1,
			OWLGraphWrapper ontology1,
			String infix,
			OWLObject x2,
			OWLGraphWrapper ontology2,
			String suffix,
			String label)
	{
		List<ISynonym> synonyms1 = getSynonyms(x1, ontology1);
		List<ISynonym> synonyms2 = getSynonyms(x2, ontology2);
		boolean empty1 = synonyms1 == null || synonyms1.isEmpty();
		boolean empty2 = synonyms2 == null || synonyms2.isEmpty();
		if (empty1 && empty2) {
			// do nothing, as both do not have any synonyms
			return null;
		}
		synonyms1 = addLabel(x1, ontology1, synonyms1);
		synonyms2 = addLabel(x2, ontology2, synonyms2);

		List<ISynonym> results = new ArrayList<ISynonym>();
		for (ISynonym synonym1 : synonyms1) {
			for (ISynonym synonym2 : synonyms2) {
				Pair<Boolean, String> match = matchScopes(synonym1, synonym2);
				if (match.getOne()) {
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
					addSynonym(results, synonym1, match.getTwo(), sb.toString(), label);
				}
			}
		}
		return results;
	}

	@Override
	public List<ISynonym> synonyms(String prefix,
			OWLObject[] x,
			OWLGraphWrapper ontology,
			String infix,
			String suffix,
			String label)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private static final Pair<Boolean, String> MISMATCH = new Pair<Boolean, String>(false, null);

	protected Pair<Boolean, String> matchScopes(ISynonym s1, ISynonym s2) {
		String scope1 = s1.getScope();
		String scope2 = s2.getScope();
		if (scope1 == null) {
			scope1 = OboFormatTag.TAG_RELATED.getTag();
		}
		if (scope2 == null) {
			scope2 = OboFormatTag.TAG_RELATED.getTag();
		}
		if (scope1.equals(scope2)) {
			return new Pair<Boolean, String>(true, scope1);
		}
		if (scope1.equals(OboFormatTag.TAG_BROAD.getTag())
				|| scope2.equals(OboFormatTag.TAG_BROAD.getTag())) {
			// this is the only case for a miss match
			return MISMATCH;	
		}
		if (scope1.equals(OboFormatTag.TAG_EXACT.getTag())) {
			return new Pair<Boolean, String>(true, scope2);
		}
		if (scope2.equals(OboFormatTag.TAG_EXACT.getTag())) {
			return new Pair<Boolean, String>(true, scope1);
		}
		if (scope1.equals(OboFormatTag.TAG_NARROW.getTag())){
			return new Pair<Boolean, String>(true, scope2);
		}
		if (scope2.equals(OboFormatTag.TAG_NARROW.getTag())) {
			return new Pair<Boolean, String>(true, scope1);
		}
		return new Pair<Boolean, String>(true, OboFormatTag.TAG_RELATED.getTag());
	}

	private List<ISynonym> addLabel(OWLObject x, OWLGraphWrapper ontology, List<ISynonym> synonyms) {
		String label = ontology.getLabel(x);
		if (synonyms == null) {
			synonyms = new ArrayList<ISynonym>(1);
		}
		synonyms.add(new Synonym(label, null, null, null));
		return synonyms;
	}

	void addSynonym(List<ISynonym> results,
			ISynonym synonym,
			String scope,
			String newLabel,
			String label)
	{
		if (!newLabel.equals(label)) {
			// if by any chance a synonym has the same label, it is ignored
			Set<String> xrefs = addXref("GOC:TermGenie", synonym.getXrefs());
			// TODO what to to with categories if creating a compound synonym?
			if (scope == null) {
				scope = OboFormatTag.TAG_RELATED.getTag();
			}
			results.add(new Synonym(newLabel, scope, null, xrefs));
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

	private List<ISynonym> getSynonyms(OWLObject id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			List<ISynonym> oboSynonyms = ontology.getOBOSynonyms(id);
			if (oboSynonyms != null && !oboSynonyms.isEmpty()) {
				// defensive copy
				oboSynonyms = new ArrayList<ISynonym>(oboSynonyms);
			}
			return oboSynonyms;
		}
		return null;
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
		TermGenerationOutput error = new TermGenerationOutput(null, tools.input, false, message);
		return error;
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
