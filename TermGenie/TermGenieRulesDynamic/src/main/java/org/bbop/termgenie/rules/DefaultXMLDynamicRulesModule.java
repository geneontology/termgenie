package org.bbop.termgenie.rules;

import org.bbop.termgenie.core.io.XMLTermTemplateModule;
import org.bbop.termgenie.core.rules.TermGenerationEngine;

/**
 * Module which provides a {@link TermGenerationEngine}, using dynamic rules
 * from an external xml file.
 */
public class DefaultXMLDynamicRulesModule extends DefaultDynamicRulesModule {

	@Override
	protected void bindTemplateIO() {
		install(new XMLTermTemplateModule());
		bind("DynamicRulesTemplateResource", "default_termgenie_rules.xml");
	}

}
