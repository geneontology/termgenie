package org.bbop.termgenie.core.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateRule;
import org.bbop.termgenie.core.TermTemplate;

/**
 * Tool for reading and writing templates to file in line based format.
 */
public class FlatFileTermTemplateIO {
	
	private static final String FLAT_FILE_TAG_RULE = "RULE";
	private static final String FLAT_FILE_TAG_ONTOLOGY = "ontology";
	private static final String FLAT_FILE_TAG_PREFIXES = "prefixes";
	private static final String FLAT_FILE_TAG_CARDINALITY = "cardinality";
	private static final String FLAT_FILE_TAG_REQUIRED = "required";
	private static final String FLAT_FILE_TAG_DESCRIPTION = "description";
	private static final String FLAT_FILE_TAG_NAME = "name";
	private static final String FLAT_FILE_TAG_TEMPLATE = "TEMPLATE";
	private static final String FLAT_FILE_TAG_FIELD = "FIELD";

	public void writeTemplates(Collection<TermTemplate> templates, BufferedWriter writer) throws IOException {
		for (TermTemplate template : templates) {
			writeTemplate(template, writer);
			writer.newLine();
			writer.newLine();
		}
	}
	
	public void writeTemplates(Collection<TermTemplate> templates, File outputFile) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			writeTemplates(templates, writer);
		} catch (UnsupportedEncodingException exception) {
			throw new RuntimeException(exception);
		} catch (FileNotFoundException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		}

	}
	
	private static char SEPARATOR_CHAR = '\t';
	private static char COMMENT_CHAR = '#';
	
	private void writeTemplate(TermTemplate template, BufferedWriter writer) throws IOException {
		writer.append(FLAT_FILE_TAG_TEMPLATE);
		writer.newLine();
		
		// name
		writer.append(FLAT_FILE_TAG_NAME).append(SEPARATOR_CHAR).append(template.getName());
		writer.newLine();
		
		// description
		String description = template.getDescription();
		if (description != null) {
			writer.append(FLAT_FILE_TAG_DESCRIPTION).append(SEPARATOR_CHAR).append(description);
			writer.newLine();
		}
		// ontologies
		addOntologies(template.getCorrespondingOntologies(), writer);
		
		// template fields
		for (TemplateField templateField : template.getFields()) {
			writer.append(COMMENT_CHAR);
			writer.newLine();
			
			writer.append(FLAT_FILE_TAG_FIELD);
			writer.newLine();
			
			// String name
			writer.append(FLAT_FILE_TAG_NAME).append(SEPARATOR_CHAR).append(templateField.getName());
			writer.newLine();
			
			// boolean required
			if (templateField.isRequired()) {
				// assume not required as default.
				writer.append(FLAT_FILE_TAG_REQUIRED).append(SEPARATOR_CHAR).append(Boolean.toString(templateField.isRequired()));
				writer.newLine();
			}
			
			// Cardinality cardinality;
			Cardinality cardinality = templateField.getCardinality();
			if (cardinality != null) {
				String serializedCardinality = CardinalityHelper.serializeCardinality(cardinality);
				if (serializedCardinality != null) {
					writer.append(FLAT_FILE_TAG_CARDINALITY).append(SEPARATOR_CHAR);
					writer.append(serializedCardinality);
					writer.newLine();
				}
			}
			
			// List<String> functionalPrefixes;
			List<String> prefixes = templateField.getFunctionalPrefixes();
			if (!prefixes.isEmpty()) {
				writer.append(FLAT_FILE_TAG_PREFIXES).append(SEPARATOR_CHAR).append(ListHelper.serializeList(prefixes, SEPARATOR_CHAR));
				writer.newLine();
			}
			
			// Ontology correspondingOntology;
			addOntologies(templateField.getCorrespondingOntologies(), writer);
		}
		// rule
		List<TemplateRule> rules = template.getRules();
		if (!rules.isEmpty()) {
			writer.append(COMMENT_CHAR);
			writer.newLine();
			for (TemplateRule rule : rules) {
				writer.append(FLAT_FILE_TAG_RULE).append(SEPARATOR_CHAR).append(rule.getName());
				writer.newLine();
				writer.append(rule.getValue());
				writer.newLine();
			}
		}
	}
	
	private void addOntologies(List<Ontology> ontologies, BufferedWriter writer) throws IOException {
		if (ontologies != null && !ontologies.isEmpty()) {
			writer.append(FLAT_FILE_TAG_ONTOLOGY).append(SEPARATOR_CHAR).append(OntologyHelper.serializeOntologies(ontologies));
			writer.newLine();
		}
	}

	private class ReadData {
		String ontology = null;
		String name = null;
		String description = null;
		List<Map<String, String>> fields=new ArrayList<Map<String,String>>();
		Map<String, String> currentField = null;
		Map<String, StringBuilder> rules = new LinkedHashMap<String, StringBuilder>();
		StringBuilder currentRule = null;
		
		void newCurrentField() {
			currentField = new HashMap<String, String>();
			fields.add(currentField);
		}
		
		void newRuleBuffer(String name) {
			currentRule = new StringBuilder();
			rules.put(name, currentRule);
		}
		
		TermTemplate createTermTemplate() {
			List<Ontology> ontologies = getOntologies(ontology);
			TermTemplate termTermplate = new TermTemplate(ontologies.get(0), name, description, createFields(), createRule(rules));
			return termTermplate;
		}
		
		private List<TemplateRule> createRule(Map<String, StringBuilder> values) {
			List<TemplateRule> list = new ArrayList<TemplateRule>(values.size());
			for (String name : values.keySet()) {
				list.add(new TemplateRule(name, values.get(name).toString()));
			}
			return list;
		}
		
		private List<Ontology> getOntologies(String ontologiesString) {
			return OntologyHelper.readOntologies(ontologiesString);
		}

		private List<TemplateField> createFields() {
			List<TemplateField> fields = new ArrayList<TemplateField>(this.fields.size());
			for (Map<String, String> map : this.fields) {
				TemplateField templateField = new TemplateField(
						map.get(FLAT_FILE_TAG_NAME), 
						parseRequired(map), 
						CardinalityHelper.parseCardinality(map.get(FLAT_FILE_TAG_CARDINALITY)), 
						ListHelper.parseString(map.get(FLAT_FILE_TAG_PREFIXES), SEPARATOR_CHAR), 
						OntologyHelper.readOntologies(map.get(FLAT_FILE_TAG_ONTOLOGY)));
				fields.add(templateField);
			}
			return fields;
		}

		protected boolean parseRequired(Map<String, String> map) {
			String requiredString = map.get(FLAT_FILE_TAG_REQUIRED);
			if (requiredString != null) {
				return Boolean.parseBoolean(requiredString);
			}
			// assume a field as not required, if not specified
			return false;
		}
		
	}
	
	public List<TermTemplate> readTemplates(BufferedReader reader) throws IOException {
		int count = 0;
		String line;
		List<TermTemplate> result = new ArrayList<TermTemplate>();
		ReadData data = null;

		while ((line = reader.readLine()) != null) {
			count++;
			if (line.length() == 0 || line.charAt(0) == COMMENT_CHAR) {
				continue;
			}
			String value = subStringAfterSep(line);
			if (line.startsWith(FLAT_FILE_TAG_TEMPLATE)) {
				if (data != null) {
					// create TermTemplate from data
					TermTemplate template = data.createTermTemplate();
					result.add(template);
				}
				// start new data collection
				data = new ReadData();
			} else if (line.startsWith(FLAT_FILE_TAG_FIELD)) {
				data.newCurrentField();
			} else if (line.startsWith(FLAT_FILE_TAG_NAME)) {
				if (data.currentField == null) {
					data.name = value;
				} else {
					data.currentField.put(FLAT_FILE_TAG_NAME, value);
				}
			} else if (line.startsWith(FLAT_FILE_TAG_DESCRIPTION)) {
				data.description = value;
			} else if (line.startsWith(FLAT_FILE_TAG_ONTOLOGY)) {
				if (data.currentField == null) {
					data.ontology = value;
				} else {
					data.currentField.put(FLAT_FILE_TAG_ONTOLOGY, value);
				}
			} else if (line.startsWith(FLAT_FILE_TAG_REQUIRED)) {
				handleField(data, FLAT_FILE_TAG_REQUIRED, value, count);
			} else if (line.startsWith(FLAT_FILE_TAG_PREFIXES)) {
				handleField(data, FLAT_FILE_TAG_PREFIXES, value, count);
			} else if (line.startsWith(FLAT_FILE_TAG_CARDINALITY)) {
				handleField(data, FLAT_FILE_TAG_CARDINALITY, value, count);
			} else if (line.startsWith(FLAT_FILE_TAG_RULE)) {
				data.newRuleBuffer(value);
			} else if (data.currentRule != null) {
				if (data.currentRule.length() > 0) {
					data.currentRule.append('\n');
				}
				data.currentRule.append(line);
			}
		}
		if (data != null) {
			TermTemplate template = data.createTermTemplate();
			result.add(template);
		} else {
			throw new RuntimeException("Error: no templates read");
		}
		return result;
	}
	
	private void handleField(ReadData data, String tag, String value, int count) {
		if (data.currentField == null) {
			throw new RuntimeException("Syntax error on line: "+count+"\n unexecpected "+tag+" tag. May be you forgot the "+FLAT_FILE_TAG_FIELD+" tag to start a new term field");
		}
		data.currentField.put(tag, value);
	}

	static String subStringAfterSep(String string) {
		if (string != null) {
			int pos = string.indexOf(SEPARATOR_CHAR);
			if (pos > 0 && (pos + 1) < string.length()) {
				return string.substring(pos + 1);
			}
		}
		return null;
	}
	
}
