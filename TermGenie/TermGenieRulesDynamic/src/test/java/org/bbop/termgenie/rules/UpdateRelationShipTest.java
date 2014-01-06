package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.ioc.TermGenieGuice;
import org.bbop.termgenie.core.rules.ReasonerModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.TestDefaultOntologyModule;
import org.bbop.termgenie.ontology.impl.XMLOntologyConfiguration;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.tools.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.QualifierValue;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.google.inject.Injector;

/**
 * Test that an existing terms are also updated.
 */
public class UpdateRelationShipTest {

	private static TermGenerationEngine generationEngine;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = TermGenieGuice.createInjector(new XMLDynamicRulesModule("termgenie_rules_simple.xml", true, true, null),
				new TestDefaultOntologyModule() {

					@Override
					protected void bindOntologyConfiguration() {
						bind(OntologyConfiguration.class, XMLOntologyConfiguration.class);
						bind("XMLOntologyConfigurationResource",
								"ontology-configuration_simple.xml");
					}
				},
				new ReasonerModule(null));

		generationEngine = injector.getInstance(TermGenerationEngine.class);
	}
	
	@Test
	public void test_involved_in_relations() throws Exception {
		TermTemplate termTemplate = generationEngine.getAvailableTemplates().get(1);
		TermGenerationParameters parameters = new TermGenerationParameters();

		String field0 = termTemplate.getFields().get(0).getName();
		String field1 = termTemplate.getFields().get(1).getName();

		parameters.setTermValues(field0,
				Arrays.asList("GO:0050673"));
		parameters.setTermValues(field1, 
				Arrays.asList("GO:0072001"));

		TermGenerationInput input = new TermGenerationInput(termTemplate, parameters);
		List<TermGenerationInput> generationTasks = Collections.singletonList(input);
		List<TermGenerationOutput> list = generationEngine.generateTerms(generationTasks, false, null);

		assertNotNull(list);
		assertEquals(1, list.size());
		TermGenerationOutput output = list.get(0);
		assertNull(output.getError());
		Frame term = output.getTerm();

		List<Clause> clauses = OboTools.getRelations(term);
		assertEquals(4, clauses.size());
		Clause clause0 = clauses.get(0);
		assertEquals(OboFormatTag.TAG_IS_A.getTag(), clause0.getTag());
		assertEquals("GO:0050673", clause0.getValue());

		Clause clause1 = clauses.get(1);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause1.getTag());
		assertEquals("GO:0050673", clause1.getValue());

		Clause clause2 = clauses.get(2);
		assertEquals(OboFormatTag.TAG_INTERSECTION_OF.getTag(), clause2.getTag());
		assertEquals("part_of", clause2.getValue());
		assertEquals("GO:0072001", clause2.getValue2());

		Clause clause3 = clauses.get(3);
		assertEquals(OboFormatTag.TAG_RELATIONSHIP.getTag(), clause3.getTag());
		assertEquals("part_of", clause3.getValue());
		assertEquals("GO:0072001", clause3.getValue2());
		
		List<Pair<Frame,Set<OWLAxiom>>> changedTermRelations = output.getChangedTermRelations();
		assertEquals(1, changedTermRelations.size());
		Pair<Frame, Set<OWLAxiom>> pair = changedTermRelations.get(0);
		Frame changedFrame = pair.getOne();
		
		assertEquals("GO:2001013", changedFrame.getId());
		Collection<Clause> isaClauses = changedFrame.getClauses(OboFormatTag.TAG_IS_A);
		assertEquals(1, isaClauses.size());
		Clause isaClause = isaClauses.iterator().next();
		assertEquals("GO:TEMP-involvedin00", isaClause.getValue());
		Collection<QualifierValue> qualifierValues = isaClause.getQualifierValues();
		assertEquals(1, qualifierValues.size());
		QualifierValue qv = qualifierValues.iterator().next();
		assertEquals("is_inferred", qv.getQualifier());
		assertEquals("true", qv.getValue());
	}

}
