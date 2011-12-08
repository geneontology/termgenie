package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.graph.OWLGraphWrapper.Synonym;

public class SynonymGenerationTools implements TermGenieScriptFunctionsSynonyms {

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
			addSynonym(results, synonym.getScope(), sb.toString(), label);
		}
		return results;
	}

	@Override
	public List<ISynonym> synonyms(String[] prefixes,
			OWLObject x,
			OWLGraphWrapper ontology,
			String[] suffixes,
			String label)
	{
		List<ISynonym> results = new ArrayList<ISynonym>();
		String termLabel = getLabel(x, ontology);
		for(String prefix : prefixes) {
			if (suffixes != null && suffixes.length > 0) {
				for(String suffix : suffixes) {
					addSynonym(label, results, termLabel, prefix, OboFormatTag.TAG_RELATED.getTag(), suffix);
				}
			}
			else {
				addSynonym(label, results, termLabel, prefix, OboFormatTag.TAG_RELATED.getTag(), null);
			}
		}
		
		List<ISynonym> synonyms = getSynonyms(x, ontology);
		if (synonyms != null && !synonyms.isEmpty()) {
			for (ISynonym synonym : synonyms) {
				for(String prefix : prefixes) {
					String scope = synonym.getScope();
					if (suffixes != null && suffixes.length > 0) {
						for(String suffix : suffixes) {
							addSynonym(label, results, synonym.getLabel(), prefix, scope, suffix);
						}
					}
					else {
						addSynonym(label, results, synonym.getLabel(), prefix, scope, null);
					}
				}
				
			}
		}
		
		if (results.isEmpty()) {
			results = null;
		}
		return results;
	}

	private void addSynonym(String label,
			List<ISynonym> results,
			String synonymLabel,
			String prefix,
			String scope,
			String suffix)
	{
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		sb.append(synonymLabel);
		if (suffix != null) {
			sb.append(suffix);
		}
		addSynonym(results, scope, sb.toString(), label);
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
					addSynonym(results, match.getTwo(), sb.toString(), label);
				}
			}
		}
		return results;
	}

	@Override
	public List<ISynonym> synonyms(String prefix,
			OWLObject[] terms,
			OWLGraphWrapper ontology,
			String infix,
			String suffix,
			String label)
	{
		if (terms == null || terms.length == 0) {
			return null;
		}
		final int size = terms.length;
		if (size == 1) {
			return synonyms(prefix, terms[0], ontology, suffix, label);
		}

		if (size == 2) {
			return synonyms(prefix, terms[0], ontology, infix, terms[1], ontology, suffix, label);
		}
		List<ISynonym> result = new ArrayList<ISynonym>();
		List<OWLObject> termList = Arrays.asList(terms).subList(1, terms.length);
		OWLObject term = terms[0];
		List<ISynonym> synonyms = getSynonyms(term, ontology);
		synonyms = addLabel(term, ontology, synonyms);
		for (ISynonym synonym : synonyms) {
			StringBuilder start = new StringBuilder();
			if (prefix != null) {
				start.append(prefix);
			}
			start.append(synonym.getLabel());
			List<Pair<StringBuilder, String>> pairs = concat(start,
					synonym.getScope(),
					termList,
					ontology,
					infix);
			for (Pair<StringBuilder, String> pair : pairs) {
				StringBuilder sb = pair.getOne();
				if (suffix != null) {
					sb.append(suffix);
				}
				addSynonym(result, pair.getTwo(), sb.toString(), label);
			}
		}
		return result;
	}

	private List<Pair<StringBuilder, String>> concat(final CharSequence prefix,
			String scope,
			List<OWLObject> terms,
			OWLGraphWrapper ontology,
			String infix)
	{
		OWLObject term = terms.get(0);
		List<ISynonym> synonyms = getSynonyms(term, ontology);
		synonyms = addLabel(term, ontology, synonyms);
		List<Pair<StringBuilder, String>> result = new ArrayList<Pair<StringBuilder, String>>();
		for (ISynonym synonym : synonyms) {
			Pair<Boolean, String> pair = matchScopes(scope, synonym.getScope());
			if (pair.getOne()) {
				StringBuilder sb = new StringBuilder(prefix);
				sb.append(infix);
				sb.append(synonym.getLabel());
				if (terms.size() > 1) {
					List<Pair<StringBuilder, String>> concats = concat(sb, pair.getTwo(), terms.subList(1, terms.size()), ontology, infix);
					result.addAll(concats);
				}
				else {
					result.add(new Pair<StringBuilder, String>(sb, pair.getTwo()));
				}
			}
		}
		return result;
	}

	private static final Pair<Boolean, String> MISMATCH = new Pair<Boolean, String>(false, null);

	protected Pair<Boolean, String> matchScopes(ISynonym s1, ISynonym s2) {
		return matchScopes(s1.getScope(), s2.getScope());
	}

	protected Pair<Boolean, String> matchScopes(String scope1, String scope2) {
		if (scope1 == null) {
			scope1 = OboFormatTag.TAG_RELATED.getTag();
		}
		if (scope2 == null) {
			scope2 = OboFormatTag.TAG_RELATED.getTag();
		}
		if (scope1.equals(scope2)) {
			return new Pair<Boolean, String>(true, scope1);
		}
		if (scope1.equals(OboFormatTag.TAG_BROAD.getTag()) || scope2.equals(OboFormatTag.TAG_BROAD.getTag())) {
			// this is the only case for a miss match
			return MISMATCH;
		}
		if (scope1.equals(OboFormatTag.TAG_EXACT.getTag())) {
			return new Pair<Boolean, String>(true, scope2);
		}
		if (scope2.equals(OboFormatTag.TAG_EXACT.getTag())) {
			return new Pair<Boolean, String>(true, scope1);
		}
		if (scope1.equals(OboFormatTag.TAG_NARROW.getTag())) {
			return new Pair<Boolean, String>(true, scope2);
		}
		if (scope2.equals(OboFormatTag.TAG_NARROW.getTag())) {
			return new Pair<Boolean, String>(true, scope1);
		}
		return new Pair<Boolean, String>(true, OboFormatTag.TAG_RELATED.getTag());
	}

	private List<ISynonym> addLabel(OWLObject x, OWLGraphWrapper ontology, List<ISynonym> synonyms)
	{
		String label = getLabel(x, ontology);
		if (synonyms == null) {
			synonyms = new ArrayList<ISynonym>(1);
		}
		synonyms.add(new Synonym(label, null, null, null));
		return synonyms;
	}

	protected String getLabel(OWLObject x, OWLGraphWrapper ontology) {
		String label = ontology.getLabel(x);
		return label;
	}

	void addSynonym(List<ISynonym> results,
			String scope,
			String newLabel,
			String label)
	{
		if (!newLabel.equals(label)) {
			// if by any chance a synonym has the same label, it is ignored
			// TODO what to to with categories if creating a compound synonym?
			if (scope == null) {
				scope = OboFormatTag.TAG_RELATED.getTag();
			}
			results.add(new Synonym(newLabel, scope, null, Collections.singleton("GOC:TermGenie")));
		}
	}

	protected List<ISynonym> getSynonyms(OWLObject id, OWLGraphWrapper ontology) {
		if (ontology != null) {
			List<ISynonym> oboSynonyms = ontology.getOBOSynonyms(id);
			if (oboSynonyms != null) {
				// defensive copy
				oboSynonyms = new ArrayList<ISynonym>(oboSynonyms);
			}
			return oboSynonyms;
		}
		return null;
	}
}
