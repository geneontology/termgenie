package org.bbop.termgenie.core.rules;

import static org.junit.Assert.*;

import org.bbop.termgenie.core.rules.TermGenerationEngine.MultiValueMap;
import org.junit.Test;

public class TermGenerationEngineTest {

	@Test
	public void testAddValue() {
		MultiValueMap<String> testMap = new MultiValueMap<String>();
		
		testMap.addValue("f0", DefaultTermTemplates.Field_DefX_Ref, 0);
		assertArrayEquals(new String[]{"f0"}, testMap.getValues(DefaultTermTemplates.Field_DefX_Ref).toArray());
		
		testMap.addValue("f1", DefaultTermTemplates.Field_DefX_Ref, 1);
		assertArrayEquals(new String[]{"f0", "f1"}, testMap.getValues(DefaultTermTemplates.Field_DefX_Ref).toArray());
		
		testMap.addValue("f11", DefaultTermTemplates.Field_DefX_Ref, 1);
		assertArrayEquals(new String[]{"f0", "f11"}, testMap.getValues(DefaultTermTemplates.Field_DefX_Ref).toArray());
		
		testMap.addValue("f3", DefaultTermTemplates.Field_DefX_Ref, 3);
		assertArrayEquals(new String[]{"f0", "f11", null, "f3"}, testMap.getValues(DefaultTermTemplates.Field_DefX_Ref).toArray());
		
		testMap.addValue("f6", DefaultTermTemplates.Field_DefX_Ref, 6);
		assertArrayEquals(new String[]{"f0", "f11", null, "f3", null, null, "f6"}, testMap.getValues(DefaultTermTemplates.Field_DefX_Ref).toArray());
		
	}

}
