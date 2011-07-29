package org.bbop.termgenie.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Tool for reading and writing templates to file in line based format.
 */
@Singleton
class FlatFileTermTemplateIO implements TermTemplateIO {
	
	private static final String FLAT_FILE_TAG_RULE = "RULE";
	private static final String FLAT_FILE_TAG_HINT = "hint";
	private static final String FLAT_FILE_TAG_ONTOLOGY = "ontology";
	private static final String FLAT_FILE_TAG_EXTERNAL = "external";
	private static final String FLAT_FILE_TAG_OBO_NAMESPACE = "obo_namespace";
	private static final String FLAT_FILE_TAG_REQUIRES = "requires";
	private static final String FLAT_FILE_TAG_PREFIXES = "prefixes";
	private static final String FLAT_FILE_TAG_CARDINALITY = "cardinality";
	private static final String FLAT_FILE_TAG_REQUIRED = "required";
	private static final String FLAT_FILE_TAG_DESCRIPTION = "description";
	private static final String FLAT_FILE_TAG_NAME = "name";
	private static final String FLAT_FILE_TAG_DISPLAY_NAME = "displayname";
	private static final String FLAT_FILE_TAG_TEMPLATE = "TEMPLATE";
	private static final String FLAT_FILE_TAG_FIELD = "FIELD";

	private final TemplateOntologyHelper helper;
	
	/**
	 * @param helper
	 */
	@Inject
	FlatFileTermTemplateIO(TemplateOntologyHelper helper) {
		super();
		this.helper = helper;
	}

	public void writeTemplates(Collection<TermTemplate> templates, StringBuilder writer) {
		for (TermTemplate template : templates) {
			writeTemplate(template, writer);
			newLine(writer);
			newLine(writer);
		}
	}
	
	private static void newLine(StringBuilder sb) {
		sb.append('\n');
	}
	
	@Override
	public void writeTemplates(Collection<TermTemplate> templates, OutputStream outputStream) {
		try {
			StringBuilder sb = new StringBuilder();
			writeTemplates(templates, sb);
			IOUtils.write(sb, outputStream, "UTF-8");
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	public void writeTemplates(Collection<TermTemplate> templates, File outputFile) {
		try {
			StringBuilder sb = new StringBuilder();
			writeTemplates(templates, sb);
			FileUtils.write(outputFile, sb, "UTF-8");
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private static char SEPARATOR_CHAR = '\t';
	private static char COMMENT_CHAR = '#';
	
	private void writeTemplate(TermTemplate template, StringBuilder writer) {
		writer.append(FLAT_FILE_TAG_TEMPLATE);
		newLine(writer);
		
		// name
		writer.append(FLAT_FILE_TAG_NAME).append(SEPARATOR_CHAR).append(template.getName());
		newLine(writer);
		
		// display name
		String displayName = template.getDisplayName();
		if (displayName != null) {
			writer.append(FLAT_FILE_TAG_DISPLAY_NAME).append(SEPARATOR_CHAR).append(displayName);
			newLine(writer);
		}
		
		// description
		String description = template.getDescription();
		if (description != null) {
			writer.append(FLAT_FILE_TAG_DESCRIPTION).append(SEPARATOR_CHAR).append(description);
			newLine(writer);
		}
		
		// hint
		final String hint = template.getHint();
		if (hint != null) {
			writer.append(FLAT_FILE_TAG_HINT).append(SEPARATOR_CHAR).append(hint);
			newLine(writer);
		}
		// ontology
		addOntology(template.getCorrespondingOntology(), writer);
		
		// external
		addOntologies(FLAT_FILE_TAG_EXTERNAL, template.getExternal(), writer);
		
		// obo_namespace
		final String oboNamespace = template.getOboNamespace();
		if (oboNamespace != null) {
			writer.append(FLAT_FILE_TAG_OBO_NAMESPACE).append(SEPARATOR_CHAR).append(oboNamespace);
			newLine(writer);
		}
		
		// requires
		final List<String> requires = template.getRequires();
		if (requires != null && !requires.isEmpty()) {
			writer.append(FLAT_FILE_TAG_REQUIRES).append(SEPARATOR_CHAR).append(ListHelper.serializeList(requires, SEPARATOR_CHAR));
			newLine(writer);
		}
		
		// template fields
		for (TemplateField templateField : template.getFields()) {
			writer.append(COMMENT_CHAR);
			newLine(writer);
			
			writer.append(FLAT_FILE_TAG_FIELD);
			newLine(writer);
			
			// String name
			writer.append(FLAT_FILE_TAG_NAME).append(SEPARATOR_CHAR).append(templateField.getName());
			newLine(writer);
			
			// boolean required
			if (templateField.isRequired()) {
				// assume not required as default.
				writer.append(FLAT_FILE_TAG_REQUIRED).append(SEPARATOR_CHAR).append(Boolean.toString(templateField.isRequired()));
				newLine(writer);
			}
			
			// Cardinality cardinality;
			Cardinality cardinality = templateField.getCardinality();
			if (cardinality != null) {
				String serializedCardinality = CardinalityHelper.serializeCardinality(cardinality);
				if (serializedCardinality != null) {
					writer.append(FLAT_FILE_TAG_CARDINALITY).append(SEPARATOR_CHAR);
					writer.append(serializedCardinality);
					newLine(writer);
				}
			}
			
			// List<String> functionalPrefixes;
			List<String> prefixes = templateField.getFunctionalPrefixes();
			if (!prefixes.isEmpty()) {
				writer.append(FLAT_FILE_TAG_PREFIXES).append(SEPARATOR_CHAR).append(ListHelper.serializeList(prefixes, SEPARATOR_CHAR));
				newLine(writer);
			}
			
			// Ontology correspondingOntology;
			addOntologies(templateField.getCorrespondingOntologies(), writer);
		}
		// rule
		String rules = template.getRules();
		if (!rules.isEmpty()) {
			writer.append(COMMENT_CHAR);
			newLine(writer);
			writer.append(FLAT_FILE_TAG_RULE);
			newLine(writer);
			writer.append(rules);
			newLine(writer);
		}
	}
	
	private void addOntologies(List<Ontology> ontologies, StringBuilder writer) {
		addOntologies(FLAT_FILE_TAG_ONTOLOGY, ontologies, writer);
	}
	
	private void addOntologies(String tag, List<Ontology> ontologies, StringBuilder writer) {
		if (ontologies != null && !ontologies.isEmpty()) {
			writer.append(tag).append(SEPARATOR_CHAR).append(helper.serializeOntologies(ontologies));
			newLine(writer);
		}
	}
	
	private void addOntology(Ontology ontology, StringBuilder writer) {
		addOntologies(Collections.singletonList(ontology), writer);
	}

	private class ReadData {
		String ontology = null;
		String name = null;
		String displayname = null;
		String description = null;
		String hint = null;
		List<Map<String, String>> fields=new ArrayList<Map<String,String>>();
		Map<String, String> currentField = null;
		StringBuilder rules = null;
		String external = null;
		String requires = null;
		String obo_namespace = null;
		
		void newCurrentField() {
			currentField = new HashMap<String, String>();
			fields.add(currentField);
		}
		
		void newRuleBuffer() {
			rules = new StringBuilder();
		}
		
		TermTemplate createTermTemplate() {
			List<Ontology> ontologies = getOntologies(ontology);
			List<Ontology> external = this.external == null ? null : getOntologies(this.external);
			TermTemplate termTermplate = new TermTemplate(
					ontologies.get(0), 
					name, 
					displayname, 
					description, 
					createFields(), 
					external,
					ListHelper.parseString(requires, SEPARATOR_CHAR),
					obo_namespace,
					rules != null ? rules.toString() : null, 
					hint);
			return termTermplate;
		}
		
		private List<Ontology> getOntologies(String ontologiesString) {
			return helper.readOntologies(ontologiesString);
		}

		private List<TemplateField> createFields() {
			List<TemplateField> fields = new ArrayList<TemplateField>(this.fields.size());
			for (Map<String, String> map : this.fields) {
				TemplateField templateField = new TemplateField(
						map.get(FLAT_FILE_TAG_NAME), 
						parseRequired(map), 
						CardinalityHelper.parseCardinality(map.get(FLAT_FILE_TAG_CARDINALITY)), 
						ListHelper.parseString(map.get(FLAT_FILE_TAG_PREFIXES), SEPARATOR_CHAR), 
						helper.readOntologies(map.get(FLAT_FILE_TAG_ONTOLOGY)));
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
	
	@SuppressWarnings("null")
	@Override
	public List<TermTemplate> readTemplates(InputStream inputStream) throws IOException {
		int count = 0;
		List<TermTemplate> result = new ArrayList<TermTemplate>();
		ReadData data = null;
		LineIterator lineIterator = IOUtils.lineIterator(inputStream, "UTF-8");
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
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
			} else if(line.startsWith(FLAT_FILE_TAG_HINT)) {
				data.hint = value;
			} else if(line.startsWith(FLAT_FILE_TAG_DISPLAY_NAME)) {
				data.displayname = value;
			} else if (line.startsWith(FLAT_FILE_TAG_DESCRIPTION)) {
				data.description = value;
			} else if (line.startsWith(FLAT_FILE_TAG_EXTERNAL)) {
				data.external = value;
			} else if (line.startsWith(FLAT_FILE_TAG_OBO_NAMESPACE)) {
				data.obo_namespace = value;
			} else if (line.startsWith(FLAT_FILE_TAG_REQUIRES)) {
				data.requires = value;
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
				data.newRuleBuffer();
			} else if (data.rules != null) {
				if (data.rules.length() > 0) {
					data.rules.append('\n');
				}
				data.rules.append(line);
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
