package org.bbop.termgenie.rules;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.rules.AbstractTermCreationTools.OWLChangeTracker;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import owltools.InferenceBuilder;
import owltools.graph.OWLGraphWrapper;


public class InferAllRelationshipsTask implements ReasonerTask {
	
	private static final Logger logger = Logger.getLogger(InferAllRelationshipsTask.class);
	
	private final OWLGraphWrapper ontology;
	private final IRI iri;
	private final OWLChangeTracker changeTracker;
	
	private List<IRelation> relations = Collections.emptyList();
	private Set<OWLClass> equivalentClasses = null;

	InferAllRelationshipsTask(OWLGraphWrapper ontology, IRI iri, OWLChangeTracker changeTracker) {
		super();
		this.ontology = ontology;
		this.iri = iri;
		this.changeTracker = changeTracker;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		InferenceBuilder inferenceBuilder = new InferenceBuilder(ontology, (OWLReasonerFactory) null, false);
		inferenceBuilder.setReasoner(reasoner);
		List<OWLAxiom> inferences = inferenceBuilder.buildInferences();
		for (OWLAxiom owlAxiom : inferences) {
			AddAxiom addAx = new AddAxiom(ontology.getSourceOntology(), owlAxiom);
			changeTracker.apply(addAx);
		}
		for(OWLAxiom redundant : inferenceBuilder.getRedundantAxioms()) {
			RemoveAxiom addAx = new RemoveAxiom(ontology.getSourceOntology(), redundant);
			changeTracker.apply(addAx);
		}
		OWLClass owlClass = ontology.getOWLClass(iri);
		Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(owlClass);
		if (equivalentClasses.getSize() > 1) {
			this.equivalentClasses = equivalentClasses.getEntities();
			this.equivalentClasses.remove(owlClass);
		} else {
			try {
				Owl2Obo owl2Obo = new Owl2Obo();
				OBODoc oboDoc = owl2Obo.convert(ontology.getSourceOntology());
				String frameId = Owl2Obo.getIdentifier(iri);
				Frame frame = oboDoc.getTermFrame(frameId);
				relations = OBOConverterTools.extractRelations(frame, oboDoc);
				
			} catch (OWLOntologyCreationException exception) {
				logger.warn("Could not create obo ontology from owl for ontology:"+ontology.getOntologyId(), exception);
			}
		}
		return Modified.no;
	}

	public List<IRelation> getRelations() {
		return relations;
	}

	/**
	 * @return the equivalentClasses
	 */
	public Set<OWLClass> getEquivalentClasses() {
		return equivalentClasses;
	}
}
