package org.bbop.termgenie.services.visualization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonChange;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.OntologyConfiguration;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.impl.ConfiguredOntology;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import owltools.gfx.OWLGraphLayoutRenderer;
import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Implementation for rendering term hierarchies using QuickGO.
 */
@Singleton
public class TermHierarchyRendererImpl implements TermHierarchyRenderer {

	private final Map<String, ConfiguredOntology> configurations;
	private final OntologyLoader loader;
	private OntologyTaskManager commitTargetOntology = null;

	/**
	 * Constructor for test purposes only.
	 */
	TermHierarchyRendererImpl() {
		super();
		configurations = Collections.emptyMap();
		this.commitTargetOntology = null;
		loader = null;
	}

	/**
	 * @param ontologyConfiguration
	 * @param loader
	 */
	@Inject
	public TermHierarchyRendererImpl(OntologyConfiguration ontologyConfiguration,
			OntologyLoader loader)
	{
		super();
		this.loader = loader;
		this.configurations = ontologyConfiguration.getOntologyConfigurations();
	}

	
	/**
	 * @param commitTargetOntology the commitTargetOntology to set
	 */
	@Inject(optional=true)
	public void setCommitTargetOntology(@Named("CommitTargetOntology") OntologyTaskManager commitTargetOntology) {
		this.commitTargetOntology = commitTargetOntology;
	}

	@Override
	public JsonResult renderHierarchy(final List<String> ids,
			final String ontology,
			final ServletContext servletContext)
	{
		ConfiguredOntology configuredOntology = configurations.get(ontology);
		if (configuredOntology == null) {
			return new JsonResult(false, "Unkown ontology: "+ontology);
		}
		final String realPath = servletContext.getRealPath("generated/index.html");
		final JsonResult jsonResult = new JsonResult();

		OntologyTaskManager taskManager = loader.getOntology(configuredOntology);
		OntologyTask task = new OntologyTask() {

			@Override
			protected void runCatching(OWLGraphWrapper graph) {
				try {
					File workDirectory = new File(new File(realPath).getParentFile(), "data");
					File resultFile = renderHierarchy(ids, graph, workDirectory, true);
					jsonResult.setSuccess(true);
					jsonResult.setMessage("generated/data/" + resultFile.getName());
				} catch (IOException exception) {
					jsonResult.setSuccess(false);
					jsonResult.setMessage(exception.getMessage());
				}
			}

		};
		taskManager.runManagedTask(task);
		return jsonResult;
	}
	
	@Override
	public JsonResult visualizeDiffTerms(JsonDiff[] jsonDiffs, ServletContext servletContext)
	{
		if (commitTargetOntology == null) {
			return new JsonResult(false, "Feature not enabled.");
		}
		
		final List<String> ids = new ArrayList<String>();
		final Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>(); 
		for(JsonDiff jsonTerm : jsonDiffs) {
			allAxioms.addAll(OwlStringTools.translateStringToAxioms(jsonTerm.getOwlAxioms()));
			ids.add(jsonTerm.getId());
			List<JsonChange> changed = jsonTerm.getRelations();
			if (changed != null && !changed.isEmpty()) {
				for (JsonChange change : changed) {
					allAxioms.addAll(OwlStringTools.translateStringToAxioms(change.getOwlAxioms()));
					ids.add(change.getId());
				}
			}
		}
		return renderImage(commitTargetOntology, servletContext, ids, allAxioms);
	}

	@Override
	public JsonResult visualizeGeneratedTerms(final List<JsonOntologyTerm> generatedTerms,
			String ontology,
			ServletContext servletContext)
	{
		ConfiguredOntology configuredOntology = configurations.get(ontology);
		if (configuredOntology == null) {
			return new JsonResult(false, "Unkown ontology: "+ontology);
		}
		OntologyTaskManager taskManager = loader.getOntology(configuredOntology);
		
		List<String> ids = new ArrayList<String>();
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>(); 
		for(JsonOntologyTerm jsonTerm : generatedTerms) {
			allAxioms.addAll(OwlStringTools.translateStringToAxioms(jsonTerm.getOwlAxioms()));
			ids.add(jsonTerm.getTempId());
			List<JsonChange> changed = jsonTerm.getChanged();
			if (changed != null && !changed.isEmpty()) {
				for (JsonChange change : changed) {
					allAxioms.addAll(OwlStringTools.translateStringToAxioms(change.getOwlAxioms()));
					ids.add(change.getId());
				}
			}
		}
		return renderImage(taskManager, servletContext, ids, allAxioms);
	}

	private JsonResult renderImage(OntologyTaskManager taskManager,
			ServletContext servletContext,
			final List<String> ids,
			final Set<OWLAxiom> allAxioms)
	{
		final String realPath = servletContext.getRealPath("generated/index.html");
		final JsonResult jsonResult = new JsonResult();
		taskManager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {
	
			@Override
			public Modified run(OWLGraphWrapper graph)
			{
				OWLOntology owlOntology = graph.getSourceOntology();
				
				final OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
				List<OWLOntologyChange> changed = manager.addAxioms(owlOntology, allAxioms);
				
				try {
					File workDirectory = new File(new File(realPath).getParentFile(), "data");
					File resultFile = renderHierarchy(ids, graph, workDirectory, true);
					jsonResult.setSuccess(true);
					jsonResult.setMessage("generated/data/" + resultFile.getName());
				} catch (IOException exception) {
					jsonResult.setSuccess(false);
					jsonResult.setMessage(exception.getMessage());
				} finally {
					for(OWLOntologyChange c : changed) {
						if (c instanceof AddAxiom) {
							manager.removeAxiom(owlOntology, c.getAxiom());
						}
						else if (c instanceof RemoveAxiom) {
							manager.addAxiom(owlOntology, c.getAxiom());
						}
					}
				}
				return Modified.no;
			}
			
		});
		
		return jsonResult;
	}

	String renderHierarchy(List<String> ids, OWLGraphWrapper graph) throws IOException {
		return renderHierarchy(ids, graph, new File(FileUtils.getTempDirectory(), "data"), false).getAbsolutePath();
	}

	protected File renderHierarchy(List<String> ids,
			OWLGraphWrapper graph,
			File folder,
			boolean useCleaner) throws IOException
	{
		Set<OWLObject> objs = new HashSet<OWLObject>();
		for (String id : ids) {
			OWLObject owlObject = graph.getOWLObjectByIdentifier(id);
			if (owlObject != null) {
				objs.add(owlObject);
			}
		}
		return renderHierarchy(objs, graph, folder, useCleaner);
	}
	
	protected File renderHierarchy(Set<OWLObject> ids,
			OWLGraphWrapper graph,
			File folder,
			boolean useCleaner) throws IOException
	{
		OutputStream fos = null;
		try {
			OWLGraphLayoutRenderer r = new OWLGraphLayoutRenderer(graph);
			r.addObjects(ids);

			TempFileTools tools = TempFileTools.getInstance(folder,
					TimeUnit.MINUTES,
					30L,
					useCleaner);
			File tempFile = tools.createTempFile("hierarchy", ".png");
			fos = new FileOutputStream(tempFile);
			r.renderImage("png", fos);
			return tempFile;
		}
		finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
