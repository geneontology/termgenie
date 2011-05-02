package org.bbop.termgenie.core.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;

public interface TermGenerationEngine {

	public List<OntologyTerm> generateTerms(TermTemplate templateName, TermGenerationParameters parameters);
	
	
	public final class TermGenerationParameters {
		
		private final Map<String, OntologyTerm> terms;
		
		private final Map<String, String> strings;
		
		public TermGenerationParameters() {
			terms = new HashMap<String, OntologyTerm>();
			strings = new HashMap<String, String>();
		}
		
		public OntologyTerm getOntologyTerm(TemplateField field) {
			return terms.get(getKey(field));
		}
		
		public OntologyTerm getOntologyTerm(TemplateField field, int pos) {
			return terms.get(getKey(field, pos));
		}

		public String getStringValue(TemplateField field) {
			return strings.get(getKey(field));
		}
		
		public void addOntologyTerm(OntologyTerm ontologyTerm, TemplateField field) {
			terms.put(getKey(field), ontologyTerm);
		}

		public void addOntologyTerm(OntologyTerm ontologyTerm, TemplateField field, int pos) {
			terms.put(getKey(field, pos), ontologyTerm);
		}
		
		public void addString(String value, TemplateField field) {
			strings.put(getKey(field), value);
		}

		private String getKey(TemplateField field) {
			return field.getName();
		}

		private String getKey(TemplateField field, int pos) {
			return getKey(field)+"|"+Integer.toString(pos);
		}
	}
}
