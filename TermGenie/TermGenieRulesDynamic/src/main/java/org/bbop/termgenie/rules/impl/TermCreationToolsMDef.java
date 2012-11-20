package org.bbop.termgenie.rules.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import org.bbop.termgenie.core.management.GenericTaskManager.InvalidManagedInstanceException;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.owl.InferAllRelationshipsTask;
import org.bbop.termgenie.owl.InferredRelations;
import org.bbop.termgenie.owl.OWLChangeTracker;
import org.bbop.termgenie.rules.api.ChangeTracker;
import org.bbop.termgenie.rules.api.TermGenieScriptFunctionsMDef.MDef;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.macro.ManchesterSyntaxTool;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

public class TermCreationToolsMDef implements ChangeTracker {

	private final ManchesterSyntaxTool syntaxTool;
	private final String targetOntologyId;
	private final String tempIdPrefix;
	private final boolean useIsInferred;
	final ReasonerFactory factory;
	final TermGenerationInput input;
	final OWLGraphWrapper targetOntology;
	final ProcessState state;
	private final String patternID;
	private final OWLChangeTracker changeTracker;
	private int count = 0;

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 * @param syntaxTool
	 * @param state
	 * @param useIsInferred
	 */
	TermCreationToolsMDef(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory,
			ManchesterSyntaxTool syntaxTool,
			ProcessState state,
			boolean useIsInferred)
	{
		super();
		this.input = input;
		this.targetOntology = targetOntology;
		this.state = state;
		this.patternID = tempIdPrefix + patternID;
		this.factory = factory;
		changeTracker = new OWLChangeTracker(targetOntology.getSourceOntology());
		this.tempIdPrefix = tempIdPrefix;
		this.targetOntologyId = targetOntology.getOntologyId();
		this.syntaxTool = syntaxTool;
		this.useIsInferred = useIsInferred;
	}

	
	protected static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");

	protected static class RelationCreationException extends Exception {

		// generated
		private static final long serialVersionUID = -1460767598044524094L;

		/**
		 * @param message
		 * @param cause
		 */
		public RelationCreationException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * @param message
		 */
		public RelationCreationException(String message) {
			super(message);
		}
	}
	
	protected final static ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};
	
	protected String renderLogicalDefinition(List<MDef> logicalDefinitions) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < logicalDefinitions.size(); i++) {
			MDef mDef = logicalDefinitions.get(i);
			String expression = getFullExpression(mDef);
			if (i > 0) {
				sb.append(", ");
			}
			sb.append('"');
			sb.append(expression);
			sb.append('"');
		}
		return sb.toString();
	}

	private String getFullExpression(MDef mDef) {
		String expression = mDef.getExpression();
		Map<String, String> parameters = mDef.getParameters();
		for (Entry<String, String> parameter : parameters.entrySet()) {
			expression = expression.replaceAll("\\?" + parameter.getKey(), parameter.getValue());
		}
		return expression;
	}

	protected InferredRelations createRelations(List<MDef> logicalDefinitions,
			String newId,
			String label,
			OWLChangeTracker changeTracker) throws RelationCreationException
	{
		if (logicalDefinitions == null || logicalDefinitions.isEmpty()) {
			return InferredRelations.EMPTY;
		}
		ProcessState.addMessage(state, "Start inferring relationships from logical definition.");
		OWLOntologyManager owlManager = targetOntology.getManager();
		OWLDataFactory owlDataFactory = owlManager.getOWLDataFactory();
		IRI iri = IRI.create(newId);
		Pair<OWLClass,OWLAxiom> pair = addClass(iri, changeTracker);
		addLabel(iri, label, changeTracker);
		for (MDef def : logicalDefinitions) {
			String expression = getFullExpression(def);
			try {
				OWLClassExpression owlClassExpression = syntaxTool.parseManchesterExpression(expression);
				OWLEquivalentClassesAxiom axiom = owlDataFactory.getOWLEquivalentClassesAxiom(pair.getOne(),
						owlClassExpression);
				changeTracker.apply(new AddAxiom(targetOntology.getSourceOntology(), axiom));

			} catch (ParserException exception) {
				throw new RelationCreationException("Could not create OWL class expressions from expression: " + expression, exception);
			}
		}

		InferAllRelationshipsTask task = new InferAllRelationshipsTask(targetOntology, iri, changeTracker, tempIdPrefix, state, useIsInferred);

		factory.updateBuffered(targetOntologyId);
		ReasonerTaskManager reasonerManager = factory.getDefaultTaskManager(targetOntology);
		reasonerManager.setProcessState(state);
		try {
			reasonerManager.runManagedTask(task);
		} catch (InvalidManagedInstanceException exception) {
			throw new RelationCreationException("Could not create releations due to an invalid reasoner.", exception);
		} finally {
			reasonerManager.dispose();
			reasonerManager.removeProcessState();
		}
		InferredRelations inferredRelations = task.getInferredRelations();
		Set<OWLAxiom> classRelationAxioms = inferredRelations.getClassRelationAxioms();
		if (classRelationAxioms != null) {
			// defensive copy
			classRelationAxioms = new HashSet<OWLAxiom>(classRelationAxioms);
		}
		else {
			classRelationAxioms = new HashSet<OWLAxiom>();
		}
		classRelationAxioms.add(pair.getTwo());
		inferredRelations.setClassRelationAxioms(classRelationAxioms);
		ProcessState.addMessage(state, "Finished inferring relationships from logical definition.");
		return inferredRelations;
	}

	private String getNewId() {
		String id = patternID + count;
		count += 1;
		return id;
	}

	protected List<String> getInputs(String name) {
		return getFieldStrings(name);
	}

	protected String getInput(String name) {
		List<String> strings = getFieldStrings(name);
		if (strings == null || strings.isEmpty()) {
			return null;
		}
		return strings.get(0);
	}

	protected List<String> getFieldStrings(String name) {
		Map<String, List<String>> strings = input.getParameters().getStrings();
		if (strings != null) {
			return strings.get(name);
		}
		return null;
	}

	protected boolean addTerm(String label, String definition, List<ISynonym> synonyms, List<MDef> logicalDefinition, List<TermGenerationOutput> output) {
		ProcessState.addMessage(state, "Start creating term.");
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Frame term = new Frame(FrameType.TERM);
	
		// get overwrites from GUI
		String inputName = getInput("Name");
		if (inputName != null) {
			inputName = inputName.trim();
			if (inputName.length() > 1) {
				label = inputName;
			}
		}
	
		// Fact Checking
		// check label
		OWLObject sameName = targetOntology.getOWLObjectByLabel(label);
		if (sameName != null) {
			output.add(singleError("The term " + targetOntology.getIdentifier(sameName) + " with the same label '"+label+"' already exists",
					input));
			return false;
		}
		OboTools.addTermLabel(term, label);
		
		String inputDefinition = getInput("Definition");
		if (inputDefinition != null) {
			inputDefinition = inputDefinition.trim();
			if (inputDefinition.length() > 1) {
				definition = inputDefinition;
			}
		}
	
		// def xref
		List<String> defxrefs = getDefXref();
		if (defxrefs != null) {
			// check xref conformity
			boolean hasXRef = false;
			for (String defxref : defxrefs) {
				// check if the termgenie def_xref is already in the list
				hasXRef = hasXRef || defxref.equals("GOC:TermGenie");
	
				// simple defxref check, TODO use a centralized qc check.
				if (defxref.length() < 3) {
					output.add(singleError("The Def_Xref " + defxref + " is too short. A Def_Xref consists of a prefix and suffix with a colon (:) as separator",
							input));
					continue;
				}
				if (!def_xref_Pattern.matcher(defxref).matches()) {
					output.add(singleError("The Def_Xref " + defxref + " does not conform to the expected pattern. A Def_Xref consists of a prefix and suffix with a colon (:) as separator and contains no whitespaces.",
							input));
				}
			}
			if (!hasXRef) {
				// add the termgenie def_xref
				ArrayList<String> newlist = new ArrayList<String>(defxrefs.size() + 1);
				newlist.addAll(defxrefs);
				newlist.add("GOC:TermGenie");
				defxrefs = newlist;
			}
		}
		else {
			defxrefs = Collections.singletonList("GOC:TermGenie");
		}
		OboTools.addDefinition(term, definition, defxrefs);
	
		term.addClause(new Clause(OboFormatTag.TAG_CREATION_DATE, getDate()));
		term.addClause(new Clause(OboFormatTag.TAG_CREATED_BY, "TermGenie"));
		String comment = getInput("Comment");
		if (comment != null && comment.length() > 0) {
			term.addClause(new Clause(OboFormatTag.TAG_COMMENT, comment));
		}
		String oboNamespace = this.input.getTermTemplate().getOboNamespace();
		if (oboNamespace != null) {
			term.addClause(new Clause(OboFormatTag.TAG_NAMESPACE, oboNamespace));
		}
		
		if (synonyms != null) {
			for(ISynonym synonym : synonyms) {
				OboTools.addSynonym(term, synonym.getLabel(), synonym.getScope(), synonym.getXrefs());
			}
		}
		
		String owlNewId = getNewId();
		String oboNewId = Owl2Obo.getIdentifier(IRI.create(owlNewId));
		OboTools.addTermId(term, oboNewId);
	
		try {
			InferredRelations inferredRelations = createRelations(logicalDefinition, owlNewId, label, changeTracker);
			if (inferredRelations.getEquivalentClasses() != null) {
				for (OWLClass owlClass : inferredRelations.getEquivalentClasses()) {
					output.add(singleError("Failed to create the term "+label+
							" with the logical definition: "+ renderLogicalDefinition(logicalDefinition) +
							" The term " + targetOntology.getIdentifier(owlClass) +" '"+ targetOntology.getLabel(owlClass) +
							"' with the same logic definition already exists",
							input));					
				}
				return false;
			}
			List<Clause> relations = inferredRelations.getClassRelations();
			if (relations != null) {
				term.getClauses().addAll(relations);
			}
			if (inferredRelations.getClassRelationAxioms() != null) {
				axioms.addAll(inferredRelations.getClassRelationAxioms());
			}
			axioms.add(OwlTranslatorTools.createLabelAxiom(owlNewId, label, targetOntology));
			// TODO add all other term details to the axioms?
			output.add(success(term, axioms , inferredRelations.getChanged(), input));
			
			ProcessState.addMessage(state, "Finished creating term.");
			return true;
		} catch (RelationCreationException exception) {
			output.add(singleError(exception.getMessage(), input));
			return false;
		}
	}

	private List<String> getDefXref() {
		return getInputs("DefX_Ref");
	}

	private String getDate() {
		return df.get().format(new Date());
	}

	protected TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return TermGenerationOutput.error(input, message);
	}

	protected TermGenerationOutput success(Frame term, Set<OWLAxiom> owlAxioms, List<Pair<Frame, Set<OWLAxiom>>> changedTermRelations, TermGenerationInput input) {
		return new TermGenerationOutput(term, owlAxioms, changedTermRelations, input, true, null);
	}

	@Override
	public boolean hasChanges() {
		return changeTracker.undoChanges() == false;
	}

	static Pair<OWLClass, OWLAxiom> addClass(IRI iri, OWLChangeTracker changeTracker) {
		OWLDataFactory factory = changeTracker.getTarget().getOWLOntologyManager().getOWLDataFactory();
		OWLClass owlClass = factory.getOWLClass(iri);
		OWLDeclarationAxiom owlDeclarationAxiom = factory.getOWLDeclarationAxiom(owlClass);
		changeTracker.apply(new AddAxiom(changeTracker.getTarget(), owlDeclarationAxiom));
		return new Pair<OWLClass, OWLAxiom>(owlClass, owlDeclarationAxiom);
	}
	
	static void addLabel(IRI iri,
			String label,
			OWLChangeTracker changeTracker)
	{
		OWLDataFactory owlDataFactory = changeTracker.getTarget().getOWLOntologyManager().getOWLDataFactory();
		OWLAnnotationAssertionAxiom axiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlDataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()),
				iri,
				owlDataFactory.getOWLLiteral(label));
		changeTracker.apply(new AddAxiom(changeTracker.getTarget(), axiom));
	}
}