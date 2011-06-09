package org.bbop.termgenie.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.OntologyAware.Ontology;
import org.bbop.termgenie.core.OntologyAware.OntologyTerm;
import org.bbop.termgenie.core.TemplateField;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationParameters;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class Patterns extends BasicRules {

	private final Map<String, Method> methods;
	
	protected Patterns(TermTemplate...templates) {
		super();
		methods = new HashMap<String, Method>();
	    for(Method method : getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(ToMatch.class)) {
				String name = method.getName();
				methods.put(name, method);
			}
	    }
	    checkAvailablePatterns(templates);
	}
	
	private void checkAvailablePatterns(TermTemplate...templates) {
		for (TermTemplate termTemplate : templates) {
			Method method = methods.get(termTemplate.getName());
			if (method == null) {
				throw new RuntimeException("No implementation found for template: "+termTemplate.getName());
			}
		}
	}
	
	public final List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		List<TermGenerationOutput> result = new ArrayList<TermGenerationOutput>();
		Map<String, OntologyTerm> pending = new HashMap<String, OntologyTerm>();
		for (TermGenerationInput input : generationTasks) {
			List<TermGenerationOutput> output = generate(input, pending);
			if (output != null && !output.isEmpty()) {
				result.addAll(output);
			}
		}
		return result;
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ToMatch {
	}

	@SuppressWarnings("unchecked")
	private List<TermGenerationOutput> generate(TermGenerationInput input, Map<String, OntologyTerm> pending) {
		TermTemplate template = input.getTermTemplate();
		Method method = methods.get(template.getName());
		if (method != null) {
			try {
				List<TermGenerationOutput> output = (List<TermGenerationOutput>) method.invoke(this, input, pending);
				return output;
			} catch (IllegalArgumentException exception) {
				throw new RuntimeException(exception);
			} catch (IllegalAccessException exception) {
				throw new RuntimeException(exception);
			} catch (InvocationTargetException exception) {
				throw new RuntimeException(exception);
			}
		}
		return null;
	}
	
	protected OWLObject getSingleTerm(TermGenerationParameters parameters, TemplateField targetField, OWLGraphWrapper...ontologies) {
		String id = parameters.getTerms().getValue(targetField, 0).getId();
		for (OWLGraphWrapper ontology : ontologies) {
			if (ontology != null) {
				OWLObject x = getTermSimple(id, ontology);
				if (x != null) {
					return x;
				}
			}
		}
		return null ;
	}
	
	protected OWLObject getSingleTerm(TermGenerationInput input, String name, OWLGraphWrapper...ontologies) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		return getSingleTerm(parameters, targetField, ontologies);
	}
	
	protected List<OWLObject> getListTerm(TermGenerationInput input, String name, OWLGraphWrapper ontology) {
		TemplateField targetField = input.getTermTemplate().getField(name);
		TermGenerationParameters parameters = input.getParameters();
		
		int count = parameters.getTerms().getCount(targetField);
		if (count == 0) {
			return Collections.emptyList();
		}
		
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (int i = 0; i < count; i++) {
			String id = parameters.getTerms().getValue(targetField, i).getId();
			OWLObject x = getTermSimple(id, ontology);
			if (x != null) {
				result.add(x);
			}
		}
		return result;
	}
	
}