package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.CommitObject;
import org.bbop.termgenie.ontology.entities.CommitHistory;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;

import owltools.graph.OWLGraphWrapper.Synonym;

public class CommitHistoryTools {
	
	private CommitHistoryTools() {
		// no instances allowed
	}

	public static void add(CommitHistory history, List<CommitObject<OntologyTerm>> terms, List<CommitObject<Relation>> relations, String user, Date date) {
		CommitHistoryItem item = new CommitHistoryItem();
	
		item.setTerms(translateTerms(terms));
		item.setRelations(translateCommitRelations(relations));
		item.setUser(user);
		item.setDate(date);
	
		synchronized (history) {
			List<CommitHistoryItem> items = history.getItems();
			if (items == null) {
				items = new ArrayList<CommitHistoryItem>();
			}
			items.add(item);
			history.setItems(items);
		}
	}

	public static CommitHistory create(List<CommitObject<OntologyTerm>> terms, List<CommitObject<Relation>> relations, String user, Date date) {
		CommitHistoryItem item = new CommitHistoryItem();
	
		item.setTerms(translateTerms(terms));
		item.setRelations(translateCommitRelations(relations));
		item.setUser(user);
		item.setDate(date);
	
		CommitHistory history = new CommitHistory();
		List<CommitHistoryItem> items = history.getItems();
		if (items == null) {
			items = new ArrayList<CommitHistoryItem>();
		}
		items.add(item);
		history.setItems(items);
		return history;
	
	}

	private static List<CommitedOntologyTerm> translateTerms(List<CommitObject<OntologyTerm>> terms) {
		List<CommitedOntologyTerm> result = null;
		if (terms != null && !terms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTerm>(terms.size());
			for (CommitObject<OntologyTerm> commitObject : terms) {
				CommitedOntologyTerm term = new CommitedOntologyTerm();
	
				term.setId(commitObject.getObject().getId());
				term.setLabel(commitObject.getObject().getLabel());
				term.setDefinition(commitObject.getObject().getDefinition());
				term.setDefXRef(commitObject.getObject().getDefXRef());
				term.setMetaData(commitObject.getObject().getMetaData());
	
				term.setSynonyms(translateSynonyms(commitObject.getObject().getSynonyms()));
				term.setOperation(translateOperation(commitObject.getType()));
				term.setRelations(translateRelations(commitObject.getObject().getRelations()));
				result.add(term);
			}
		}
		return result;
	}

	private static List<CommitedOntologyTermSynonym> translateSynonyms(List<Synonym> synonyms) {
		List<CommitedOntologyTermSynonym> result = null;
		if (synonyms != null && !synonyms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermSynonym>(synonyms.size());
			for (Synonym synonym : synonyms) {
				CommitedOntologyTermSynonym syn = new CommitedOntologyTermSynonym();
				syn.setCategory(synonym.getCategory());
				syn.setLabel(synonym.getLabel());
				syn.setScope(synonym.getScope());
				syn.setXrefs(synonym.getXrefs());
				result.add(syn);
			}
		}
		return result;
	}

	private static List<CommitedOntologyTermRelation> translateRelations(List<Relation> relations) {
		List<CommitedOntologyTermRelation> result = null;
		if (relations != null && !relations.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermRelation>(relations.size());
			for (Relation relation : relations) {
				result.add(translateRelation(relation, null));
			}
		}
		return result;
	}

	protected static CommitedOntologyTermRelation translateRelation(Relation relation, CommitObject.Modification type) {
		CommitedOntologyTermRelation rel = new CommitedOntologyTermRelation();
		rel.setOperation(translateOperation(type));
		rel.setProperties(relation.getProperties());
		rel.setSource(relation.getSource());
		rel.setTarget(relation.getTarget());
		return rel;
	}

	protected static List<CommitedOntologyTermRelation> translateCommitRelations(List<CommitObject<Relation>> relations) {
		List<CommitedOntologyTermRelation> result = null;
		if (relations != null && !relations.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermRelation>(relations.size());
			for (CommitObject<Relation> commitObj : relations) {
				result.add(translateRelation(commitObj.getObject(), commitObj.getType()));
			}
		}
		return result;
	}

	protected static int translateOperation(CommitObject.Modification type) {
		if (type == null) {
			return -1;
		}
		return type.ordinal();
	}

}
