package org.bbop.termgenie.ontology.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.tools.ResourceLoader;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class XMLOntologyConfiguration extends ResourceLoader implements OntologyConfiguration {

	static final String SETTINGS_FILE = "ontology-configuration.xml";

	private static final String TAG_ontologyconfiguration = "ontologyconfiguration";
	private static final String TAG_ontology = "ontology";
	private static final String TAG_source = "source";
	private static final String TAG_supports = "supports";
	private static final String TAG_support = "support";
	private static final String TAG_requires = "requires";
	private static final String TAG_name = "name";
	private static final String TAG_roots = "roots";
	private static final String TAG_root = "root";
	private static final String TAG_ontologybranch = "ontologybranch";

	private static final String ATTR_name = "name";

	private final Map<String, ConfiguredOntology> configuration;

	@Inject
	XMLOntologyConfiguration(@Named("XMLOntologyConfigurationResource") String resource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles)
	{
		super(tryResourceLoadAsFiles);
		configuration = loadOntologyConfiguration(resource);
	}

	@Override
	public Map<String, ConfiguredOntology> getOntologyConfigurations() {
		return configuration;
	}

	private Map<String, ConfiguredOntology> loadOntologyConfiguration(String resource) {
		InputStream inputStream = null;
		try {
			inputStream = loadResource(resource);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(inputStream);

			Map<String, ConfiguredOntology> map = null;
			for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					String element = parser.getLocalName();
					if (TAG_ontologyconfiguration.equals(element)) {
						if (map != null) {
							error("Multiple " + TAG_ontologyconfiguration + " tags found", parser);
						}
						map = new LinkedHashMap<String, ConfiguredOntology>();
					}
					else if (TAG_ontology.equals(element)) {
						if (map == null) {
							error("No " + TAG_ontologyconfiguration + " top level element found.",
									parser);
						}
						parseOntology(parser, map);
					}
				}
			}
			parser.close();
			return Collections.unmodifiableMap(map);
		} catch (XMLStreamException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private void error(String message, XMLStreamReader parser) {
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

	private void parseOntology(XMLStreamReader parser, Map<String, ConfiguredOntology> ontologies)
			throws XMLStreamException
	{
		String name = getAttribute(parser, ATTR_name);
		ConfiguredOntology current = new ConfiguredOntology(name);
		while (true) {
			int event = parser.next();
			if (event == XMLStreamConstants.END_ELEMENT) {
				String element = parser.getLocalName();
				if (TAG_ontology.equals(element)) {
					addOntology(current, ontologies, parser);
					return;
				}
			}
			if (event == XMLStreamConstants.START_ELEMENT) {
				String element = parser.getLocalName();
				if (TAG_source.equals(element)) {
					String text = parseElement(parser, TAG_source);
					if (current.source != null) {
						error("Multiple " + TAG_source + " tags found", parser);
					}
					current.source = text;
				}
				else if (TAG_supports.equals(element)) {
					parseSupports(parser, current);
				}
				else if (TAG_requires.equals(element)) {
					parseRequires(parser, current);
				}
				else if (TAG_roots.equals(element)) {
					parseRoots(parser, current);
				}
				else if (TAG_ontologybranch.equals(element)) {
					parseOntologyBranch(parser, current, ontologies);
				}
				else {
					error("Unexpected tag: " + element, parser);
				}
			}
		}
	}

	private void parseSupports(XMLStreamReader parser, ConfiguredOntology current)
			throws XMLStreamException
	{
		if (current.supports != null) {
			error("Multiple " + TAG_supports + " tags found", parser);
		}
		current.setSupport(parseList(parser, TAG_supports, TAG_support));
	}

	private void parseRequires(XMLStreamReader parser, ConfiguredOntology current)
			throws XMLStreamException
	{
		if (current.requires != null) {
			error("Multiple " + TAG_requires + " tags found", parser);
		}
		current.setRequires(parseList(parser, TAG_requires, TAG_name));
	}

	private void parseRoots(XMLStreamReader parser, ConfiguredOntology current)
			throws XMLStreamException
	{
		if (current.getRoots() != null) {
			error("Multiple " + TAG_roots + " tags found", parser);
		}
		current.setRoots(parseList(parser, TAG_roots, TAG_root));
	}

	private void parseOntologyBranch(XMLStreamReader parser,
			ConfiguredOntology current,
			Map<String, ConfiguredOntology> ontologies) throws XMLStreamException
	{
		String name = getAttribute(parser, ATTR_name);
		List<String> roots = null;
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					if (TAG_ontologybranch.equals(parser.getLocalName())) {
						addOntology(current.createBranch(name, roots), ontologies, parser);
						return;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					if (TAG_roots.equals(parser.getLocalName())) {
						roots = parseList(parser, TAG_roots, TAG_root);
					}
					else {
						error("Unexpected element: " + parser.getLocalName(), parser);
					}
					break;
			}
		}
	}

	private void addOntology(ConfiguredOntology ontology,
			Map<String, ConfiguredOntology> ontologies,
			XMLStreamReader parser)
	{
		if (ontology.source == null) {
			error("Missing tag: " + TAG_source, parser);
		}
		String name = ontology.getUniqueName();
		String branch = ontology.getBranch();
		ontologies.put(branch != null ? branch : name, ontology);
	}

	private List<String> parseList(XMLStreamReader parser, String tag, String subTag)
			throws XMLStreamException
	{
		List<String> result = null;
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					if (tag.equals(parser.getLocalName())) {
						return result;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					if (subTag.equals(parser.getLocalName())) {
						String text = parseElement(parser, subTag);
						if (result == null) {
							result = Collections.singletonList(text);
						}
						else if (result.size() == 1) {
							result = new ArrayList<String>(result);
							result.add(text);
						}
						else {
							result.add(text);
						}
					}
					break;
			}
		}
	}

	private String parseElement(XMLStreamReader parser, String tag) throws XMLStreamException {
		String text = null;
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					String element = parser.getLocalName();
					if (tag.equals(element)) {
						if (text == null || text.isEmpty()) {
							error("Empty" + tag + " tag", parser);
						}
						if (text != null) {
							text = text.trim();
						}
						return text;
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					text = parser.getText();
					break;
				case XMLStreamConstants.START_ELEMENT:
					error("Unexpected element: " + parser.getLocalName(), parser);
					break;
			}
		}
	}

	private String getAttribute(XMLStreamReader parser, String attrName) {
		String value = parser.getAttributeValue(null, attrName);
		if (value == null) {
			error("Missing Attribute: " + attrName, parser);
		}
		else if (value.isEmpty()) {
			error("Empty Attribute: " + attrName, parser);
		}
		return value;
	}

	public static void main(String[] args) {
		XMLOntologyConfiguration c = new XMLOntologyConfiguration(SETTINGS_FILE, false);
		Map<String, ConfiguredOntology> ontologies = c.getOntologyConfigurations();
		for (String key : ontologies.keySet()) {
			ConfiguredOntology ontology = ontologies.get(key);
			System.out.print(ontology.getUniqueName());
			if (ontology.getBranch() != null) {
				System.out.print(" - ");
				System.out.print(ontology.getBranch());
			}
			System.out.println();
			System.out.print("roots: ");
			System.out.println(ontology.getRoots());
			System.out.print("source: ");
			System.out.println(ontology.source);
			for (String support : ontology.getSupports()) {
				System.out.println("Support: " + support);
			}
			for (String requires : ontology.getRequires()) {
				System.out.println("Requires: " + requires);
			}
			System.out.println();
		}
	}
}
