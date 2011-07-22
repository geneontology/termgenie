package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;

public class TermGenieScriptRunner implements TermGenerationEngine {

	private final JSEngineManager jsEngineManager;
	private final List<TermTemplate> templates;
	private final Map<TermTemplate, Ontology[]> templateOntologyManagers;
	private final MultiOntologyTaskManager multiOntologyTaskManager;
	
	@Inject
	TermGenieScriptRunner(List<TermTemplate> templates, MultiOntologyTaskManager multiOntologyTaskManager) {
		super();
		this.jsEngineManager = new JSEngineManager();
		this.multiOntologyTaskManager = multiOntologyTaskManager;
		this.templateOntologyManagers = new HashMap<TermTemplate, Ontology[]>();
		this.templates = templates;
		for (TermTemplate termTemplate : templates) {
			List<Ontology> requiredOntologies = new ArrayList<Ontology>();
			Ontology targetOntology = termTemplate.getCorrespondingOntology();
			requiredOntologies.add(targetOntology);
			List<Ontology> external = termTemplate.getExternal();
			if (external != null && !external.isEmpty()) {
				requiredOntologies.addAll(external);
			}
			Ontology[] array = requiredOntologies.toArray(new Ontology[requiredOntologies.size()]);
			templateOntologyManagers.put(termTemplate, array);
		}
	}

	@Override
	public List<TermGenerationOutput> generateTerms(Ontology ontology, List<TermGenerationInput> generationTasks) {
		if (ontology == null || generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		List<TermGenerationOutput> generationOutputs = new ArrayList<TermGenerationOutput>();
		for (TermGenerationInput input : generationTasks) {
			TermTemplate termTemplate = input.getTermTemplate();
			String templateOntologyName = termTemplate.getCorrespondingOntology().getUniqueName();
			String requestedOntologyName = ontology.getUniqueName();
			if (!templateOntologyName.equals(requestedOntologyName)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Requested ontology (");
				sb.append(requestedOntologyName);
				sb.append(") differs from expected ontology (");
				sb.append(templateOntologyName);
				sb.append(')');
				generationOutputs.add(new TermGenerationOutput(null, input, false, sb.toString()));
				continue;
			}
			final Ontology[] ontologies = templateOntologyManagers.get(termTemplate);
			if (ontologies != null && ontologies.length > 0) {
				GenerationTask task = new GenerationTask(ontologies, input, termTemplate.getRules());
				multiOntologyTaskManager.runManagedTask(task, ontologies);
				if (task.result != null && !task.result.isEmpty()) {
					generationOutputs.addAll(task.result);
				}
			}
		}
		if (!generationOutputs.isEmpty()) {
			return generationOutputs;
		}
		return null;
	}

	private final class GenerationTask extends MultiOntologyTask {
		private final Ontology[] ontologies;
		private final String script;
		private final TermGenerationInput input;
		
		List<TermGenerationOutput> result = null;

		private GenerationTask(Ontology[] ontologies, TermGenerationInput input, String script) {
			this.ontologies = ontologies;
			this.input = input;
			this.script = script;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public void run(List<OWLGraphWrapper> requested) {
			TermGenieScriptFunctionsImpl functionsImpl = new TermGenieScriptFunctionsImpl(input);
			try {
				ScriptEngine engine = jsEngineManager.getEngine();
				for (int i = 0; i < ontologies.length; i++) {
					engine.put(ontologies[i].getUniqueName(), requested.get(i));
				}
				engine.put("termgenie", functionsImpl);
				result = (List<TermGenerationOutput>) engine.eval(script);
			} catch (ScriptException exception) {
				result = functionsImpl.error("Error during script execution:\n"+exception.getMessage());
			} catch (ClassCastException exception) {
				result = functionsImpl.error("Error, script did not return expected type:\n"+exception.getMessage());
			}
		}
	}

	@Override
	public List<TermTemplate> getAvailableTemplates() {
		return templates;
	}
}
