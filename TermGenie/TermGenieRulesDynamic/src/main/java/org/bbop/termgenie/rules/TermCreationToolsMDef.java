package org.bbop.termgenie.rules;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsMDef.MDef;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import owltools.graph.OWLGraphWrapper;

public class TermCreationToolsMDef extends AbstractTermCreationTools<List<MDef>> {

	private final ManchesterSyntaxTool syntaxTool;
	private final String targetOntologyId;
	
	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	TermCreationToolsMDef(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, tempIdPrefix, patternID, factory);
		this.targetOntologyId = targetOntology.getOntologyId();
		syntaxTool = new ManchesterSyntaxTool(targetOntology.getSourceOntology());
	}

	@Override
	protected List<IRelation> createRelations(List<MDef> logicalDefinitions,
			String newId,
			OWLChangeTracker changeTracker)  throws RelationCreationException
	{
		if (logicalDefinitions == null || logicalDefinitions.isEmpty()) {
			return Collections.emptyList();
		}
		OWLOntologyManager owlManager = targetOntology.getManager();
		OWLDataFactory owlDataFactory = owlManager.getOWLDataFactory();
		OWLClassExpression newOwlClass = createOWLClass(newId, targetOntology, owlDataFactory);
		for (MDef def : logicalDefinitions) {
			String expression = def.getExpression();
			Map<String, String> parameters = def.getParameters();
			for (Entry<String, String> parameter : parameters.entrySet()) {
				expression = expression.replaceAll("\\?"+parameter.getKey(), parameter.getValue());
			}
			try {
				OWLClassExpression owlClassExpression = syntaxTool.parseManchesterExpression(expression);
				OWLEquivalentClassesAxiom axiom = owlDataFactory.getOWLEquivalentClassesAxiom(newOwlClass, owlClassExpression);
				changeTracker.apply(new AddAxiom(targetOntology.getSourceOntology(), axiom));
				
			} catch (ParserException exception) {
				throw new RelationCreationException("Could not create OWL class expressions from expression: "+expression, exception);
			}
		}
		
		InferAllRelationshipsTask task = new InferAllRelationshipsTask(targetOntology, newOwlClass, changeTracker);

		factory.updateBuffered(targetOntologyId);
		ReasonerTaskManager reasonerManager = factory.getDefaultTaskManager(targetOntology);
		reasonerManager.runManagedTask(task);
		return task.getRelations();
	}

	private OWLClassExpression createOWLClass(String newId,
			OWLGraphWrapper ontology,
			OWLDataFactory owlDataFactory)
	{
		// TODO is this good enough for a temp id?
		IRI iri = IRI.create(Obo2OWLConstants.DEFAULT_IRI_PREFIX + ontology.getOntologyId()+"/"+newId);
		return owlDataFactory.getOWLClass(iri);
	}

	private class ManchesterSyntaxTool {

		private final OWLDataFactory dataFactory;
		private OWLEntityChecker entityChecker;

		ManchesterSyntaxTool(OWLOntology inputOntology) {
			OWLOntologyManager manager = inputOntology.getOWLOntologyManager();
			this.dataFactory = manager.getOWLDataFactory();
			entityChecker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, Collections.singleton(inputOntology), new SimpleShortFormProvider()));
		}

		OWLClassExpression parseManchesterExpression(String expression) throws ParserException {

			ManchesterOWLSyntaxEditorParser parser = createParser(expression);
			OWLClassExpression ce = parser.parseClassExpression();
			return ce;
		}

		private ManchesterOWLSyntaxEditorParser createParser(String expression) {
			ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, expression);
			parser.setOWLEntityChecker(entityChecker);
			return parser;
		}
	}
}
