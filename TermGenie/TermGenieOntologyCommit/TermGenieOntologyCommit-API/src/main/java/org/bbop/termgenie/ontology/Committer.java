package org.bbop.termgenie.ontology;

import java.util.List;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;


/**
 * Methods for commiting changes to an ontology
 */
public interface Committer {

	public boolean commit(List<CommitObject<OntologyTerm>> terms, List<CommitObject<Relation>> relations) throws CommitException;
}
