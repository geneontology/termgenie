package org.bbop.termgenie.ontology.go;

import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitInfo;
import org.bbop.termgenie.ontology.CommitObject;

import owltools.graph.OWLGraphWrapper.ISynonym;


public class GoCommitInfo extends CommitInfo {

	/**
	 * Create a commit
	 * 
	 * @param terms
	 * @param termgenieUser
	 */
	public GoCommitInfo(List<CommitObject<OntologyTerm<ISynonym, IRelation>>> terms,
			String termgenieUser)
	{
		super(terms, termgenieUser, CommitMode.internal, null, null);
	}

}
