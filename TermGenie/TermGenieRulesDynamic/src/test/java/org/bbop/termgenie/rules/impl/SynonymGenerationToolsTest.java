package org.bbop.termgenie.rules.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;
import owltools.graph.OWLGraphWrapper.Synonym;

public class SynonymGenerationToolsTest {

	private static SynonymGenerationTools tool;
	private static OWLObject t1 = new DummyOWLObject();
	private static OWLObject t2 = new DummyOWLObject();
	private static OWLObject t3 = new DummyOWLObject();
	private static OWLObject t4 = new DummyOWLObject();
	private static OWLObject t5 = new DummyOWLObject();
	private static OWLObject t6 = new DummyOWLObject();

	private static class DummyOWLObject implements OWLObject {

		@Override
		public int compareTo(OWLObject o) {
			return 0;
		}

		@Override
		public Set<OWLEntity> getSignature() {
			return null;
		}

		@Override
		public Set<OWLClass> getClassesInSignature() {
			return null;
		}

		@Override
		public Set<OWLDataProperty> getDataPropertiesInSignature() {
			return null;
		}

		@Override
		public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
			return null;
		}

		@Override
		public Set<OWLNamedIndividual> getIndividualsInSignature() {
			return null;
		}

		@Override
		public Set<OWLDatatype> getDatatypesInSignature() {
			return null;
		}

		@Override
		public Set<OWLClassExpression> getNestedClassExpressions() {
			return null;
		}

		@Override
		public void accept(OWLObjectVisitor visitor) {
			// empty
		}

		@Override
		public <O> O accept(OWLObjectVisitorEx<O> visitor) {
			return null;
		}

		@Override
		public boolean isTopEntity() {
			return false;
		}

		@Override
		public boolean isBottomEntity() {
			return false;
		}

		@Override
		public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
			return null;
		}
	}

	@BeforeClass
	public static void beforeClass() {
		final Map<OWLObject, String> labels = new HashMap<OWLObject, String>();
		final Map<OWLObject, List<ISynonym>> source = new HashMap<OWLObject, List<ISynonym>>();
		tool = new SynonymGenerationTools() {

			@Override
			protected List<ISynonym> getSynonyms(OWLObject id, OWLGraphWrapper ontology, String requiredPrefix, boolean ignoreSynonyms) {
				List<ISynonym> oboSynonyms = source.get(id);
				if (oboSynonyms != null) {
					// defensive copy
					oboSynonyms = new ArrayList<ISynonym>(oboSynonyms);
				}
				return oboSynonyms;
			}

			@Override
			protected String getLabel(OWLObject x, OWLGraphWrapper ontology) {
				return labels.get(x);
			}

		};
		labels.put(t1, "t1");
		source.put(t1, 
				Arrays.asList(synonym("s11", OboFormatTag.TAG_EXACT, "s11xref1", "s11xref2"),
						synonym("s12", OboFormatTag.TAG_BROAD, "s12xref1", "s12xref2", "s12xref3")));
		labels.put(t2, "t2");
		source.put(t2,
				Arrays.asList(synonym("s21", OboFormatTag.TAG_NARROW, "s21xref1", "s21xref2"),
						synonym("s22", OboFormatTag.TAG_EXACT, "s22xref1", "s22xref2", "s22xref3"),
						synonym("s23", OboFormatTag.TAG_BROAD, "s23xref1")));
		
		labels.put(t3, "t3");
		source.put(t3, Collections.<ISynonym> emptyList());
		
		labels.put(t4, "t4");
		source.put(t4,
				Arrays.asList(synonym("s41", OboFormatTag.TAG_NARROW, "s41xref1", "s41xref2"),
						synonym("s42", OboFormatTag.TAG_BROAD, "s42xref1", "s42xref2", "s42xref3")));
		
		labels.put(t5, "t5");
		source.put(t5, Collections.<ISynonym> emptyList());
		
		labels.put(t6, "t6");
		source.put(t6, Collections.<ISynonym> emptyList());
	}

	private static ISynonym synonym(String label, OboFormatTag tag, String...xrefs) {
		Set<String> set = new HashSet<String>(Arrays.asList(xrefs));
		return new Synonym(label, tag.getTag(), null, set);
	}

	@Test
	public void testSynonymsSingle() {
		String defaultScope = null;
		List<ISynonym> synonyms = tool.synonyms("prefix-", t1, null, "-suffix", defaultScope, "t1");
		assertEquals(2, synonyms.size());
		{
			ISynonym s1 = synonyms.get(0);
			assertEquals("prefix-s11-suffix", s1.getLabel());
			assertEquals(OboFormatTag.TAG_EXACT.getTag(), s1.getScope());
			Set<String> xrefs1 = s1.getXrefs();
			assertEquals(1, xrefs1.size());
			assertTrue(xrefs1.contains("GOC:TermGenie"));
		}
		{
			ISynonym s2 = synonyms.get(1);
			assertEquals("prefix-s12-suffix", s2.getLabel());
			assertEquals(OboFormatTag.TAG_BROAD.getTag(), s2.getScope());
			Set<String> xrefs2 = s2.getXrefs();
			assertEquals(1, xrefs2.size());
			assertTrue(xrefs2.contains("GOC:TermGenie"));
		}
		assertNull(tool.synonyms("prefix-", t3, null, "-suffix", defaultScope , "t3"));
	}

	@Test
	public void testSynonymsPair() {
		String defaultScope = null;
		List<ISynonym> synonyms = tool.synonyms("prefix-", t1, null, "-infix-", t2, null, "-suffix", defaultScope, "prefix-t1-infix-t2-suffix");
		assertEquals(6, synonyms.size());
		Iterator<ISynonym> it = synonyms.iterator();
		assertSynonym("prefix-s11-infix-s21-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-s11-infix-s22-suffix", OboFormatTag.TAG_EXACT, it.next());
		assertSynonym("prefix-s11-infix-t2-suffix", OboFormatTag.TAG_EXACT, it.next());
		
		assertSynonym("prefix-s12-infix-s23-suffix", OboFormatTag.TAG_BROAD, it.next());
		
		assertSynonym("prefix-t1-infix-s21-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-t1-infix-s22-suffix", OboFormatTag.TAG_EXACT, it.next());
	}
	
	private void assertSynonym(String label, OboFormatTag scope, ISynonym synonym) {
		assertEquals(label, synonym.getLabel());
		assertEquals(scope.getTag(), synonym.getScope());
		assertEquals(1, synonym.getXrefs().size());
		assertTrue(synonym.getXrefs().contains("GOC:TermGenie"));
	}

	@Test
	public void testSynonymsList() {
		OWLObject[] terms = {t1, t2, t4};
		String[] defaultScopes = null;
		List<ISynonym> synonyms = tool.synonyms("prefix-", terms, defaultScopes, null, "-infix-", "-suffix", "prefix-t1-infix-t2-infix-t4-suffix");
		assertEquals(12, synonyms.size());
		
		Iterator<ISynonym> it = synonyms.iterator();
		assertSynonym("prefix-s11-infix-s21-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-s11-infix-s21-infix-t4-suffix", OboFormatTag.TAG_NARROW, it.next());
		
		assertSynonym("prefix-s11-infix-s22-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-s11-infix-s22-infix-t4-suffix", OboFormatTag.TAG_EXACT, it.next());
		
		assertSynonym("prefix-s11-infix-t2-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-s11-infix-t2-infix-t4-suffix", OboFormatTag.TAG_EXACT, it.next());
		
		assertSynonym("prefix-s12-infix-s23-infix-s42-suffix", OboFormatTag.TAG_BROAD, it.next());
		
		assertSynonym("prefix-t1-infix-s21-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-t1-infix-s21-infix-t4-suffix", OboFormatTag.TAG_NARROW, it.next());
		
		assertSynonym("prefix-t1-infix-s22-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
		assertSynonym("prefix-t1-infix-s22-infix-t4-suffix", OboFormatTag.TAG_EXACT, it.next());
		
		assertSynonym("prefix-t1-infix-t2-infix-s41-suffix", OboFormatTag.TAG_NARROW, it.next());
	}

	@Test
	public void testMatchScopes() {
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(null, null));

		assertScopeMatch(OboFormatTag.TAG_RELATED,
				match(OboFormatTag.TAG_RELATED, OboFormatTag.TAG_RELATED));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(OboFormatTag.TAG_RELATED, null));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(null, OboFormatTag.TAG_RELATED));

		assertScopeMatch(OboFormatTag.TAG_BROAD,
				match(OboFormatTag.TAG_BROAD, OboFormatTag.TAG_BROAD));
		assertFalse(match(OboFormatTag.TAG_BROAD, null).getOne());
		assertFalse(match(null, OboFormatTag.TAG_BROAD).getOne());

		assertFalse(match(OboFormatTag.TAG_BROAD, OboFormatTag.TAG_RELATED).getOne());
		assertFalse(match(OboFormatTag.TAG_BROAD, OboFormatTag.TAG_NARROW).getOne());
		assertFalse(match(OboFormatTag.TAG_BROAD, OboFormatTag.TAG_EXACT).getOne());

		assertScopeMatch(OboFormatTag.TAG_NARROW,
				match(OboFormatTag.TAG_NARROW, OboFormatTag.TAG_NARROW));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(OboFormatTag.TAG_NARROW, null));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(null, OboFormatTag.TAG_NARROW));

		assertScopeMatch(OboFormatTag.TAG_NARROW,
				match(OboFormatTag.TAG_NARROW, OboFormatTag.TAG_NARROW));
		assertScopeMatch(OboFormatTag.TAG_NARROW,
				match(OboFormatTag.TAG_NARROW, OboFormatTag.TAG_EXACT));
		assertScopeMatch(OboFormatTag.TAG_RELATED,
				match(OboFormatTag.TAG_NARROW, OboFormatTag.TAG_RELATED));

		assertScopeMatch(OboFormatTag.TAG_EXACT,
				match(OboFormatTag.TAG_EXACT, OboFormatTag.TAG_EXACT));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(OboFormatTag.TAG_EXACT, null));
		assertScopeMatch(OboFormatTag.TAG_RELATED, match(null, OboFormatTag.TAG_EXACT));

		assertScopeMatch(OboFormatTag.TAG_NARROW,
				match(OboFormatTag.TAG_EXACT, OboFormatTag.TAG_NARROW));
		assertScopeMatch(OboFormatTag.TAG_RELATED,
				match(OboFormatTag.TAG_EXACT, OboFormatTag.TAG_RELATED));
	}

	private Pair<Boolean, String> match(OboFormatTag tag1, OboFormatTag tag2) {
		return tool.matchScopes(tag1 != null ? tag1.getTag() : null,
				tag2 != null ? tag2.getTag() : null);
	}

	private void assertScopeMatch(OboFormatTag tag, Pair<Boolean, String> match) {
		assertTrue(match.getOne());
		assertEquals(tag.getTag(), match.getTwo());
	}

}
