package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;

import owltools.graph.OWLGraphWrapper.Synonym;

public class CommitHistoryTools {

	private CommitHistoryTools() {
		// no instances allowed
	}

	public static CommitHistoryItem create(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms,
			String user,
			Date date)
	{
		CommitHistoryItem item = new CommitHistoryItem();

		item.setTerms(translateTerms(terms));
		item.setUser(user);
		item.setDate(date);

		return item;
	}
	
	public static Modification getModification(int type) {
		if (type >= 0 && type < Modification.values().length) {
			return Modification.values()[type];
		}
		return null;
	}

	private static List<CommitedOntologyTerm> translateTerms(List<CommitObject<OntologyTerm<Synonym, IRelation>>> terms)
	{
		List<CommitedOntologyTerm> result = null;
		if (terms != null && !terms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTerm>(terms.size());
			for (CommitObject<OntologyTerm<Synonym, IRelation>> commitObject : terms) {
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

	private static List<CommitedOntologyTermRelation> translateRelations(List<IRelation> relations) {
		List<CommitedOntologyTermRelation> result = null;
		if (relations != null && !relations.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermRelation>(relations.size());
			for (IRelation relation : relations) {
				result.add(translateRelation(relation));
			}
		}
		return result;
	}

	protected static CommitedOntologyTermRelation translateRelation(IRelation relation)
	{
		CommitedOntologyTermRelation rel = new CommitedOntologyTermRelation();
		rel.setProperties(relation.getProperties());
		rel.setSource(relation.getSource());
		rel.setTarget(relation.getTarget());
		rel.setTargetLabel(relation.getTargetLabel());
		return rel;
	}

	protected static int translateOperation(CommitObject.Modification type) {
		if (type == null) {
			return -1;
		}
		return type.ordinal();
	}

}
