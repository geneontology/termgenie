package org.bbop.termgenie.owl;

import java.util.HashSet;
import java.util.Set;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.rules.ReasonerTaskManager.ReasonerTask;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import owltools.graph.OWLGraphWrapper;


/**
 * Simple mechanism to add part_of links. For a given set of class expressions,
 * check that there is a named class. If such a class exists, create the part_of
 * relationship/axiom.
 */
public class AddPartOfRelationshipsTask implements ReasonerTask {
	
	private final OWLGraphWrapper ontology;
	private final Set<OWLClassExpression> partOfExpressions;
	private final InferredRelations allRelations;
	private final OWLClass cls;
	
	/**
	 * @param ontology
	 * @param cls
	 * @param partOfExpressions
	 * @param allRelations
	 * @param state
	 */
	public AddPartOfRelationshipsTask(OWLGraphWrapper ontology, OWLClass cls, Set<OWLClassExpression> partOfExpressions, InferredRelations allRelations, ProcessState state) {
		super();
		this.ontology = ontology;
		this.cls = cls;
		this.partOfExpressions = partOfExpressions;
		this.allRelations = allRelations;
	}

	@Override
	public Modified run(OWLReasoner reasoner) {
		
		// Use the given class expressions to check for matching classes.
		// Add the classes as part of relations.
		
		OWLOntologyManager manager = ontology.getSourceOntology().getOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		final OWLClass owlNothing = factory.getOWLNothing();
		final OWLObjectProperty partOf = ontology.getOWLObjectPropertyByIdentifier("part_of");
		
		// use a set to ensure that part_of links are only added once.
		Set<OWLClass> usedClasses = new HashSet<OWLClass>();
		
		for(OWLClassExpression partOfExpression : partOfExpressions) {
			Set<OWLClass> classes = reasoner.getEquivalentClasses(partOfExpression).getEntities();
			// skip result sets which contain owl:Nothing (a.k.a. are unsatisfiable).
			if (classes.contains(owlNothing) == false) {
				for (OWLClass candidate : classes) {
					if (usedClasses.add(candidate)) {
						// create part_of clause
						String oboId = ontology.getIdentifier(candidate);
						Clause cl = new Clause(OboFormatTag.TAG_RELATIONSHIP);
						cl.addValue("part_of");
						cl.addValue(oboId);
						
						// create subClassOf statement
						OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(this.cls, factory.getOWLObjectSomeValuesFrom(partOf, candidate));
						
						// add to result
						allRelations.classRelations.add(cl);
						allRelations.classRelationAxioms.add(axiom);
					}
				}
			}
		}
		// sort clauses again
		OBOFormatWriter.sortTermClauses(allRelations.classRelations);
		
		return Modified.no;
	}

}
