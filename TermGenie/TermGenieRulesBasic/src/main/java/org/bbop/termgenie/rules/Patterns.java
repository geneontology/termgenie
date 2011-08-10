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

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;

class Patterns extends BasicRules {

	private final Map<String, Method> methods;
	
	protected Patterns(ReasonerFactory factory, TermTemplate...templates) {
		super(factory);
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
		// intentionally empty
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
	
	protected OWLObject getSingleTerm(TermGenerationInput input, String name, OWLGraphWrapper...ontologies) {
		String id = getFieldSingleTerm(input, name).getId(); 
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
	
	protected List<OWLObject> getListTerm(TermGenerationInput input, String name, OWLGraphWrapper ontology) {
		List<OntologyTerm> terms = getFieldTermList(input, name);
		if (terms == null || terms.isEmpty()) {
			return Collections.emptyList();
		}
		List<OWLObject> result = new ArrayList<OWLObject>();
		for (OntologyTerm term : terms) {
			if (term != null) {
				OWLObject x = getTermSimple(term.getId(), ontology);
				if (x != null) {
					result.add(x);
				}
			}
		}
		return result;
	}
	
}