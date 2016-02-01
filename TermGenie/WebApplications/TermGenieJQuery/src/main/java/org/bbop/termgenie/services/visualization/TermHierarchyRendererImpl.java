package org.bbop.termgenie.services.visualization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.data.JsonOntologyTerm;
import org.bbop.termgenie.data.JsonOntologyTerm.JsonChange;
import org.bbop.termgenie.data.JsonResult;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.ontology.OntologyTaskManager;
import org.bbop.termgenie.ontology.OntologyTaskManager.OntologyTask;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

import owltools.gfx.OWLGraphLayoutRenderer;
import owltools.graph.OWLGraphWrapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation for rendering term hierarchies using QuickGO.
 */
@Singleton
public class TermHierarchyRendererImpl implements TermHierarchyRenderer {

	private static final Logger logger = Logger.getLogger(TermHierarchyRendererImpl.class);
	
	private OntologyTaskManager manager = null;

	/**
	 * Constructor for test purposes only.
	 */
	TermHierarchyRendererImpl() {
		super();
		this.manager = null;
	}

	/**
	 * @param loader
	 */
	@Inject
	public TermHierarchyRendererImpl(OntologyLoader loader)
	{
		super();
		this.manager = loader.getOntologyManager();
	}

	
	@Override
	public JsonResult renderHierarchy(final List<String> ids,
			final ServletContext servletContext)
	{
		final String realPath = servletContext.getRealPath("generated/index.html");
		final JsonResult jsonResult = new JsonResult();

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
		try {
			manager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			return error("Could not create hierarchy to due an invalid ontology", exception);
		}
		return jsonResult;
	}
	
	private JsonResult error(String message, Exception exception) {
		logger.error(message, exception);
		JsonResult json = new JsonResult(false, message+": "+exception.getMessage());
		return json;
	}

	@Override
	public JsonResult visualizeDiffTerms(JsonDiff[] jsonDiffs, ServletContext servletContext)
	{
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
		try {
			return renderImage(manager, servletContext, ids, allAxioms);
		} catch (InvalidManagedInstanceException exception) {
			return error("Could not create hierarchy to due an invalid ontology", exception);
		}
	}

	@Override
	public JsonResult visualizeGeneratedTerms(final List<JsonOntologyTerm> generatedTerms,
			ServletContext servletContext)
	{
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
		try {
			return renderImage(manager, servletContext, ids, allAxioms);
		} catch (InvalidManagedInstanceException exception) {
			return error("Could not create hierarchy to due an invalid ontology", exception);
		}
	}

	private JsonResult renderImage(OntologyTaskManager taskManager,
			ServletContext servletContext,
			final List<String> ids,
			final Set<OWLAxiom> allAxioms) throws InvalidManagedInstanceException
	{
		final String realPath = servletContext.getRealPath("generated/index.html");
		final JsonResult jsonResult = new JsonResult();
		taskManager.runManagedTask(new ManagedTask<OWLGraphWrapper>() {
	
			@Override
			public Modified run(OWLGraphWrapper graph)
			{
				OWLOntology owlOntology = graph.getSourceOntology();
				
				final OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
				final ChangeApplied applied = manager.addAxioms(owlOntology, allAxioms);
				
				try {
					File workDirectory = new File(new File(realPath).getParentFile(), "data");
					File resultFile = renderHierarchy(ids, graph, workDirectory, true);
					jsonResult.setSuccess(true);
					jsonResult.setMessage("generated/data/" + resultFile.getName());
				} catch (IOException exception) {
					jsonResult.setSuccess(false);
					jsonResult.setMessage(exception.getMessage());
				} finally {
					if (applied == ChangeApplied.SUCCESSFULLY) {
						manager.removeAxioms(owlOntology, allAxioms);
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
			else {
				logger.warn("Skipping id: '"+id+"' for hierarchy image generation.");
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
