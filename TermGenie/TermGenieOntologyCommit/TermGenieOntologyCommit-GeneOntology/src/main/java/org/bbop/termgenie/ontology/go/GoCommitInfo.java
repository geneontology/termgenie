package org.bbop.termgenie.ontology.go;

import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;

import owltools.graph.OWLGraphWrapper.Synonym;


public class GoCommitInfo extends CommitInfo {

	/**
	 * Create a commit
	 * 
	 * @param terms
	 * @param relations
	 * @param termgenieUser
	 */
	public GoCommitInfo(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			List<CommitObject<Relation>> relations,
			String termgenieUser)
	{
		super(terms, relations, termgenieUser, CommitMode.internal, null, null);
	}

}
