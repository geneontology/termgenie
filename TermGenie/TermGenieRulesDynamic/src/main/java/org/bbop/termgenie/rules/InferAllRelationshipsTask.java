package org.bbop.termgenie.rules;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.bbop.termgenie.rules.AbstractTermCreationTools.OWLChangeTracker;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.InferenceBuilder;
import owltools.graph.OWLGraphWrapper;


public class InferAllRelationshipsTask implements ReasonerTask {
	
	private static final Logger logger = Logger.getLogger(InferAllRelationshipsTask.class);
	
	private final OWLGraphWrapper ontology;
	private final String id;
	private final OWLClassExpression cls;
	private final OWLChangeTracker changeTracker;
	
	private List<IRelation> relations = Collections.emptyList();

	InferAllRelationshipsTask(OWLGraphWrapper ontology, String id, OWLClassExpression cls, OWLChangeTracker changeTracker) {
		super();
		this.ontology = ontology;
		this.id = id;
		this.cls = cls;
		this.changeTracker = changeTracker;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		InferenceBuilder inferenceBuilder = new InferenceBuilder(ontology);
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
		try {
			Owl2Obo owl2Obo = new Owl2Obo();
			OBODoc oboDoc = owl2Obo.convert(ontology.getSourceOntology());
			String frameId = Owl2Obo.getIdentifierFromObject(cls, ontology.getSourceOntology());
			Frame frame = oboDoc.getTermFrame(frameId);
			relations = OBOConverterTools.extractRelations(frame, oboDoc);
			
		} catch (OWLOntologyCreationException exception) {
			logger.warn("Could not create obo ontology from owl for ontology:"+ontology.getOntologyId(), exception);
		}
		
		return Modified.no;
	}

	public List<IRelation> getRelations() {
		return relations;
	}
}
