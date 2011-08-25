package org.bbop.termgenie.rules;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.OntologyTerm.DefaultOntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationOutput;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.rules.TermGenieScriptFunctions.CDef;
import org.bbop.termgenie.rules.TermGenieScriptFunctions.CDef.Differentium;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapper.Synonym;

/**
 * Implementation of the term creation for given parameters.
 */
public class TermCreationTools {
	
	private static final Logger logger = Logger.getLogger(TermCreationTools.class);

	final ReasonerFactory factory;
	final TermGenerationInput input;
	private final String targetOntologyId;
	final OWLGraphWrapper targetOntology;
	private final String patternID;
	private final TermGenieObo2Owl obo2Owl;
	private int count = 0;

	
	/**
	 * @param input
	 * @param targetOntology
	 * @param patternID
	 * @param factory
	 */
	TermCreationTools(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String patternID,
			ReasonerFactory factory)
	{
		super();
		this.input = input;
		this.targetOntology = targetOntology;
		this.targetOntologyId = targetOntology.getOntologyId();
		this.patternID = targetOntology.getOntologyId().toUpperCase() + ":" + patternID;
		this.factory = factory;
		this.obo2Owl = new TermGenieObo2Owl(targetOntology.getManager());
		obo2Owl.setObodoc(new OBODoc());
		obo2Owl.setOwlOntology(targetOntology.getSourceOntology());
	}

	private final class TermGenieObo2Owl extends Obo2Owl {
		
		ArrayList<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		private OWLOntology owlOntology;
		private final OWLOntologyManager manager;
	
		private TermGenieObo2Owl(OWLOntologyManager manager) {
			super(manager);
			this.manager = manager;
		}
	
		@Override
		protected synchronized void apply(OWLOntologyChange change) {
			super.apply(change);
			changes.add(change);
		}
	
		@Override
		public void setOwlOntology(OWLOntology owlOntology) {
			this.owlOntology = owlOntology;
			super.setOwlOntology(owlOntology);
		}
		
		
		/**
		 * @return true if all changes have been reverted.
		 */
		synchronized boolean undoChanges() {
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
	}

	private String getNewId() {
		String id = patternID + count;
		count += 1;
		return id;
	}

	protected int getFieldPos(String name) {
		return input.getTermTemplate().getFieldPos(name);
	}

	protected String[] getInputs(String name) {
		String[] strings = getFieldStrings(name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return strings;
	}

	protected String getInput(String name) {
		String[] strings = getFieldStrings(name);
		if (strings == null || strings.length < 1) {
			return null;
		}
		return strings[0];
	}

	protected String[] getFieldStrings(String name) {
		int pos = getFieldPos(name);
		if (pos < 0) {
			return null;
		}
		String[][] matrix = input.getParameters().getStrings();
		if (matrix.length <= pos) {
			return null;
		}
		return matrix[pos];
	}

	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");

	protected void addTerm(String label,
			String definition,
			List<Synonym> synonyms,
			CDef logicalDefinition,
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
		metaData.put("creation_date", getDate());
		metaData.put("created_by", "TermGenie");
		metaData.put("resource", targetOntology.getOntologyId());
		metaData.put("comment", getInput("Comment"));

		String newId = getNewId();

		List<IRelation> relations;

		if (logicalDefinition != null) {
			relations = new ArrayList<IRelation>();
			Pair<OWLObject, OWLGraphWrapper> genus = logicalDefinition.getBase();
			addIntersection(relations, newId, genus.getOne(), genus.getTwo());

			List<Differentium> differentia = logicalDefinition.getDifferentia();
			for (Differentium differentium : differentia) {
				List<OWLObject> terms = differentium.getTerms();
				String relation = differentium.getRelation();
				for (int i = 0; i < terms.size(); i++) {
					addIntersection(relations,
							newId,
							relation,
							terms.get(i),
							differentium.getOntologies());
					addRelation(relations,
							newId,
							relation,
							terms.get(i),
							differentium.getOntologies());

				}
			}
			List<IRelation> inferred = extractRelations(newId, relations);
			if (inferred != null && !inferred.isEmpty()) {
				relations.addAll(inferred);
			}
			Collections.sort(relations, new Comparator<IRelation>() {

				@Override
				public int compare(IRelation r1, IRelation r2) {
					// compare type
					String t1 = Relation.getType(r1.getProperties());
					String t2 = Relation.getType(r2.getProperties());
					int tv1 = value(t1);
					int tv2 = value(t2);
					if (tv1 == tv2) {
						if(tv1 == 4 || tv1 == 2 || tv1 == 1) {
							return r1.getTarget().compareTo(r2.getTarget());
						}
						if (tv1 == 3) {
							String rs1 = Relation.getRelationShip(r1.getProperties());
							String rs2 = Relation.getRelationShip(r2.getProperties());
							if (rs1 == null && rs2 == null) {
								return r1.getTarget().compareTo(r2.getTarget());
							}
							if (rs1 == null) {
								return -1;
							}
							if (rs2 == null) {
								return 1;
							}
							if (rs1.equals(rs2)) {
								return r1.getTarget().compareTo(r2.getTarget());
							}
							return rs1.compareTo(rs2);
						}
						if (tv1 == 0) {
							if (t1.equals(t2)) {
								return r1.getTarget().compareTo(r2.getTarget());
							}
							return t1.compareTo(t2);
						}
						return 0;
					}
					return tv2 - tv1;
				}
				
				private int value(String type) {
					if(type.equals(OboFormatTag.TAG_IS_A.getTag())) {
						return 4;
					}
					if (type.equals(OboFormatTag.TAG_INTERSECTION_OF.getTag())) {
						return 3;
					}
					if (type.equals(OboFormatTag.TAG_UNION_OF.getTag())) {
						return 2;
					}
					if (type.equals(OboFormatTag.TAG_DISJOINT_FROM.getTag())) {
						return 1;
					}
					return 0;
				}
				
			});
		}
		else {
			relations = Collections.emptyList();
		}
		DefaultOntologyTerm term = new DefaultOntologyTerm(newId, label, definition, synonyms, defxrefs, metaData, relations);

		output.add(success(term, input));

	}

	private List<IRelation> extractRelations(String newId, List<IRelation> knownRelations) {

		Frame termFrame = new Frame(FrameType.TERM);
		termFrame.setId(newId);
		OBOConverterTools.fillRelations(termFrame, knownRelations, null);

		OWLClassExpression cls = obo2Owl.trTermFrame(termFrame);

		factory.updateBuffered(targetOntologyId);
		ReasonerTaskManager reasonerManager = factory.getDefaultTaskManager(targetOntology);
		InferRelationshipsTask task = new InferRelationshipsTask(targetOntology, cls, knownRelations);
		reasonerManager.runManagedTask(task);
		return task.getRelations();
	}

	private void addRelation(List<IRelation> relations,
			String source,
			String relationship,
			OWLObject x,
			List<OWLGraphWrapper> ontologies)
	{
		String id = null;
		String label = null;
		for (OWLGraphWrapper ontology : ontologies) {
			id = ontology.getIdentifier(x);
			if (id != null) {
				label = ontology.getLabel(x);
				break;
			}
		}
		if (id != null) {
			Map<String, String> properties = new HashMap<String, String>();
			Relation.setType(properties, relationship);
			relations.add(new Relation(source, id, label, properties));
		}
	}

	private void addIntersection(List<IRelation> relations,
			String source,
			OWLObject x,
			OWLGraphWrapper ontology)
	{
		String target = ontology.getIdentifier(x);
		String targetLabel = ontology.getLabel(x);
		Map<String, String> properties = new HashMap<String, String>();
		Relation.setType(properties, OboFormatTag.TAG_INTERSECTION_OF);
		relations.add(new Relation(source, target, targetLabel, properties));
	}

	private void addIntersection(List<IRelation> relations,
			String source,
			String relationship,
			OWLObject x,
			List<OWLGraphWrapper> ontologies)
	{
		String id = null;
		String label = null;
		for (OWLGraphWrapper ontology : ontologies) {
			id = ontology.getIdentifier(x);
			if (id != null) {
				label = ontology.getLabel(x);
				break;
			}
		}
		if (id != null) {
			Map<String, String> properties = new HashMap<String, String>();
			Relation.setType(properties, OboFormatTag.TAG_INTERSECTION_OF, relationship);
			relations.add(new Relation(source, id, label, properties));
		}
	}

	private List<String> getDefXref() {
		String[] strings = getInputs("DefX_Ref");
		if (strings == null || strings.length == 0) {
			return null;
		}
		return Arrays.asList(strings);
	}

	private final static DateFormat df = new ISO8601DateFormat();

	private String getDate() {
		return df.format(new Date());
	}

	protected TermGenerationOutput singleError(String message, TermGenerationInput input) {
		return new TermGenerationOutput(null, input, false, message);
	}

	protected TermGenerationOutput success(OntologyTerm term, TermGenerationInput input) {
		return new TermGenerationOutput(term, input, true, null);
	}
	
	public boolean hasChanges() {
		return !obo2Owl.undoChanges();
	}
}
