package org.bbop.termgenie.core.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.Ontology.OntologySubset;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.TestDefaultOntologyModule;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;

public class XMLTermTemplateIOTest extends ResourceLoader {

	public XMLTermTemplateIOTest() {
		super(false);
	}

	private static XMLTermTemplateIO instance;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Injector injector = TermGenieGuice.createInjector(new TestDefaultOntologyModule("ontology-configuration_simple.xml"));
		instance = new XMLTermTemplateIO(injector.getInstance(OntologyConfiguration.class));
	}

	@Test
	public void testTemplateIO() throws IOException {
		List<TermTemplate> templates0 = instance.readTemplates(loadResource("termgenie_rules_simple.xml"));

		String xmlString1 = write(templates0);
		List<TermTemplate> templates1 = read(xmlString1);
		assertTemplateList(templates0, templates1);

		String xmlString2 = write(templates1);
		List<TermTemplate> templates2 = read(xmlString1);
		assertTemplateList(templates0, templates2);

		assertEquals(xmlString1, xmlString2);
	}
	
	private void assertTemplateList(List<TermTemplate> templates1, List<TermTemplate> templates2) {
		assertEquals(templates1.size(), templates2.size());
		for (int i = 0; i < templates1.size(); i++) {
			TermTemplate t1 = templates1.get(i);
			TermTemplate t2 = templates2.get(i);
			assertNotNull(t1);
			assertNotNull(t2);
			assertEquals(t1.getName(), t2.getName());
			assertEquals(t1.getDisplayName(), t2.getDisplayName());
			assertEquals(t1.getDescription(), t2.getDescription());
			assertEquals(t1.getHint(), t2.getHint());
			assertEquals(t1.getOboNamespace(), t2.getOboNamespace());
			assertEquals(t1.getDefinitionXref(), t2.getDefinitionXref());
			assertArrayEquals(t1.getRuleFiles().toArray(), t2.getRuleFiles().toArray());
			assertEquals(t1.getMethodName(), t2.getMethodName());
			assertFields(t1.getFields(), t2.getFields());
		}
	}

	private void assertFields(List<TemplateField> fields1, List<TemplateField> fields2) {
		assertNotNull(fields1);
		assertNotNull(fields2);
		assertFalse(fields1.isEmpty());
		assertFalse(fields2.isEmpty());
		for (int i = 0; i < fields1.size(); i++) {
			assertField(fields1.get(i), fields2.get(i));
		}
	}

	private void assertField(TemplateField field1, TemplateField field2) {
		assertEquals(field1.getName(), field2.getName());
		assertEquals(field1.getLabel(), field2.getLabel());
		assertEquals(field1.getHint(), field2.getHint());
		assertEquals(field1.getRemoteResource(), field2.getRemoteResource());
		assertEquals(field1.isRequired(), field2.isRequired());
		assertEquals(field1.getCardinality(), field2.getCardinality());
		assertSubsets(field1.getSubset(), field2.getSubset());
		assertList(field1.getFunctionalPrefixes(), field2.getFunctionalPrefixes());
		assertList(field1.getFunctionalPrefixesIds(), field2.getFunctionalPrefixesIds());
		assertEquals(field1.isPreSelected(), field2.isPreSelected());
	}

	private void assertList(List<String> l1, List<String> l2) {
		if (l1 != l2) {
			assertNotNull(l1);
			assertNotNull(l2);
			assertArrayEquals(l1.toArray(), l2.toArray());
		}
	}

	private void assertSubsets(OntologySubset s1, OntologySubset s2) {
		if (s1 != s2) {
			assertNotNull(s1);
			assertNotNull(s2);
			assertEquals(s1.getName(), s2.getName());
		}
	}

	private String write(Collection<TermTemplate> templates) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		instance.writeTemplates(templates, outputStream);
		outputStream.close();
		return outputStream.toString();
	}

	private List<TermTemplate> read(String xmlString) throws IOException {
		InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
		List<TermTemplate> templates = instance.readTemplates(inputStream);
		inputStream.close();
		return templates;
	}

}
