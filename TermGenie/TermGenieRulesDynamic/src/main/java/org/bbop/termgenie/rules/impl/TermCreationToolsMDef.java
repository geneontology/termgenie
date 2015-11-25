package org.bbop.termgenie.rules.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.SharedReasoner;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.owl.AddPartOfRelationshipsTask;
import org.bbop.termgenie.owl.InferAllRelationshipsTask;
import org.bbop.termgenie.owl.InferredRelations;
import org.bbop.termgenie.owl.OWLChangeTracker;
import org.bbop.termgenie.owl.RelationshipTask;
import org.bbop.termgenie.owl.SimpleRelationHandlingTask;
import org.bbop.termgenie.rules.api.ChangeTracker;
import org.bbop.termgenie.rules.api.TermGenieScriptFunctionsMDef.MDef;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.xrefs.XrefTools;
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
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

public class TermCreationToolsMDef implements ChangeTracker {

	private final ManchesterSyntaxTool syntaxTool;
	private final String tempIdPrefix;
	private final boolean assertInferences;
	private final boolean useIsInferred;
	private final boolean requireLiteratureReference;
	final ReasonerFactory factory;
	final TermGenerationInput input;
	final OWLGraphWrapper targetOntology;
	final ProcessState state;
	private final String patternID;
	private final OWLChangeTracker changeTracker;
	private final String defaultXref;
	private int count = 0;
	
	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 * @param syntaxTool
	 * @param state
	 * @param requireLiteratureReference
	 * @param useIsInferred
	 * @param assertInferences
	 * @param defaultXref
	 */
	TermCreationToolsMDef(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory,
			ManchesterSyntaxTool syntaxTool,
			ProcessState state,
			boolean requireLiteratureReference,
			boolean useIsInferred,
			boolean assertInferences,
			String defaultXref)
	{
		super();
		this.input = input;
		this.targetOntology = targetOntology;
		this.state = state;
		this.requireLiteratureReference = requireLiteratureReference;
		this.patternID = tempIdPrefix + patternID;
		this.factory = factory;
		changeTracker = new OWLChangeTracker(targetOntology.getSourceOntology());
		this.tempIdPrefix = tempIdPrefix;
		this.syntaxTool = syntaxTool;
		this.assertInferences = assertInferences;
		this.useIsInferred = useIsInferred;
		this.defaultXref = defaultXref;
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
			List<MDef> partOf,
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
		Set<OWLClassExpression> partOfExpressions = new HashSet<OWLClassExpression>();
		if (partOf != null && !partOf.isEmpty()) {
			for(MDef mdef : partOf) {
				String expression = getFullExpression(mdef);
				try {
					partOfExpressions.add(syntaxTool.parseManchesterExpression(expression));
				} catch (ParserException exception) {
					throw new RelationCreationException("Could not create OWL class expressions from expression: " + expression, exception);
				}
			}
		}
		OWLReasoner reasoner = factory.createReasoner(targetOntology, state);
		try {
			ProcessState.addMessage(state, "Check for ontology consistency.");
			if (reasoner.isConsistent() == false) {
				throw new RelationCreationException("The ontology is inconsistent. No safe inferences are possible.");
			}
			ProcessState.addMessage(state, "Check for unsatisfiable classes");
			final Set<OWLClass> unsatisfiable = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			if (unsatisfiable.isEmpty() == false) {
				Logger.getLogger(TermCreationToolsMDef.class).warn("Unsatisfiable classes: "+unsatisfiable);
				if (unsatisfiable.contains(targetOntology.getOWLClass(iri))) {
					return InferredRelations.UNSATISFIABLE;
				}
				StringBuilder sb = new StringBuilder();
				for (OWLClass owlClass : unsatisfiable) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(targetOntology.getIdentifier(owlClass));
				}
				throw new RelationCreationException("No safe inferences are possible. The ontology has unsatisfiable classes: "+sb);
			}
			RelationshipTask task;
			if (assertInferences) {
				task = new InferAllRelationshipsTask(targetOntology, iri, changeTracker, tempIdPrefix, state, useIsInferred);
			}
			else {
				task = new SimpleRelationHandlingTask(targetOntology, iri, state);
			}
			task.run(reasoner);
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
			final Set<OWLClass> equivalentClasses = inferredRelations.getEquivalentClasses();
			ProcessState.addMessage(state, "Finished inferring relationships from logical definition.");
			
			if (equivalentClasses != null && !equivalentClasses.isEmpty()) {
				// quick exit, if there are existing classes
				return inferredRelations;
			}
			
			ProcessState.addMessage(state, "Start checking for part_of relationships.");
			if (!partOfExpressions.isEmpty()) {
				AddPartOfRelationshipsTask partOfTask = new AddPartOfRelationshipsTask(targetOntology, pair.getOne(), partOfExpressions , inferredRelations, state);
				partOfTask.run(reasoner);
			}
			ProcessState.addMessage(state, "Finished checking for part_of relationships.");
			return inferredRelations;
		} finally {
			reasoner.dispose();
		}
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

	protected boolean addTerm(String label, String definition, List<String> comments, List<ISynonym> synonyms, List<MDef> logicalDefinition, List<MDef> partOf, List<TermGenerationOutput> output) {
		ProcessState.addMessage(state, "Checking state of current ontology.");
		SharedReasoner manager = factory.getSharedReasoner(targetOntology);
		List<String> errors = manager.checkConsistency(targetOntology);
		if (errors != null && !errors.isEmpty()) {
			for(String error : errors) {
				output.add(singleError("Cannot safely create term: "+error, input));
			}
			return false;
		}
		ProcessState.addMessage(state, "Start creating term.");
		final List<String> warnings = new ArrayList<String>(1);
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
		boolean hasLiteratureReference = false;
		List<String> defxrefs = getDefXrefs();
		if (defxrefs != null) {
			// check xref conformity
			boolean hasXRef = false;
			
			for (String defxref : defxrefs) {
				// check if the termgenie def_xref is already in the list
				hasXRef = hasXRef || defxref.equals(defaultXref);
	
				// simple defxref check
				String xrefError = XrefTools.checkXref(defxref);
				if (xrefError != null) {
					output.add(singleError(xrefError, input));
					continue;
				}
				if (hasLiteratureReference == false) {
					hasLiteratureReference = XrefTools.isLiteratureReference(defxref);
				}
			}
			if (!hasXRef) {
				// add the termgenie def_xref
				ArrayList<String> newlist = new ArrayList<String>(defxrefs.size() + 1);
				newlist.addAll(defxrefs);
				newlist.add(defaultXref);
				defxrefs = newlist;
			}
		}
		else {
			defxrefs = Collections.singletonList(defaultXref);
		}
		if (!hasLiteratureReference) {
			if (requireLiteratureReference) {
				output.add(singleError(XrefTools.getLiteratureReferenceErrorString(false), input));
				return false;
			}
			warnings.add(XrefTools.getLiteratureReferenceErrorString(true));
		}
		// check for a pattern-specific definition xrefs
		String definitionXref = this.input.getTermTemplate().getDefinitionXref();
		if (definitionXref != null) {
			List<String> tempXrefs = new ArrayList<String>(defxrefs.size() + 1);
			tempXrefs.addAll(defxrefs);
			tempXrefs.add(definitionXref);
			defxrefs = tempXrefs;
		}
		
		// Check definition
		String definitionErrors = TextualDefinitionTool.validateDefinition(definition);
		if (definitionErrors != null) {
			output.add(singleError(definitionErrors, input));
			return false;
		}
		OboTools.addDefinition(term, definition, defxrefs);
	
		term.addClause(new Clause(OboFormatTag.TAG_CREATION_DATE, getDate()));
		term.addClause(new Clause(OboFormatTag.TAG_CREATED_BY, "TermGenie"));
		String comment = getInput("Comment");
		if (comment != null && comment.length() > 0) {
			term.addClause(new Clause(OboFormatTag.TAG_COMMENT, comment));
		}
		if (comments != null) {
			for (String c : comments) {
				c = StringUtils.trimToNull(c);
				if (c != null) {
					term.addClause(new Clause(OboFormatTag.TAG_COMMENT, c));
				}
			}
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
		IRI iri = IRI.create(owlNewId);
		String oboNewId = Owl2Obo.getIdentifier(iri);
		OboTools.addTermId(term, oboNewId);
	
		try {
			InferredRelations inferredRelations = createRelations(logicalDefinition, partOf, owlNewId, label, changeTracker);
			if (inferredRelations.isUnsatisfiable()) {
				output.add(singleError("Failed to create the term "+label+
						" with the logical definition: "+ renderLogicalDefinition(logicalDefinition) +
						" The term is not satisfiable.", input));
				return false;
			}
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
			if (inferredRelations.getClassRelationAxioms() != null) {
				axioms.addAll(inferredRelations.getClassRelationAxioms());
			}
			OWLClass cls = targetOntology.getManager().getOWLDataFactory().getOWLClass(iri);
			axioms.addAll(OwlTranslatorTools.translate(term.getClauses(), cls , targetOntology.getSourceOntology()));
			
			List<Clause> relations = inferredRelations.getClassRelations();
			if (relations != null) {
				term.getClauses().addAll(relations);
			}
			output.add(success(term, axioms, inferredRelations.getChanged(), warnings, input));
			
			ProcessState.addMessage(state, "Finished creating term.");
			return true;
		} catch (RelationCreationException exception) {
			output.add(singleError(exception.getMessage(), input));
			return false;
		} 
//		catch (Throwable t) {
//			Logger.getLogger(TermCreationToolsMDef.class).error(t.getMessage(), t);
//			output.add(singleError(t.getMessage(), input));
//			return false;
//		}
	}

	private List<String> getDefXrefs() {
		return merge(getInputs("DefX_Ref"), merge(getInputs("Literature_Ref"), getInputs("Literature_Refs")));
	}
	
	private List<String> merge(List<String> l1, List<String> l2) {
		if (l1 == null || l1.isEmpty()) {
			return l2;
		}
		if (l2 == null || l2.isEmpty()) {
			return l1;
		}
		List<String> r = new ArrayList<String>(l1.size() + l2.size());
		r.addAll(l1);
		r.addAll(l2);
		return r;
	}
	
	private String getDate() {
		return df.get().format(new Date());
	}

	protected TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return TermGenerationOutput.error(input, message);
	}

	protected TermGenerationOutput success(Frame term, Set<OWLAxiom> owlAxioms, List<Pair<Frame, Set<OWLAxiom>>> changedTermRelations, List<String> warnings, TermGenerationInput input) {
		return new TermGenerationOutput(term, owlAxioms, changedTermRelations, input, null, warnings);
	}
	
	@Override
	public boolean hasChanges() {
		return changeTracker.undoChanges() == false;
	}

	static Pair<OWLClass, OWLAxiom> createClass(IRI iri, OWLOntologyManager manager) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass owlClass = factory.getOWLClass(iri);
		OWLDeclarationAxiom owlDeclarationAxiom = factory.getOWLDeclarationAxiom(owlClass);
		return Pair.<OWLClass, OWLAxiom>of(owlClass, owlDeclarationAxiom);
	}
	
	static Pair<OWLClass, OWLAxiom> addClass(IRI iri, OWLChangeTracker changeTracker) {
		OWLOntology target = changeTracker.getTarget();
		Pair<OWLClass,OWLAxiom> pair = createClass(iri, target.getOWLOntologyManager());
		changeTracker.apply(new AddAxiom(target, pair.getTwo()));
		return pair;
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
