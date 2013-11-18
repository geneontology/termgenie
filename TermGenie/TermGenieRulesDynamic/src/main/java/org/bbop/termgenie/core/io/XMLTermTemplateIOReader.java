package org.bbop.termgenie.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;

/**
 * Parse term templates from an XML stream.
 */
class XMLTermTemplateIOReader implements XMLTermTemplateIOTags {

	private final Map<String, ConfiguredOntology> configurations;
	private final XMLInputFactory factory;

	/**
	 * @param ontologyConfiguration
	 */
	XMLTermTemplateIOReader(OntologyConfiguration ontologyConfiguration) {
		super();
		configurations = ontologyConfiguration.getOntologyConfigurations();
		factory = XMLInputFactory.newInstance();
	}

	/**
	 * @param inputStream
	 * @return templates
	 * @throws IOException
	 */
	List<TermTemplate> readTemplates(InputStream inputStream) throws IOException {
		try {
			XMLStreamReader parser = factory.createXMLStreamReader(inputStream);

			List<TermTemplate> result = null;
			for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					String element = parser.getLocalName();
					if (TAG_termgenietemplates.equals(element)) {
						if (result != null) {
							error("Multiple " + TAG_termgenietemplates + " tags found", parser);
						}
						result = new ArrayList<TermTemplate>();
					}
					else if (TAG_template.equals(element)) {
						if (result == null) {
							error("No " + TAG_termgenietemplates + " top level element found.",
									parser);
						}
						parseTemplate(parser, result);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
				}
			}
			parser.close();
			return result;
		} catch (XMLStreamException exception) {
			throw new RuntimeException(exception);
		}
	}

	private static class TemplateParseData {

		Ontology correspondingOntology = null;
		String name = null;
		String displayName = null;
		String description = null;
		List<TemplateField> fields = null;
		List<Ontology> external = null;
		List<String> requires = null;
		String obo_namespace = null;
		List<String> ruleFiles = null;
		String methodName = null;
		String hint = null;
		List<String> categories = null;
	}

	private void parseTemplate(XMLStreamReader parser, List<TermTemplate> result)
			throws XMLStreamException
	{
		TemplateParseData current = new TemplateParseData();
		current.name = getAttribute(parser, ATTR_name);
		current.displayName = getAttribute(parser, ATTR_displayname, true);

		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_template.equals(parser.getLocalName())) {
						add(result, current, parser);
						return;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_description.equals(element)) {
						current.description = getTextTag(parser,
								TAG_description,
								current.description);
					}
					else if (TAG_hint.equals(element)) {
						current.hint = getTextTag(parser, TAG_hint, current.hint);
					}
					else if (TAG_obonamespace.equals(element)) {
						current.obo_namespace = getTextTag(parser,
								TAG_obonamespace,
								current.obo_namespace);
					}
					else if (TAG_ruleFiles.equals(element)) {
						current.ruleFiles = parseRuleFiles(parser, current);
					}
					else if (TAG_ontology.equals(element)) {
						if (current.correspondingOntology != null) {
							error("Multiple " + TAG_ontology + " tags found", parser);
						}
						current.correspondingOntology = parseOntology(parser);
					}
					else if (TAG_requires.equals(element)) {
						current.requires = parseRequires(parser, current);
					}
					else if (TAG_fields.equals(element)) {
						current.fields = parseFields(parser, current);
					}
					else if (TAG_external.equals(element)) {
						current.external = parseExternal(parser, current);
					}
					else if (TAG_categories.equals(element)) {
						current.categories = parseCategories(parser, current);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	private Ontology parseOntology(XMLStreamReader parser) throws XMLStreamException {
		String name = getAttribute(parser, ATTR_name);
		String branch = null;
		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_ontology.equals(parser.getLocalName())) {
						Ontology ontology;
						if (branch != null) {
							ontology = configurations.get(branch);
						}
						else {
							ontology = configurations.get(name);
						}
						return ontology;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_branch.equals(element)) {
						branch = getTextTag(parser, TAG_branch, branch);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	private List<String> parseRequires(XMLStreamReader parser, TemplateParseData current)
			throws XMLStreamException
	{
		String text = parseElementText(parser, TAG_requires);
		if (current.requires == null) {
			return Collections.singletonList(text);
		}
		else if (current.requires.size() == 1) {
			List<String> result = new ArrayList<String>(2);
			result.add(current.requires.get(0));
			result.add(text);
			return result;
		}
		else {
			current.requires.add(text);
			return current.requires;
		}
	}

	private List<String> parseRuleFiles(XMLStreamReader parser, TemplateParseData current) 
			throws XMLStreamException
	{
		if (current.ruleFiles != null) {
			error("Multiple " + TAG_ruleFiles + " tags found", parser);
		}
		current.ruleFiles = new ArrayList<String>();
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					if (TAG_ruleFiles.equals(parser.getLocalName())) {
						if (current.fields.isEmpty()) {
							error("Missing " + TAG_ruleFile + " tag", parser);
						}
						return current.ruleFiles;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_ruleFile.equals(element)) {
						String text = getTextTag(parser, TAG_ruleFile, null);
						if (text == null || text.isEmpty()) {
							error("Found unexpected empty tag: "+TAG_ruleFile, parser);
						}
						current.ruleFiles.add(text);
					}
					else if (TAG_methodName.equals(element)) {
						current.methodName = getTextTag(parser, TAG_ruleFile, current.methodName);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	private List<TemplateField> parseFields(XMLStreamReader parser, TemplateParseData current)
			throws XMLStreamException
	{
		if (current.fields != null) {
			error("Multiple " + TAG_fields + " tags found", parser);
		}
		current.fields = new ArrayList<TemplateField>();
		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_fields.equals(parser.getLocalName())) {
						if (current.fields.isEmpty()) {
							error("Missing " + TAG_field + " tag", parser);
						}
						return current.fields;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_field.equals(element)) {
						current.fields.add(parseField(parser));
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}
	
	private List<String> parseCategories(XMLStreamReader parser, TemplateParseData current)
			throws XMLStreamException
	{
		if (current.categories != null) {
			error("Multiple " + TAG_categories + " tags found", parser);
		}
		current.categories = new ArrayList<String>();
		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_categories.equals(parser.getLocalName())) {
						if (current.fields.isEmpty()) {
							error("Missing " + TAG_category + " tag", parser);
						}
						return current.categories;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_category.equals(element)) {
						String text = getTextTag(parser, TAG_category, null);
						if (text == null || text.isEmpty()) {
							error("Found unexpected empty tag: "+TAG_category, parser);
						}
						current.categories.add(text);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	private TemplateField parseField(XMLStreamReader parser) throws XMLStreamException {
		String name = getAttribute(parser, ATTR_name);
		String label = getAttribute(parser, ATTR_label, true);
		String hint = getAttribute(parser, ATTR_hint, true);
		String remoteResource = getAttribute(parser, ATTR_remoteResource, true);
		String stringRequired = getAttribute(parser, ATTR_required, true);
		boolean required = stringRequired != null ? Boolean.parseBoolean(stringRequired) : false;
		Cardinality cardinality = null;
		List<String> functionalPrefixes = null;
		List<String> functionalPrefixesIds = null;
		List<Ontology> correspondingOntologies = null;
		String preSelectedString = null;

		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_field.equals(parser.getLocalName())) {
						if (cardinality == null) {
							cardinality = TemplateField.SINGLE_FIELD_CARDINALITY;
						}
						boolean preSelected = preSelectedString != null ? Boolean.parseBoolean(preSelectedString) : true;
						return new TemplateField(name, label, hint, required, cardinality, functionalPrefixes, functionalPrefixesIds, preSelected, correspondingOntologies, remoteResource);
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_ontology.equals(element)) {
						Ontology ontology = parseOntology(parser);
						if (correspondingOntologies == null) {
							correspondingOntologies = Collections.singletonList(ontology);
						}
						else if (correspondingOntologies.size() == 1) {
							correspondingOntologies = new ArrayList<Ontology>(correspondingOntologies);
							correspondingOntologies.add(ontology);
						}
						else {
							correspondingOntologies.add(ontology);
						}
					}
					else if (TAG_cardinality.equals(element)) {
						if (cardinality != null) {
							error("Multiple " + TAG_cardinality + " tags found", parser);
						}
						cardinality = CardinalityHelper.parseCardinality(parseElementText(parser,
								TAG_cardinality));
					}
					else if (TAG_prefixes.equals(element)) {
						if (functionalPrefixes != null) {
							error("Multiple " + TAG_prefixes + " tags found", parser);
						}
						preSelectedString = getAttribute(parser, ATTR_preselected, true);
						Map<String, String> map = parsePrefixList(parser, TAG_prefixes, TAG_prefix);
						if (map != null && !map.isEmpty()) {
							functionalPrefixes = new ArrayList<String>(map.size());
							functionalPrefixesIds = new ArrayList<String>(map.size());
							for (Entry<String, String> entry : map.entrySet()) {
								functionalPrefixes.add(entry.getKey());
								final String value = entry.getValue();
								if (value != null) {
									functionalPrefixesIds.add(value);
								}
							}
							if (functionalPrefixesIds.size() < functionalPrefixes.size()) {
								// either all have an id or none, no middle ground!
								functionalPrefixesIds = null;
							}
						}
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	private List<Ontology> parseExternal(XMLStreamReader parser, TemplateParseData current)
			throws XMLStreamException
	{
		if (current.external != null) {
			error("Multiple " + TAG_external + " tags found", parser);
		}
		current.external = new ArrayList<Ontology>(1);
		while (true) {
			switch (parser.next()) {

				case XMLStreamConstants.END_ELEMENT:
					if (TAG_external.equals(parser.getLocalName())) {
						return current.external;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					String element = parser.getLocalName();
					if (TAG_ontology.equals(element)) {
						Ontology ontology = parseOntology(parser);
						current.external.add(ontology);
					}
					else {
						error("Unexpected tag: " + element, parser);
					}
					break;
			}
		}
	}

	static String getTextTag(XMLStreamReader parser, String tag, String value)
			throws XMLStreamException
	{
		String text = parseElementText(parser, tag);
		if (value != null) {
			error("Multiple " + tag + " tags found", parser);
		}
		return text;
	}

	private void add(List<TermTemplate> result, TemplateParseData current, XMLStreamReader parser) {
		if (current.correspondingOntology == null) {
			error("Missing " + TAG_ontology + " tag", parser);
		}
		if (current.description == null) {
			error("Missing " + TAG_description + " tag", parser);
		}
		if (current.fields == null) {
			error("Missing " + TAG_fields + " tag", parser);
		}
//		if (current.obo_namespace == null) {
//			error("Missing " + TAG_obonamespace + " tag", parser);
//		}
		if (current.ruleFiles == null || current.ruleFiles.isEmpty()) {
			error("Missing " + TAG_ruleFiles + " tag", parser);
		}
		TermTemplate template = new TermTemplate(current.correspondingOntology, current.name, current.displayName, current.description, current.fields, current.external, current.requires, current.obo_namespace, current.ruleFiles, current.methodName, current.hint, current.categories);
		result.add(template);
	}

	static void error(String message, XMLStreamReader parser) {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		Location location = parser.getLocation();
		if (location != null) {
			int lineNumber = location.getLineNumber();
			if (lineNumber >= 0) {
				sb.append(" at line number: ");
				sb.append(lineNumber);
			}
		}
		throw new RuntimeException(sb.toString());
	}

	static String getAttribute(XMLStreamReader parser, String attrName) {
		return getAttribute(parser, attrName, false);
	}

	static String getAttribute(XMLStreamReader parser, String attrName, boolean optional) {
		String value = parser.getAttributeValue(null, attrName);
		if (!optional) {
			if (value == null) {
				error("Missing Attribute: " + attrName, parser);
			}
			else if (value.isEmpty()) {
				error("Empty Attribute: " + attrName, parser);
			}
		}
		return value;
	}

	static String parseElementText(XMLStreamReader parser, String tag) throws XMLStreamException {
		final StringBuilder sb = new StringBuilder();
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					String element = parser.getLocalName();
					if (tag.equals(element)) {
						if (sb.length() == 0) {
							error("Empty" + tag + " tag", parser);
						}
						String text = sb.toString().trim();
						return text;
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					sb.append(parser.getText());
					break;
				case XMLStreamConstants.CDATA:
					sb.append(parser.getText());
					break;
				case XMLStreamConstants.START_ELEMENT:
					error("Unexpected element: " + parser.getLocalName(), parser);
					break;
			}
		}
	}

	static Map<String, String> parsePrefixList(XMLStreamReader parser, String tag, String subTag)
			throws XMLStreamException
	{
		final Map<String, String> prefixIds = new LinkedHashMap<String, String>();
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					if (tag.equals(parser.getLocalName())) {
						return prefixIds;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					if (subTag.equals(parser.getLocalName())) {
						String id = parser.getAttributeValue(null, ATTR_id);
						String text = parseElementText(parser, subTag);
						prefixIds.put(text, id);
					}
					break;
			}
		}
	}
}
