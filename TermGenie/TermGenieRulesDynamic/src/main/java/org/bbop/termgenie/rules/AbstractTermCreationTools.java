package org.bbop.termgenie.rules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.AbstractOntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.ISynonym;

/**
 * Implementation of the term creation for given parameters.
 * 
 * @param <T> Logical definition class 
 */
public abstract class AbstractTermCreationTools<T> implements ChangeTracker {
	
	private static final Logger logger = Logger.getLogger(AbstractTermCreationTools.class);

	protected final ReasonerFactory factory;
	protected final TermGenerationInput input;
	protected final OWLGraphWrapper targetOntology;
	private final String patternID;
	private final OWLChangeTracker changeTracker;
	private int count = 0;

	
	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	AbstractTermCreationTools(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super();
		this.input = input;
		this.targetOntology = targetOntology;
		this.patternID = tempIdPrefix + patternID;
		this.factory = factory;
		changeTracker = new OWLChangeTracker(targetOntology.getSourceOntology());
	}

	protected static final class OWLChangeTracker {
		
		ArrayList<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		private final OWLOntology owlOntology;
		private final OWLOntologyManager manager;
	
		OWLChangeTracker(OWLOntology owlOntology) {
			this.owlOntology = owlOntology;
			this.manager = owlOntology.getOWLOntologyManager();
		}
	
		protected synchronized void apply(OWLOntologyChange change) {
			List<OWLOntologyChange> changes = manager.applyChange(change);
			if (changes != null && !changes.isEmpty()) {
				this.changes.addAll(changes);
			}
		}
	
		/**
		 * @return true if all changes have been reverted.
		 */
		protected synchronized boolean undoChanges() {
			boolean success = true;
			if (!changes.isEmpty()) {
				for (int i = changes.size() - 1; i >= 0 && success ; i--) {
					OWLOntologyChange change = changes.get(i);
					if (change instanceof AddAxiom) {
						AddAxiom addAxiom = (AddAxiom) change;
						success = applyChange(new RemoveAxiom(owlOntology, addAxiom.getAxiom()));
					}
					else if (change instanceof RemoveAxiom) {
						RemoveAxiom removeAxiom = (RemoveAxiom) change;
						success = applyChange(new AddAxiom(owlOntology, removeAxiom.getAxiom()));
					}
					else if (change instanceof AddOntologyAnnotation) {
						AddOntologyAnnotation addOntologyAnnotation = (AddOntologyAnnotation) change;
						success = applyChange(new RemoveOntologyAnnotation(owlOntology, addOntologyAnnotation.getAnnotation()));
					}
					else if (change instanceof RemoveOntologyAnnotation) {
						RemoveOntologyAnnotation removeOntologyAnnotation = (RemoveOntologyAnnotation) change;
						success = applyChange(new AddOntologyAnnotation(owlOntology, removeOntologyAnnotation.getAnnotation()));
					}
					else {
						success = false;
					}
				}
				if (success) {
					changes.clear();
				}
			}
			return success;
		}
		
		private boolean applyChange(OWLOntologyChange change) {
			try {
				manager.applyChange(change);
				return true;
			} catch (OWLOntologyRenameException exception) {
				logger.warn("Can not apply change", exception);
				return false;
			}
		}
		
		protected OWLOntology getTarget() {
			return owlOntology;
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

	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");

	protected void addTerm(String label,
			String definition,
			List<ISynonym> synonyms,
			T logicalDefinition,
			List<TermGenerationOutput> output)
	{

		// get overwrites from GUI
		String inputName = getInput("Name");
		if (inputName != null) {
			inputName = inputName.trim();
			if (inputName.length() > 1) {
				label = inputName;
			}
		}

		String inputDefinition = getInput("Definition");
		if (inputDefinition != null) {
			inputDefinition = inputDefinition.trim();
			if (inputDefinition.length() > 1) {
				definition = inputDefinition;
			}
		}

		// Fact Checking
		// check label
		OWLObject sameName = targetOntology.getOWLObjectByLabel(label);
		if (sameName != null) {
			output.add(singleError("The term " + targetOntology.getIdentifier(sameName) + " with the same label already exists",
					input));
			return;
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

		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put(OboFormatTag.TAG_CREATION_DATE.getTag(), getDate());
		metaData.put(OboFormatTag.TAG_CREATED_BY.getTag(), "TermGenie");
		String comment = getInput("Comment");
		if (comment != null && comment.length() > 0) {
			metaData.put(OboFormatTag.TAG_COMMENT.getTag(), comment);
		}
		String owlNewId = getNewId();

		try {
			InferredRelations inferredRelations = createRelations(logicalDefinition, owlNewId, label, changeTracker);
			if (inferredRelations.equivalentClasses != null) {
				for (OWLClass owlClass : inferredRelations.equivalentClasses) {
					output.add(singleError("The term " + targetOntology.getIdentifier(owlClass) +" '"+ targetOntology.getLabel(owlClass) +"' with the same logic definition already exists",
							input));					
				}
				return;
			}
			List<IRelation> relations = inferredRelations.classRelations;
			if (relations != null && !relations.isEmpty()) {
				Collections.sort(relations, IRelation.RELATION_SORT_COMPARATOR);
			}
			String oboNewId = Owl2Obo.getIdentifier(IRI.create(owlNewId));
			DefaultOntologyTerm term = new DefaultOntologyTerm(oboNewId, label, definition, synonyms, defxrefs, metaData, relations);
			output.add(success(term, inferredRelations.changed, input));
		} catch (RelationCreationException exception) {
			output.add(singleError(exception.getMessage(), input));
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

	static class InferredRelations {
		
		static final InferredRelations EMPTY = new InferredRelations(Collections.<IRelation>emptyList(), null);
		
		List<IRelation> classRelations = null;
		List<IRelation> changed = null;
		Set<OWLClass> equivalentClasses = null;
		
		/**
		 * @param equivalentClasses
		 */
		InferredRelations(Set<OWLClass> equivalentClasses) {
			this.equivalentClasses = equivalentClasses;
		}
		
		/**
		 * @param classRelations
		 * @param directSubClasses
		 */
		InferredRelations(List<IRelation> classRelations, List<IRelation> changed) {
			this.classRelations = classRelations;
			this.changed = changed;
		}
	}
	
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

	protected TermGenerationOutput success(OntologyTerm<ISynonym, IRelation> term, List<IRelation> changed, TermGenerationInput input) {
		return new TermGenerationOutput(term, changed, input, true, null);
	}
	
	@Override
	public boolean hasChanges() {
		return changeTracker.undoChanges() == false;
	}
}
