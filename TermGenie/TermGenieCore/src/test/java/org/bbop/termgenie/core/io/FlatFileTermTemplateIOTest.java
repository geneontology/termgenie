package org.bbop.termgenie.core.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.ontology.impl.DefaultOntologyModule;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class FlatFileTermTemplateIOTest extends ResourceLoader {

	private static TermTemplateIO templateIO;

	@BeforeClass
	public static void setUpBeforeClass() {
		Injector injector = Guice.createInjector(new DefaultOntologyModule(),
				new TermTemplateIOModule());
		templateIO = injector.getInstance(TermTemplateIO.class);
	}
	
	/**
	 * Tests for {@link FlatFileTermTemplateIO#writeTemplates(java.util.Collection, BufferedWriter)} 
	 * and {@link FlatFileTermTemplateIO#readTemplates(java.io.BufferedReader)}
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadWriteTemplates() throws IOException {
		
		String init = load(loadResource("test_termgenie_rules.txt"));
		List<TermTemplate> read0 = templateIO.readTemplates(new BufferedReader(new StringReader(init)));
		
		String write1 = write(read0);
		
		assertEquals(init, write1);
		
		List<TermTemplate> read1 = read(write1);
		compare(read0, read1);
		
		String write2 = write(read1);
		
		assertEquals(write1, write2);
		
		List<TermTemplate> read2 = read(write2);
		compare(read0, read2);
		
		String write3 = write(read2);
		assertEquals(write1, write3);
	}
	
	private String load(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	private void compare(List<TermTemplate> l1, List<TermTemplate> l2) {
		assertEquals(l1.size(), l2.size());
		for (int i = 0; i < l1.size(); i++) {
			TermTemplate t1 = l1.get(i);
			TermTemplate t2 = l2.get(i);
			assertEquals(t1.getName(), t2.getName());
			assertEquals(t1.getDisplayName(), t2.getDisplayName());
			assertEquals(t1.getDescription(), t2.getDescription());
			assertEquals(t1.getHint(), t2.getHint());
			compare(t1.getCorrespondingOntology(), t2.getCorrespondingOntology());
			assertEquals(t1.getRules(), t2.getRules());
			List<TemplateField> fl1 = t1.getFields();
			List<TemplateField> fl2 = t2.getFields();
			assertEquals(fl1.size(), fl2.size());
			for (int j = 0; j < fl1.size(); j++) {
				TemplateField f1 = fl1.get(j);
				TemplateField f2 = fl2.get(j);
				compareList(f1.getCorrespondingOntologies(), f2.getCorrespondingOntologies());
				assertEquals(f1.getName(), f2.getName());
				assertEquals(f1.isRequired(), f2.isRequired());
				assertEquals(f1.getFunctionalPrefixes(), f1.getFunctionalPrefixes());
				assertEquals(CardinalityHelper.serializeCardinality(f1.getCardinality()), CardinalityHelper.serializeCardinality(f2.getCardinality()));
			}
		}
	}
	
	private void compareList(List<Ontology> l1, List<Ontology> l2) {
		if (l1 == null) {
			assertNull(l2);
			return;
		}
		assertNotNull(l2);
		assertEquals(l1.size(), l2.size());
		for (int i = 0; i < l1.size(); i++) {
			Ontology o1 = l1.get(i);
			Ontology o2 = l2.get(i);
			compare(o1, o2);
		}
	}
	
	private void compare(Ontology o1, Ontology o2) {
		if (o1 == null) {
			assertNull(o2);
			return;
		}
		assertNotNull(o2);
		assertEquals(o1.getUniqueName(), o2.getUniqueName());
		if (o1.getBranch() == null) {
			assertNull(o2.getBranch());
		} else {
			assertEquals(o1.getBranch(), o2.getBranch());
		}
	}
	
	private String write(Collection<TermTemplate> templates) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		templateIO.writeTemplates(templates, writer);
		writer.close();
		String writtenString = stringWriter.getBuffer().toString();
		return writtenString;
	}
	
	private List<TermTemplate> read(String string) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(string));
		List<TermTemplate> readTemplates = templateIO.readTemplates(reader);
		return readTemplates;
	}

	/**
	 * Tests for {@link FlatFileTermTemplateIO#subStringAfterSep(String)}
	 */
	@Test
	public void testSubStringAfterSep() {
		assertNull(FlatFileTermTemplateIO.subStringAfterSep(null));
		assertNull(FlatFileTermTemplateIO.subStringAfterSep(""));
		assertNull(FlatFileTermTemplateIO.subStringAfterSep("\t"));
		assertNull(FlatFileTermTemplateIO.subStringAfterSep("aaa\t"));
		assertEquals("b", FlatFileTermTemplateIO.subStringAfterSep("aaa\tb"));
		assertEquals("b\tc", FlatFileTermTemplateIO.subStringAfterSep("aaa\tb\tc"));
	}

}
