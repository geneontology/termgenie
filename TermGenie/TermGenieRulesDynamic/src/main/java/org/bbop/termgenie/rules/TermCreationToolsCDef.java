package org.bbop.termgenie.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.core.rules.ReasonerFactory;
import org.bbop.termgenie.core.rules.ReasonerTaskManager;
import org.bbop.termgenie.core.rules.TermGenerationEngine.TermGenerationInput;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsCDef.CDef;
import org.bbop.termgenie.rules.TermGenieScriptFunctionsCDef.CDef.Differentium;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owltools.graph.OWLGraphWrapper;

/**
 * Implementation of the term creation, including relations from {@link CDef}.
 */
public class TermCreationToolsCDef extends AbstractTermCreationTools<CDef> {

	private static final Logger logger = Logger.getLogger(TermCreationToolsCDef.class);

	private final String targetOntologyId;

	/**
	 * @param input
	 * @param targetOntology
	 * @param tempIdPrefix
	 * @param patternID
	 * @param factory
	 */
	TermCreationToolsCDef(TermGenerationInput input,
			OWLGraphWrapper targetOntology,
			String tempIdPrefix,
			String patternID,
			ReasonerFactory factory)
	{
		super(input, targetOntology, tempIdPrefix, patternID, factory);
		this.targetOntologyId = targetOntology.getOntologyId();
	}

	private static class TObo2Owl extends Obo2Owl {
		
		private final OWLChangeTracker changeTracker;

		TObo2Owl(OWLOntologyManager manager, OWLChangeTracker changeTracker) {
			super(manager);
			this.changeTracker = changeTracker;
			
		}
		
		@Override
		protected void apply(OWLOntologyChange change) {
			changeTracker.apply(change);
		}

		@Override
		protected void setOwlOntology(OWLOntology owlOntology) {
			super.setOwlOntology(owlOntology);
		}
	}
	
	@Override
	protected List<IRelation> createRelations(CDef logicalDefinition,
			String newId,
			final OWLChangeTracker changeTracker)
	{
		List<IRelation> relations = Collections.emptyList();

		if (logicalDefinition != null) {
			TObo2Owl obo2Owl = new TObo2Owl(targetOntology.getManager(), changeTracker);
			obo2Owl.setObodoc(new OBODoc());
			obo2Owl.setOwlOntology(targetOntology.getSourceOntology());

			OWLClassExpression cls = createClass(newId, obo2Owl);
			relations = new ArrayList<IRelation>();
			Pair<OWLObject, OWLGraphWrapper> genus = logicalDefinition.getBase();
			addIntersection(relations, newId, genus.getOne(), genus.getTwo());

			List<Differentium> differentia = logicalDefinition.getDifferentia();
			// OWLOntologyManager manager =
			// targetOntology.getSourceOntology().getOWLOntologyManager();
			// OWLDataFactory owlFactory = manager.getOWLDataFactory();

			for (Differentium differentium : differentia) {
				List<OWLObject> terms = differentium.getTerms();

				// Set<? extends OWLAnnotation> annotations = null;
				// OWLClassExpression clsB =
				// targetOntology.getOWLClass(genus.getOne());
				// owlFactory.getOWLEquivalentClassesAxiom(cls, clsB,
				// annotations);

				String relation = differentium.getRelation();
				for (int i = 0; i < terms.size(); i++) {
					addIntersection(relations,
							newId,
							relation,
							terms.get(i),
							differentium.getOntologies());
				}
			}

			List<IRelation> inferred = extractRelations(newId, cls, relations, obo2Owl);
			if (inferred != null && !inferred.isEmpty()) {
				relations.addAll(inferred);
			}
		}
		return relations;
	}

	private OWLClassExpression createClass(String newId, Obo2Owl obo2Owl) {
		Frame termFrame = new Frame(FrameType.TERM);
		termFrame.setId(newId);
		OWLClassExpression cls = obo2Owl.trTermFrame(termFrame);
		return cls;
	}

	private List<IRelation> extractRelations(String newId,
			OWLClassExpression cls,
			List<IRelation> knownRelations,
			Obo2Owl obo2Owl)
	{

		Frame termFrame = new Frame(FrameType.TERM);
		termFrame.setId(newId);
		OBOConverterTools.fillRelations(termFrame, knownRelations, null);

		OWLClassExpression cls2 = obo2Owl.trTermFrame(termFrame);

		if (!cls.equals(cls2)) {
			logger.error("Can not infer relationships");
			return Collections.emptyList();
		}

		factory.updateBuffered(targetOntologyId);
		ReasonerTaskManager reasonerManager = factory.getDefaultTaskManager(targetOntology);
		InferIsARelationshipsTask task = new InferIsARelationshipsTask(targetOntology, cls);
		reasonerManager.runManagedTask(task);
		return task.getRelations();
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
}
