package org.bbop.termgenie.rules;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;

public class TermGenieScriptRunner implements TermGenerationEngine {
	
	private static final Logger logger = Logger.getLogger(TermGenieScriptRunner.class);

	private final JSEngineManager jsEngineManager;
	private final List<TermTemplate> templates;
	private final Map<TermTemplate, Ontology[]> templateOntologyManagers;
	private final MultiOntologyTaskManager multiOntologyTaskManager;

	// set to true, for debugging
	private boolean printScript = false;
	private final ReasonerFactory factory;

	@Inject
	TermGenieScriptRunner(List<TermTemplate> templates,
			MultiOntologyTaskManager multiOntologyTaskManager,
			ReasonerFactory factory)
	{
		super();
		this.factory = factory;
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
	public List<TermGenerationOutput> generateTerms(Ontology ontology,
			List<TermGenerationInput> generationTasks)
	{
		if (ontology == null || generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		List<TermGenerationOutput> generationOutputs = new ArrayList<TermGenerationOutput>();
		int count = 0;
		for (TermGenerationInput input : generationTasks) {
			TermTemplate termTemplate = input.getTermTemplate();
			Ontology targetOntology = termTemplate.getCorrespondingOntology();
			String templateOntologyName = targetOntology.getUniqueName();
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
				String templateId = getTemplateId(termTemplate, count);
				String script = termTemplate.getRules();
				GenerationTask task = new GenerationTask(ontologies, targetOntology, input, script, templateId, factory);
				multiOntologyTaskManager.runManagedTask(task, ontologies);
				if (task.result != null && !task.result.isEmpty()) {
					generationOutputs.addAll(task.result);
				}
			}
			count += 1;
		}
		if (!generationOutputs.isEmpty()) {
			return generationOutputs;
		}
		return null;
	}

	/**
	 * Create an id for a templat which is unique for this input during a single
	 * {@link #generateTerms(Ontology, List)} request.
	 * 
	 * @param template
	 * @param count
	 * @return templateId
	 */
	private String getTemplateId(TermTemplate template, int count) {
		StringBuilder sb = new StringBuilder();
		String name = template.getName();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			}
		}
		sb.append(count);
		return sb.toString();
	}

	private final class GenerationTask extends MultiOntologyTask {

		private final Ontology[] ontologies;
		private final Ontology targetOntology;
		private final String script;
		private final TermGenerationInput input;
		private final String templateId;
		private final ReasonerFactory factory;

		List<TermGenerationOutput> result = null;

		private GenerationTask(Ontology[] ontologies,
				Ontology targetOntology,
				TermGenerationInput input,
				String script,
				String templateId,
				ReasonerFactory factory)
		{
			this.ontologies = ontologies;
			this.targetOntology = targetOntology;
			this.input = input;
			this.script = script;
			this.templateId = templateId;
			this.factory = factory;
		}

		@Override
		public List<Modified> run(List<OWLGraphWrapper> requested) {

			List<Modified> modified = new ArrayList<Modified>(requested.size());
			for (int i = 0; i < requested.size(); i++) {
				modified.add(Modified.no);
			}
			Integer targetOntologyIndex = null;
			TermGenieScriptFunctionsImpl functionsImpl = null;
			try {
				OWLGraphWrapper targetOntology = null;
				ScriptEngine engine = jsEngineManager.getEngine();
				for (int i = 0; i < ontologies.length; i++) {
					String name = ontologies[i].getUniqueName();
					engine.put(name, requested.get(i));
					if (name.equals(this.targetOntology.getUniqueName())) {
						targetOntology = requested.get(i);
						targetOntologyIndex = Integer.valueOf(i);
					}
				}
				if (targetOntology == null || targetOntologyIndex == null) {
					result = createError("Could not find requested ontology: "+this.targetOntology.getUniqueName());
					return modified;
				}
				
				functionsImpl = new TermGenieScriptFunctionsImpl(input, targetOntology, templateId, factory);
				engine.put("termgenie", functionsImpl);
				if (printScript) {
					PrintWriter writer = new PrintWriter(System.out);
					printScript(writer, script);
					writer.flush();
				}
				engine.eval(script);
				Invocable invocableEngine = (Invocable) engine;
				invocableEngine.invokeFunction("run");
				result = functionsImpl.getResult();
			} catch (ScriptException exception) {
				result = createError("Error during script execution:\n" + exception.getMessage());
				logger.error("Error during script execution", exception);
			} catch (ClassCastException exception) {
				result = createError("Error, script did not return expected type:\n" + exception.getMessage());
			} catch (NoSuchMethodException exception) {
				result = createError("Error, script did not contain expected method run:\n" + exception.getMessage());
			} finally {
				// set the target ontology modified flag
				if (functionsImpl != null) {
					if (functionsImpl.hasChanges()) {
						if (targetOntologyIndex != null) {
							modified.set(targetOntologyIndex.intValue(), Modified.reset);
						}
					}
				}
			}
			return modified;
		}

		protected List<TermGenerationOutput> createError(String message) {
			TermGenerationOutput error = new TermGenerationOutput(null, input, false, message);
			return Collections.singletonList(error);
		}
	}

	/**
	 * Print the script with additional line numbers for easy debugging.
	 * 
	 * @param writer
	 * @param script
	 */
	private static void printScript(PrintWriter writer, String script) {
		writer.println();
		writer.println("Script:");
		int pos;
		int prev = 0;
		int count = 1;
		while ((pos = script.indexOf('\n', prev)) >= 0) {
			writer.print(count);
			if (count < 10) {
				writer.print(' ');
			}
			if (count < 100) {
				writer.print(' ');
			}
			writer.print(' ');
			writer.println(script.substring(prev, pos));

			prev = pos + 1;
			count += 1;
		}
		if (prev < script.length()) {
			writer.print(count);
			if (count < 10) {
				writer.print(' ');
			}
			if (count < 100) {
				writer.print(' ');
			}
			writer.print(' ');
			writer.println(script.substring(prev));
		}
		writer.println();
		writer.println();
	}

	@Override
	public List<TermTemplate> getAvailableTemplates() {
		return templates;
	}
}
