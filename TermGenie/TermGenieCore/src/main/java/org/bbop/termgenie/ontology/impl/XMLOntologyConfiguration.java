package org.bbop.termgenie.ontology.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologySubset;
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
	private static final String TAG_roots = "roots";
	private static final String TAG_root = "root";
	private static final String TAG_dlquery = "dlquery";
	private static final String TAG_ontologybranch = "ontologybranch";
	private static final String TAG_importrewrites = "importrewrites";
	private static final String TAG_importrewrite = "importrewrite";

	private static final String ATTR_name = "name";
	
	private static final String ATTR_source = "source";
	private static final String ATTR_target = "target";

	private final Ontology configuration;

	@Inject
	XMLOntologyConfiguration(@Named("XMLOntologyConfigurationResource") String resource,
			@Named("TryResourceLoadAsFiles") boolean tryResourceLoadAsFiles)
	{
		super(tryResourceLoadAsFiles);
		configuration = loadOntologyConfiguration(resource);
	}

	@Override
	public Ontology getOntologyConfiguration() {
		return configuration;
	}

	private Ontology loadOntologyConfiguration(String resource) {
		InputStream inputStream = null;
		try {
			inputStream = loadResource(resource);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(inputStream);

			Ontology config = null;
			for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
				if (event == XMLStreamConstants.START_ELEMENT) {
					String element = parser.getLocalName();
					if (TAG_ontologyconfiguration.equals(element)) {
						if (config != null) {
							error("Multiple " + TAG_ontologyconfiguration + " tags found", parser);
						}
					}
					else if (TAG_ontology.equals(element)) {
						if (config != null) {
							error("Multiple " + TAG_ontology + " tags found. Only one main ontology is supported", parser);
						}
						config = parseOntology(parser);
					}
				}
			}
			parser.close();
			if (config == null) {
				 throw new RuntimeException("No ontology found in resource: "+resource);
			}
			return config;
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

	private Ontology parseOntology(XMLStreamReader parser)
			throws XMLStreamException
	{
		String name = getAttribute(parser, ATTR_name);
		Ontology current = new Ontology();
		current.setName(name);
		List<OntologySubset> subsets = null;
		while (true) {
			int event = parser.next();
			if (event == XMLStreamConstants.END_ELEMENT) {
				String element = parser.getLocalName();
				if (TAG_ontology.equals(element)) {
					current.setSubsets(subsets);
					return current;
				}
			}
			if (event == XMLStreamConstants.START_ELEMENT) {
				String element = parser.getLocalName();
				if (TAG_source.equals(element)) {
					String text = parseElement(parser, TAG_source);
					if (current.getSource() != null) {
						error("Multiple " + TAG_source + " tags found", parser);
					}
					current.setSource(text);
				}
				else if (TAG_supports.equals(element)) {
					parseSupports(parser, current);
				}
				else if (TAG_roots.equals(element)) {
					parseRoots(parser, current);
				}
				else if (TAG_dlquery.equals(element)) {
					String text = parseElement(parser, TAG_dlquery);
					if (current.getDlQuery() != null) {
						error("Multiple " + TAG_dlquery + " tags found", parser);
					}
					current.setDlQuery(text);
				}
				else if (TAG_ontologybranch.equals(element)) {
					OntologySubset subset = parseOntologyBranch(parser);
					if (subset != null) {
						if (subsets == null) {
							subsets = new ArrayList<OntologySubset>();
						}
						subsets.add(subset);
					}
				}
				else if (TAG_importrewrites.equals(element)) {
					parseImportRewrites(parser, current);
				}
				else {
					error("Unexpected tag: " + element, parser);
				}
			}
		}
	}

	private void parseSupports(XMLStreamReader parser, Ontology current)
			throws XMLStreamException
	{
		if (current.getAdditionals() != null) {
			error("Multiple " + TAG_supports + " tags found", parser);
		}
		current.setAdditionals(parseList(parser, TAG_supports, TAG_support));
	}

	private void parseRoots(XMLStreamReader parser, Ontology current)
			throws XMLStreamException
	{
		if (current.getRoots() != null) {
			error("Multiple " + TAG_roots + " tags found", parser);
		}
		current.setRoots(parseList(parser, TAG_roots, TAG_root));
	}
	
	private void parseImportRewrites(XMLStreamReader parser, Ontology current)
			throws XMLStreamException
	{
		if (current.getImportRewrites() != null) {
			error("Multiple " + TAG_importrewrites + " tags found", parser);
		}
		current.setImportRewrites(parseMap(parser, TAG_importrewrites, TAG_importrewrite, ATTR_source, ATTR_target));
	}

	private OntologySubset parseOntologyBranch(XMLStreamReader parser) throws XMLStreamException
	{
		String name = getAttribute(parser, ATTR_name);
		List<String> roots = null;
		String dlQuery = null;
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT: {
					String element = parser.getLocalName();
					if (TAG_ontologybranch.equals(element)) {
						if (dlQuery != null || roots != null && !roots.isEmpty()) {
							return new OntologySubset(name, roots, dlQuery);
						}
						error("No valid roots or DL-Query found for ontology branch", parser);
					}
					break; 
				}
				case XMLStreamConstants.START_ELEMENT: {
					String element = parser.getLocalName();
					if (TAG_roots.equals(element)) {
						roots = parseList(parser, TAG_roots, TAG_root);
					}
					else if (TAG_dlquery.equals(element)) {
						String text = parseElement(parser, TAG_dlquery);
						if (dlQuery != null) {
							error("Multiple " + TAG_dlquery + " tags found", parser);
						}
						dlQuery = text;
					}
					else {
						error("Unexpected element: " + element, parser);
					}
					break;
				}
			}
		}
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
	
	private Map<String, String> parseMap(XMLStreamReader parser, String tag, String subTag, String keyTag, String valueTag)
			throws XMLStreamException
	{
		Map<String, String> result = null;
		while (true) {
			switch (parser.next()) {
				case XMLStreamConstants.END_ELEMENT:
					if (tag.equals(parser.getLocalName())) {
						return result;
					}
					break;
				case XMLStreamConstants.START_ELEMENT:
					if (subTag.equals(parser.getLocalName())) {
						String key = getAttribute(parser, keyTag);
						String value = getAttribute(parser, valueTag);
						if (result == null) {
							result = new HashMap<String, String>();
						}
						result.put(key, value);
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
				case XMLStreamConstants.CDATA:
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
		Ontology ontology = c.getOntologyConfiguration();
		System.out.println(ontology.getName());
		System.out.print("roots: ");
		System.out.println(ontology.getRoots());
		System.out.print("source: ");
		System.out.println(ontology.getSource());
		if (ontology.getAdditionals() != null) {
			for (String support : ontology.getAdditionals()) {
				System.out.println("support: " + support);
			}
		}
		if (ontology.getSubsets() != null) {
			for(OntologySubset subset : ontology.getSubsets()) {
				System.out.print("subset: ");
				System.out.print(subset.getName());
				if (subset.getDlQuery() != null) {
					System.out.print("DL: "+subset.getDlQuery());
				}
				if (subset.getRoots() != null) {
					System.out.print(subset.getRoots());
				}
				System.out.println();
			}
		}
		System.out.println();
	}
}
