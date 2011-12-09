package org.bbop.termgenie.rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager;
import org.bbop.termgenie.ontology.MultiOntologyTaskManager.MultiOntologyTask;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.tools.ResourceLoader;
import org.obolibrary.obo2owl.Obo2OWLConstants;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;

public class TermGenieScriptRunner extends ResourceLoader implements TermGenerationEngine {

	private static final Logger logger = Logger.getLogger(TermGenieScriptRunner.class);

	private final JSEngineManager jsEngineManager;
	private final List<TermTemplate> templates;
	final Map<TermTemplate, String> scripts;
	private final Map<TermTemplate, Ontology[]> templateOntologyManagers;
	private final MultiOntologyTaskManager multiOntologyTaskManager;

	private final ReasonerFactory factory;

	private final OntologyConfiguration ontologyConfiguration;

	@Inject
	TermGenieScriptRunner(List<TermTemplate> templates,
			MultiOntologyTaskManager multiOntologyTaskManager,
			ReasonerFactory factory,
			OntologyConfiguration ontologyConfiguration)
	{
		super(false);
		this.factory = factory;
		this.ontologyConfiguration = ontologyConfiguration;
		this.jsEngineManager = new JSEngineManager();
		this.multiOntologyTaskManager = multiOntologyTaskManager;
		this.templateOntologyManagers = new HashMap<TermTemplate, Ontology[]>();
		this.templates = templates;
		this.scripts = new HashMap<TermTemplate, String>();
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
			scripts.put(termTemplate, loadScript(termTemplate));
		}
	}

	private String loadScript(TermTemplate template) {
		StringBuilder sb = new StringBuilder();
		Set<String> loadedFiles = new HashSet<String>();
		ConcurrentLinkedQueue<String> fileQueue = new ConcurrentLinkedQueue<String>(template.getRuleFiles());
		while (fileQueue.peek() != null) {
			String ruleFile = fileQueue.poll();
			sb.append("\n// ruleFile: ");
			sb.append(ruleFile);
			sb.append('\n');
			InputStream stream = null;
			try {
				boolean add = loadedFiles.add(ruleFile);
				if (!add) {
					continue;
				}
				stream = loadResourceSimple(ruleFile);
				if (stream == null) {
					String msg = "Could not find ruleFile: '"+ruleFile+"'";
					logger.error(msg);
					throw new RuntimeException(msg);
				}
				
				LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");
				while (iterator.hasNext()) {
					String line = iterator.next();
					String requiredFile = getRequiredImport(line);
					if (requiredFile != null) {
						fileQueue.add(requiredFile);
					}
					sb.append(line);
					sb.append('\n');
				}
			} catch (IOException exception) {
				String msg = "Could not load ruleFile: '"+ruleFile+"'";
				logger.error(msg, exception);
				throw new RuntimeException(msg, exception);
			}
			finally {
				IOUtils.closeQuietly(stream);
			}
		}
		if (loadedFiles.isEmpty()) {
			String msg = "No rule files loaded for template: "+template.getName();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		return sb.toString();
	}
	
	private static final String JS_REQUIRES_PREFIX = "// @requires ";
	private static final int  JS_REQUIRES_PREFIX_LENGTH = JS_REQUIRES_PREFIX.length();

	private String getRequiredImport(String line) {
		if (line.length() > JS_REQUIRES_PREFIX_LENGTH && line.startsWith(JS_REQUIRES_PREFIX)) {
			String requiredFile = line.substring(JS_REQUIRES_PREFIX_LENGTH).trim();
			if (requiredFile.length() > 1) {
				return requiredFile;
			}
		}
		return null;
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
				generationOutputs.add(TermGenerationOutput.error(input, sb.toString()));
				continue;
			}
			
			List<String> missing = checkRequired(termTemplate, targetOntology);
			if (missing != null && !missing.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Configuration error: For template '");
				sb.append(termTemplate.getName());
				sb.append("' the following required ontology ");
				if (missing.size() == 1) {
					sb.append("file is not configured: ");
					sb.append(missing.get(0));
				}
				else {
					sb.append("files are not configured: ");
					for (int i = 0; i < missing.size(); i++) {
						if (i > 0) {
							sb.append(", ");
						}
						sb.append(missing.get(i));
					}
				}
				generationOutputs.add(TermGenerationOutput.error(input, sb.toString()));
				continue;
			}
			
			final Ontology[] ontologies = templateOntologyManagers.get(termTemplate);
			if (ontologies != null && ontologies.length > 0) {
				String templateId = getTemplateId(termTemplate, count);
				String script = scripts.get(termTemplate);
				if (script == null) {
					String msg = "No valid script found for template: "+termTemplate.getName();
					logger.error(msg);
					generationOutputs.add(TermGenerationOutput.error(input, msg));
					continue;
				}
				String methodName = termTemplate.getMethodName();
				if (methodName == null) {
					methodName = termTemplate.getName();
				}
				GenerationTask task = new GenerationTask(ontologies, targetOntology, input, script, methodName, templateId, factory);
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
	
	private List<String> checkRequired(TermTemplate termTemplate, Ontology targetOntology) {
		List<String> requires = termTemplate.getRequires();
		if (requires != null && !requires.isEmpty()) {
			ConfiguredOntology configuredOntology = ontologyConfiguration.getOntologyConfigurations().get(targetOntology.getUniqueName());
			List<String> supports = configuredOntology.getSupports();
			List<String> missing = new ArrayList<String>();
			for(String require : requires) {
				if (!supports.contains(require)) {
					missing.add(require);
				}
			}
			return missing;
		}
		return null;
	}

	/**
	 * Create an id for a template which is unique for this input during a
	 * single {@link #generateTerms(Ontology, List)} request.
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
		private final String methodName;
		private final TermGenerationInput input;
		private final String templateId;
		private final ReasonerFactory factory;

		List<TermGenerationOutput> result = null;

		private GenerationTask(Ontology[] ontologies,
				Ontology targetOntology,
				TermGenerationInput input,
				String script,
				String methodName,
				String templateId,
				ReasonerFactory factory)
		{
			this.ontologies = ontologies;
			this.targetOntology = targetOntology;
			this.input = input;
			this.script = script;
			this.methodName = methodName;
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
			ChangeTracker changeTracker = null;
			try {
				List<OWLGraphWrapper> auxiliaryOntologies = new ArrayList<OWLGraphWrapper>(ontologies.length);
				OWLGraphWrapper targetOntology = null;
				ScriptEngine engine = jsEngineManager.getEngine();
				for (int i = 0; i < ontologies.length; i++) {
					String name = ontologies[i].getUniqueName();
					OWLGraphWrapper ontology = requested.get(i);
					engine.put(name, ontology);
					if (name.equals(this.targetOntology.getUniqueName())) {
						targetOntology = ontology;
						targetOntologyIndex = Integer.valueOf(i);
					}
					else{
						auxiliaryOntologies.add(ontology);
					}
				}
				if (targetOntology == null || targetOntologyIndex == null) {
					result = createError("Could not find requested ontology: " + this.targetOntology.getUniqueName());
					return modified;
				}

				TermGenieScriptFunctionsMDefImpl functionsImpl = new TermGenieScriptFunctionsMDefImpl(input, targetOntology, auxiliaryOntologies, getTempIdPrefix(targetOntology), templateId, factory);
				changeTracker = functionsImpl;
				run(engine, functionsImpl);
				result = functionsImpl.getResult();
			} catch (ScriptException exception) {
				result = createError("Error during script execution:\n" + exception.getMessage());
				printScript(script);
				logger.error("Error during script execution", exception);
			} catch (ClassCastException exception) {
				printScript(script);
				result = createError("Error, script did not return expected type:\n" + exception.getMessage());
			} catch (NoSuchMethodException exception) {
				printScript(script);
				result = createError("Error, script did not contain expected method run:\n" + exception.getMessage());
			}
			finally {
				// set the target ontology modified flag
				if (changeTracker != null) {
					if (changeTracker.hasChanges()) {
						if (targetOntologyIndex != null) {
							modified.set(targetOntologyIndex.intValue(), Modified.reset);
						}
					}
				}
			}
			return modified;
		}

		private void run(ScriptEngine engine, TermGenieScriptFunctions functionsImpl)
				throws ScriptException, NoSuchMethodException
		{
			engine.put("termgenie", functionsImpl);
			engine.eval(script);
			Invocable invocableEngine = (Invocable) engine;
			invocableEngine.invokeFunction(methodName);
		}

		protected List<TermGenerationOutput> createError(String message) {
			return Collections.singletonList(TermGenerationOutput.error(input, message));
		}
	}

	/**
	 * Print the script with additional line numbers for easy debugging.
	 * 
	 * @param writer
	 * @param script
	 */
	private static void printScript(String script) {
		PrintWriter writer = new PrintWriter(System.out);
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
		writer.flush();
	}

	@Override
	public List<TermTemplate> getAvailableTemplates() {
		return templates;
	}

	@Override
	public String getTempIdPrefix(OWLGraphWrapper ontology) {
		return Obo2OWLConstants.DEFAULT_IRI_PREFIX + ontology.getOntologyId().toUpperCase()+"_"+"TEMP-";
	}
}
