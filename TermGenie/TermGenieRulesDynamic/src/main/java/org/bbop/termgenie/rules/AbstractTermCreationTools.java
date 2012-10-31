package org.bbop.termgenie.rules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.owl.InferredRelations;
import org.bbop.termgenie.owl.OWLChangeTracker;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Implementation of the term creation for given parameters.
 * 
 * @param <T> Logical definition class 
 */
public abstract class AbstractTermCreationTools<T> implements ChangeTracker {
	
	protected final ReasonerFactory factory;
	protected final TermGenerationInput input;
	protected final OWLGraphWrapper targetOntology;
	protected final ProcessState state;
	private final String patternID;
	private final OWLChangeTracker changeTracker;
	private int count = 0;

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 * @param state
	 */
	AbstractTermCreationTools(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory,
			ProcessState state)
	{
		super();
		this.input = input;
		this.targetOntology = targetOntology;
		this.state = state;
		this.patternID = tempIdPrefix + patternID;
		this.factory = factory;
		changeTracker = new OWLChangeTracker(targetOntology.getSourceOntology());
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

	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");

	protected boolean addTerm(String label,
			String definition,
			List<ISynonym> synonyms,
			T logicalDefinition,
			List<TermGenerationOutput> output)
	{
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
					output.add(singleError("Falied to create the term "+label+
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
	
	protected abstract InferredRelations createRelations(T logicalDefinition, String newId, String label, OWLChangeTracker changeTracker) throws RelationCreationException;

	protected abstract String renderLogicalDefinition(T logicalDefinition);
	
	private List<String> getDefXref() {
		return getInputs("DefX_Ref");
	}

	private final static ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>(){

		@Override
		protected DateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};

	private String getDate() {
		return df.get().format(new Date());
	}

	protected TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return TermGenerationOutput.error(input, message);
	}

	protected TermGenerationOutput success(Frame term,
			Set<OWLAxiom> owlAxioms,
			List<Pair<Frame, Set<OWLAxiom>>> changedTermRelations,
			TermGenerationInput input)
	{
		return new TermGenerationOutput(term, owlAxioms, changedTermRelations, input, true, null);
	}
	
	@Override
	public boolean hasChanges() {
		return changeTracker.undoChanges() == false;
	}
}
