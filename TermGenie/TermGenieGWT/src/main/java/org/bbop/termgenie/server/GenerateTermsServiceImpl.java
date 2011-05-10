package org.bbop.termgenie.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TemplateField.Cardinality;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.DefaultTermTemplates;
import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.shared.FieldValidator;
import org.bbop.termgenie.shared.FieldValidator.GWTValidationHint;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;
import org.bbop.termgenie.shared.GenerationResponse;
import org.bbop.termgenie.shared.Pair;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GenerateTermsServiceImpl extends RemoteServiceServlet implements GenerateTermsService {

	private static final TemplateCache TEMPLATE_CACHE = TemplateCache.getInstance();

	@Override
	public GWTTermTemplate[] getAvailableGWTTermTemplates(String ontology) {
		Collection<TermTemplate> templates = getTermTemplates(ontology);
		if (templates.isEmpty()) {
			// short cut for empty results.
			return new GWTTermTemplate[0];
		}

		// encode the templates for GWT
		List<GWTTermTemplate> gwtTemplates = new ArrayList<GWTTermTemplate>();
		for (TermTemplate template : templates) {
			gwtTemplates.add(GWTTemplateTools.createGWTTermTemplate(template));
		}
		return gwtTemplates.toArray(new GWTTermTemplate[gwtTemplates.size()]);
	}

	@Override
	public GenerationResponse generateTerms(String ontology,
			Pair<GWTTermTemplate, GWTTermGenerationParameter>[] allParameters, boolean commit,
			String username, String password)
	{
		// Validation
		List<GWTValidationHint> allErrors = new ArrayList<FieldValidator.GWTValidationHint>();
		for (Pair<GWTTermTemplate, GWTTermGenerationParameter> pair : allParameters) {
			TermTemplate template = getTermTemplate(ontology, pair.getOne().getName());
			GWTTermGenerationParameter parameter = pair.getTwo();
			GWTTermTemplate gwtTermTemplate = GWTTemplateTools.createGWTTermTemplate(template);
			List<GWTValidationHint> errors = FieldValidator.validateParameters(gwtTermTemplate, parameter);
			if (!errors.isEmpty()) {
				allErrors.addAll(errors);
			}
		}
		// return errors
		if (!allErrors.isEmpty()) {
			return new GenerationResponse(allErrors, null);
		}

		// Call rule engine

		// Commit, if required

		// return response
		return null;
	}

	private Collection<TermTemplate> getTermTemplates(String ontology) {
		Collection<TermTemplate> templates;
		synchronized (TEMPLATE_CACHE) {
			templates = TEMPLATE_CACHE.getTemplates(ontology);
			if (templates == null) {
				templates = requestTemplates(ontology);
				TEMPLATE_CACHE.put(ontology, templates);
			}
		}
		return templates;
	}
	
	private TermTemplate getTermTemplate(String ontology, String name) {
		TermTemplate template;
		synchronized (TEMPLATE_CACHE) {
			template = TEMPLATE_CACHE.getTemplate(ontology, name);
			if (template == null) {
				Collection<TermTemplate> templates = TEMPLATE_CACHE.getTemplates(ontology);
				if (templates == null) {
					templates = requestTemplates(ontology);
					TEMPLATE_CACHE.put(ontology, templates);	
				}
				template = TEMPLATE_CACHE.getTemplate(ontology, name);
			}
		}
		return template;
	}

	/**
	 * Request the templates for a given ontology.
	 * 
	 * @param ontology
	 * @return templates, never null
	 */
	protected Collection<TermTemplate> requestTemplates(String ontology) {
		if ("GeneOntology".equals(ontology)) {
			return DefaultTermTemplates.defaultTemplates;
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Tools for converting a term generation details into the GWT specific
	 * (transfer) formats.
	 */
	static class GWTTemplateTools {

		/**
		 * Convert a single template into a GWT specific data structure.
		 * 
		 * @param template
		 * @return internal format
		 */
		static GWTTermTemplate createGWTTermTemplate(TermTemplate template) {
			GWTTermTemplate gwtTermTemplate = new GWTTermTemplate();
			gwtTermTemplate.setName(template.getName());
			List<TemplateField> fields = template.getFields();
			int size = fields.size();
			GWTTemplateField[] gwtFields = new GWTTemplateField[size];
			for (int i = 0; i < size; i++) {
				gwtFields[i] = createGWTTemplateField(fields.get(i));
			}
			gwtTermTemplate.setFields(gwtFields);
			return gwtTermTemplate;
		}

		private static GWTTemplateField createGWTTemplateField(TemplateField field) {
			GWTTemplateField gwtField = new GWTTemplateField();
			gwtField.setName(field.getName());
			gwtField.setRequired(field.isRequired());
			Cardinality c = field.getCardinality();
			gwtField.setCardinality(new GWTCardinality(c.getMinimum(), c.getMaximum()));
			gwtField.setFunctionalPrefixes(field.getFunctionalPrefixes().toArray(new String[0]));
			Ontology ontology = field.getCorrespondingOntology();
			if (ontology != null) {
				gwtField.setOntology(ontology.getUniqueName());
			}
			return gwtField;
		}
	}

	static class TemplateCache {
		private static volatile TemplateCache instance = null;
		private final Map<String, Map<String, TermTemplate>> templates;

		private TemplateCache() {
			templates = new HashMap<String, Map<String, TermTemplate>>();
		}

		public synchronized static TemplateCache getInstance() {
			if (instance == null) {
				instance = new TemplateCache();
			}
			return instance;
		}

		void put(String ontology, Collection<TermTemplate> templates) {
			Map<String, TermTemplate> namedValues = new HashMap<String, TermTemplate>();
			for (TermTemplate template : templates) {
				namedValues.put(template.getName(), template);
			}
			if (namedValues.isEmpty()) {
				namedValues = Collections.emptyMap();
			}
			this.templates.put(ontology, namedValues);
		}

		boolean hasOntology(String ontology) {
			return templates.containsKey(ontology);
		}

		Collection<TermTemplate> getTemplates(String ontology) {
			Map<String, TermTemplate> namedValues = templates.get(ontology);
			if (namedValues == null) {
				return null;
			}
			return namedValues.values();
		}

		TermTemplate getTemplate(String ontology, String templateName) {
			Map<String, TermTemplate> namedValues = templates.get(ontology);
			if (namedValues == null) {
				return null;
			}
			return namedValues.get(templateName);
		}
	}
}
