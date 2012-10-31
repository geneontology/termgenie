package org.bbop.termgenie.owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.ontology.obo.OwlTranslatorTools;
import org.bbop.termgenie.tools.Pair;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.InferenceBuilder;
import owltools.graph.AxiomAnnotationTools;
import owltools.graph.OWLGraphWrapper;


public class InferAllRelationshipsTask implements ReasonerTask {
	
	private final OWLGraphWrapper ontology;
	private final IRI iri;
	private final OWLChangeTracker changeTracker;
	private final String tempIdPrefix;
	private final ProcessState state;
	private final boolean useIsInferred;
	
	private InferredRelations result;

	public InferAllRelationshipsTask(OWLGraphWrapper ontology, IRI iri, OWLChangeTracker changeTracker, String tempIdPrefix, ProcessState state, boolean useIsInferred) {
		super();
		this.ontology = ontology;
		this.iri = iri;
		this.changeTracker = changeTracker;
		this.tempIdPrefix = tempIdPrefix;
		this.state = state;
		this.useIsInferred = useIsInferred;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		// First check for equivalent classes
		ProcessState.addMessage(state, "Check for equivalent classes of new term");
		OWLClass owlClass = ontology.getOWLClass(iri);
		Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass);
		if (equivalentClasses != null && equivalentClasses.getSize() > 1) {
			Set<OWLClass> equivalentClassesSet = equivalentClasses.getEntities();
			equivalentClassesSet.remove(owlClass);
			result = new InferredRelations(equivalentClassesSet);
			return Modified.no;
		}
		
		// remove redundant links and assert inferred ones
		ProcessState.addMessage(state, "Check for changed relations");
		InferenceBuilder inferenceBuilder = new InferenceBuilder(ontology, (OWLReasonerFactory) null, false);
		inferenceBuilder.setReasoner(reasoner);
		List<OWLAxiom> inferences = inferenceBuilder.buildInferences();
		OWLOntologyManager manager = ontology.getSourceOntology().getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		for (OWLAxiom owlAxiom : inferences) {
			if (useIsInferred) {
				owlAxiom = AxiomAnnotationTools.markAsInferredAxiom(owlAxiom, factory);
			}
			AddAxiom addAx = new AddAxiom(ontology.getSourceOntology(), owlAxiom);
			changeTracker.apply(addAx);
		}
		for(OWLAxiom redundant : inferenceBuilder.getRedundantAxioms()) {
			RemoveAxiom addAx = new RemoveAxiom(ontology.getSourceOntology(), redundant);
			changeTracker.apply(addAx);
		}
		List<Pair<Frame, Set<OWLAxiom>>> changed = null;
		NodeSet<OWLClass> subClasses = reasoner.getSubClasses(owlClass, true);
		if (subClasses != null && !subClasses.isEmpty()) {
			changed = new ArrayList<Pair<Frame, Set<OWLAxiom>>>();
			for (OWLClass subClass : subClasses.getFlattened()) {
				if (subClass.isBottomEntity()) {
					// skip owl:Nothing
					continue;
				}
				String subClassIRI = subClass.getIRI().toString();
				if (!subClassIRI.startsWith(tempIdPrefix)) {
					Frame frame = OboTools.createTermFrame(subClass);
					Pair<List<Clause>, Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(subClass,
							ontology);
					List<Clause> clauses = pair.getOne();
					OBOFormatWriter.sortTermClauses(clauses);
					frame.getClauses().addAll(clauses);
					changed.add(new Pair<Frame, Set<OWLAxiom>>(frame, pair.getTwo()));

				}
			}
			if (changed.isEmpty()) {
				changed = null;
			}
			Pair<List<Clause>,Set<OWLAxiom>> pair = OwlTranslatorTools.extractRelations(owlClass, ontology);
			result = new InferredRelations(pair.getOne(), pair.getTwo(), changed);
		}
		return Modified.no;
	}

	
	/**
	 * @return the {@link InferredRelations}
	 */
	public final InferredRelations getInferredRelations() {
		return result;
	}

}
