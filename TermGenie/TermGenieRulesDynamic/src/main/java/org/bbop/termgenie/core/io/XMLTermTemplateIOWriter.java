package org.bbop.termgenie.core.io;

import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;

/**
 * Write term templates into XML format.
 */
class XMLTermTemplateIOWriter implements XMLTermTemplateIOTags {

	private final XMLOutputFactory factory;

	XMLTermTemplateIOWriter() {
		super();
		factory = XMLOutputFactory.newInstance();
	}

	/**
	 * @param templates
	 * @param outputStream
	 * @throws XMLStreamException
	 */
	void writeTemplates(Collection<TermTemplate> templates, OutputStream outputStream) {
		try {
			XMLStreamWriter writer = createWriter(outputStream);
			writer.writeStartDocument();
			writer.writeStartElement(TAG_termgenietemplates);
			writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			writer.writeAttribute("xsi:noNamespaceSchemaLocation", "termgenie_rules.xsd");
			for (TermTemplate termTemplate : templates) {
				writer.writeStartElement(TAG_template);
				writer.writeAttribute(ATTR_name, termTemplate.getName());
				if (termTemplate.getDisplayName() != null) {
					writer.writeAttribute(ATTR_displayname, termTemplate.getDisplayName());
				}
				writeTag(TAG_description, termTemplate.getDescription(), writer);
				writeTag(TAG_hint, termTemplate.getHint(), writer);
				writeOntology(termTemplate.getCorrespondingOntology(), writer);
				writeExternal(termTemplate.getExternal(), writer);
				writeTag(TAG_obonamespace, termTemplate.getOboNamespace(), writer);
				writeRequires(termTemplate.getRequires(), writer);
				writeFields(termTemplate.getFields(), writer);
				writeRules(termTemplate.getRuleFiles(), termTemplate.getMethodName(), writer);
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();

		} catch (XMLStreamException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void writeRules(List<String> ruleFiles, String methodName, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(TAG_ruleFiles);
		if (methodName != null) {
			writeTag(TAG_methodName, methodName, writer);
		}
		for (String ruleFile : ruleFiles) {
			writeTag(TAG_ruleFile, ruleFile, writer);
		}
		writer.writeEndElement();	
	}

	private void writeFields(List<TemplateField> fields, XMLStreamWriter writer)
			throws XMLStreamException
	{
		writer.writeStartElement(TAG_fields);
		for (TemplateField templateField : fields) {
			writer.writeStartElement(TAG_field);
			writer.writeAttribute(ATTR_name, templateField.getName());
			if (templateField.isRequired()) {
				writer.writeAttribute(ATTR_required, Boolean.toString(true));
			}
			List<Ontology> ontologies = templateField.getCorrespondingOntologies();
			if (ontologies != null) {
				for (Ontology ontology : ontologies) {
					writeOntology(ontology, writer);
				}
			}
			Cardinality cardinality = templateField.getCardinality();
			if (cardinality.getMinimum() != 1 || cardinality.getMaximum() != 1) {
				writeTag(TAG_cardinality,
						CardinalityHelper.serializeCardinality(cardinality),
						writer);
			}
			List<String> prefixes = templateField.getFunctionalPrefixes();
			if (prefixes != null && !prefixes.isEmpty()) {
				writer.writeStartElement(TAG_prefixes);
				for (String prefix : prefixes) {
					writeTag(TAG_prefix, prefix, writer);
				}
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	private void writeRequires(List<String> requires, XMLStreamWriter writer)
			throws XMLStreamException
	{
		if (requires != null) {
			for (String require : requires) {
				writeTag(TAG_requires, require, writer);
			}
		}
	}

	private void writeExternal(List<Ontology> external, XMLStreamWriter writer)
			throws XMLStreamException
	{
		if (external != null) {
			writer.writeStartElement(TAG_external);
			for (Ontology ontology : external) {
				writeOntology(ontology, writer);
			}
			writer.writeEndElement();
		}
	}

	private void writeOntology(Ontology ontology, XMLStreamWriter writer) throws XMLStreamException
	{
		writer.writeStartElement(TAG_ontology);
		writer.writeAttribute(ATTR_name, ontology.getUniqueName());
		writeTag(TAG_branch, ontology.getBranch(), writer);
		writer.writeEndElement();
	}

	static void writeTag(String tag, String text, XMLStreamWriter writer) throws XMLStreamException
	{
		if (text != null) {
			writer.writeStartElement(tag);
			writer.writeCharacters(text);
			writer.writeEndElement();
		}
	}

	private XMLStreamWriter createWriter(OutputStream outputStream) throws XMLStreamException {
		XMLStreamWriter writer = factory.createXMLStreamWriter(outputStream);
		PrettyPrintHandler handler = new PrettyPrintHandler(writer);
		return (XMLStreamWriter) Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(),
				new Class[] { XMLStreamWriter.class },
				handler);
	}

	private static class PrettyPrintHandler implements InvocationHandler {

		private XMLStreamWriter target;

		private int depth = 0;
		private Map<Integer, Boolean> hasChild = new HashMap<Integer, Boolean>();

		private static final String INDENT_CHAR = "\t";
		private static final String LINEFEED_CHAR = "\n";

		public PrettyPrintHandler(XMLStreamWriter target) {
			this.target = target;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			String m = method.getName();

			// Needs to be BEFORE the actual event, so that for instance the
			// sequence writeStartElem, writeAttr, writeStartElem, writeEndElem,
			// writeEndElem
			// is correctly handled

			if ("writeStartElement".equals(m)) {
				// update state of parent node
				if (depth > 0) {
					hasChild.put(depth - 1, true);
				}

				// reset state of current node
				hasChild.put(depth, false);

				// indent for current depth
				target.writeCharacters(LINEFEED_CHAR);
				target.writeCharacters(repeat(depth, INDENT_CHAR));

				depth++;
			}

			if ("writeEndElement".equals(m)) {
				depth--;

				if (hasChild.get(depth) == true) {
					target.writeCharacters(LINEFEED_CHAR);
					target.writeCharacters(repeat(depth, INDENT_CHAR));
				}

			}

			if ("writeEmptyElement".equals(m)) {
				// update state of parent node
				if (depth > 0) {
					hasChild.put(depth - 1, true);
				}

				// indent for current depth
				target.writeCharacters(LINEFEED_CHAR);
				target.writeCharacters(repeat(depth, INDENT_CHAR));

			}

			method.invoke(target, args);

			return null;
		}

		private String repeat(int d, String s) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < d; i++) {
				sb.append(s);
			}
			return sb.toString();
		}
	}
}
