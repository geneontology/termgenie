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
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.rules.api.ChangeTracker;
import org.bbop.termgenie.rules.api.TermGenieScriptFunctions;
import org.bbop.termgenie.rules.impl.TermGenieScriptFunctionsMDefImpl;
import org.bbop.termgenie.tools.ResourceLoader;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TermGenieScriptRunner extends ResourceLoader implements TermGenerationEngine {

	private static final Logger logger = Logger.getLogger(TermGenieScriptRunner.class);

	public static final String USE_IS_INFERRED_BOOLEAN_NAME = "TermGenieScriptRunnerUseInferred";
	public static final String ASSERT_INFERERNCES_BOOLEAN_NAME = "TermGenieScriptRunnerAssertInferences";
	public static final String FILTER_NON_ASCII_SYNONYMS = "TermGenieScriptRunnerFilterNonAsciiSynonyms";
	
	private final JSEngineManager jsEngineManager;
	private final List<TermTemplate> templates;
	final Map<TermTemplate, String> scripts;
	private final OntologyTaskManager ontologyTaskManager;
	private final String ontologyName;
	private final ReasonerFactory factory;
	private final boolean useIsInferred;
	private final boolean assertInferences;
	private final boolean filterNonAsciiSynonyms;

	@Inject
	TermGenieScriptRunner(List<TermTemplate> templates,
			OntologyLoader loader,
			ReasonerFactory factory,
			@Named(USE_IS_INFERRED_BOOLEAN_NAME) boolean useIsInferred,
			@Named(ASSERT_INFERERNCES_BOOLEAN_NAME) boolean assertInferences,
			@Named(FILTER_NON_ASCII_SYNONYMS) boolean filterNonAsciiSynonyms)
	{
		super(false);
		this.factory = factory;
		this.jsEngineManager = new JSEngineManager();
		this.ontologyTaskManager = loader.getOntologyManager();
		ontologyName = ontologyTaskManager.getOntology().getName();
		this.templates = templates;
		this.scripts = new HashMap<TermTemplate, String>();
		this.useIsInferred = useIsInferred;
		this.assertInferences = assertInferences;
		this.filterNonAsciiSynonyms = filterNonAsciiSynonyms;
		for (TermTemplate termTemplate : templates) {
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
	public List<TermGenerationOutput> generateTerms(List<TermGenerationInput> generationTasks,
			boolean requireLiteratureReference,
			ProcessState processState)
	{
		if (generationTasks == null || generationTasks.isEmpty()) {
			// do nothing
			return null;
		}
		List<TermGenerationOutput> generationOutputs = new ArrayList<TermGenerationOutput>();
		int count = 0;
		for (TermGenerationInput input : generationTasks) {
			TermTemplate termTemplate = input.getTermTemplate();
			
			
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
			GenerationTask task = new GenerationTask(input, script, methodName, templateId, factory, processState, requireLiteratureReference, useIsInferred, filterNonAsciiSynonyms);
			try {
				ontologyTaskManager.runManagedTask(task);
			} catch (InvalidManagedInstanceException exception) {
				logger.error("Could not create terms due to an invalid ontology", exception);
				generationOutputs.add(TermGenerationOutput.error(input,"Could not create terms due to an invalid ontology: "+exception.getMessage()));
			}
			if (task.result != null && !task.result.isEmpty()) {
				generationOutputs.addAll(task.result);
			}
			count += 1;
		}
		if (!generationOutputs.isEmpty()) {
			return generationOutputs;
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

	private final class GenerationTask extends OntologyTask {

		private final String script;
		private final String methodName;
		private final TermGenerationInput input;
		private final String templateId;
		private final ReasonerFactory factory;
		private final ProcessState state;
		private final boolean requireLiteratureReference;
		private final boolean useIsInferred;
		private final boolean filterNonAsciiSynonyms;
		
		List<TermGenerationOutput> result = null;

		private GenerationTask(TermGenerationInput input,
				String script,
				String methodName,
				String templateId,
				ReasonerFactory factory,
				ProcessState state,
				boolean requireLiteratureReference,
				boolean useIsInferred,
				boolean filterNonAsciiSynonyms)
		{
			this.input = input;
			this.script = script;
			this.methodName = methodName;
			this.templateId = templateId;
			this.factory = factory;
			this.state = state;
			this.requireLiteratureReference = requireLiteratureReference;
			this.useIsInferred = useIsInferred;
			this.filterNonAsciiSynonyms = filterNonAsciiSynonyms;
		}

		@Override
		public Modified runCatchingMod(OWLGraphWrapper graph) throws InvalidManagedInstanceException {

			OWLOntology sourceOntology = graph.getSourceOntology();
			try {
				sourceOntology.getImportsClosure();
			} catch (UnknownOWLOntologyException exception) {
				throw new InvalidManagedInstanceException("Can't create terms, inconsistent ontology state: "+sourceOntology.getOntologyID().getOntologyIRI(), exception);
			}
			Modified modified = Modified.no;
			ChangeTracker changeTracker = null;
			TermGenieScriptFunctionsMDefImpl functionsImpl = null;
			try {
				ScriptEngine engine = jsEngineManager.getEngine();
				engine.put(ontologyName, graph);
				

				functionsImpl = new TermGenieScriptFunctionsMDefImpl(input, graph, getTempIdPrefix(graph), templateId, factory, state, requireLiteratureReference, useIsInferred, assertInferences, filterNonAsciiSynonyms);
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
				// properly dispose the script runner
				if (functionsImpl != null) {
					functionsImpl.dispose();
				}
				// set the target ontology modified flag
				if (changeTracker != null) {
					if (changeTracker.hasChanges()) {
						modified = Modified.reset;
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
		return TemporaryIdentifierTools.getTempIdPrefix(ontology);
	}
}
