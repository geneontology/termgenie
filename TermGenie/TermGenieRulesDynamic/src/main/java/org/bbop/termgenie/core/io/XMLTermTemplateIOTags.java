package org.bbop.termgenie.core.io;

/**
 * Tags used in the XML based {@link TermTemplateIO}.
 */
interface XMLTermTemplateIOTags {

	static final String TAG_termgenietemplates = "termgenietemplates";
	static final String TAG_template = "template";
	static final String TAG_description = "description";
	static final String TAG_hint = "hint";
	static final String TAG_ontology = "ontology";
	static final String TAG_external = "external";
	static final String TAG_obonamespace = "obonamespace";
	static final String TAG_requires = "requires";
	static final String TAG_fields = "fields";
	static final String TAG_field = "field";
	static final String TAG_ruleFiles = "ruleFiles";
	static final String TAG_ruleFile = "ruleFile";
	static final String TAG_methodName = "methodName";
	static final String TAG_cardinality = "cardinality";
	static final String TAG_prefixes = "prefixes";
	static final String TAG_prefix = "prefix";
	static final String TAG_branch = "branch";

	static final String ATTR_name = "name";
	static final String ATTR_label = "label";
	static final String ATTR_displayname = "displayname";
	static final String ATTR_required = "required";
}
