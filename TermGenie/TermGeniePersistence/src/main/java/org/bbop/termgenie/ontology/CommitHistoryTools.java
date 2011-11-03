package org.bbop.termgenie.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermRelation;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTermSynonym;
import org.bbop.termgenie.ontology.obo.OBOConverterTools;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class CommitHistoryTools {

	private static final Map<String, OboFormatTag> SCOPE_TAGS = new HashMap<String, OboFormatTag>();
	static {
		SCOPE_TAGS.put(OboFormatTag.TAG_RELATED.getTag(), OboFormatTag.TAG_RELATED);
		SCOPE_TAGS.put(OboFormatTag.TAG_EXACT.getTag(), OboFormatTag.TAG_EXACT);
		SCOPE_TAGS.put(OboFormatTag.TAG_BROAD.getTag(), OboFormatTag.TAG_BROAD);
		SCOPE_TAGS.put(OboFormatTag.TAG_NARROW.getTag(), OboFormatTag.TAG_NARROW);
	}
	
	private CommitHistoryTools() {
		// no instances allowed
	}
	
	public static CommitedOntologyTerm create(Frame frame, Modification operation) {
		CommitedOntologyTerm term = new CommitedOntologyTerm();
		term.setId(frame.getId());
		term.setOperation(operation);
		term.setLabel(frame.getTagValue(OboFormatTag.TAG_NAME, String.class));
		term.setDefinition(frame.getTagValue(OboFormatTag.TAG_DEF, String.class));
		term.setDefXRef(convertXrefs(frame.getTagXrefs(OboFormatTag.TAG_DEF.getTag())));
		term.setMetaData(convertMetaData(frame));
		term.setSynonyms(convertSynonyms(frame));
		term.setRelations(convertRelations(frame));
		return term;
	}

	private static List<CommitedOntologyTermRelation> convertRelations(Frame frame) {
		List<IRelation> relations = OBOConverterTools.extractRelations(frame, null);
		return translateRelations(relations);
	}

	private static List<CommitedOntologyTermSynonym> convertSynonyms(Frame frame) {
		List<CommitedOntologyTermSynonym> result = null;
		Collection<Clause> synonymClauses = frame.getClauses(OboFormatTag.TAG_SYNONYM);
		if (synonymClauses != null && !synonymClauses.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermSynonym>();
			for (Clause clause : synonymClauses) {
				CommitedOntologyTermSynonym synonym = new CommitedOntologyTermSynonym();
				synonym.setLabel(clause.getValue(String.class));
				String value2 = clause.getValue(String.class); 
				if (value2 != null && SCOPE_TAGS.containsKey(value2)) {
					synonym.setScope(value2);
				}
				Collection<Xref> xrefs = clause.getXrefs();
				if (xrefs != null && !xrefs.isEmpty()) {
					Set<String> xrefStrings  = new HashSet<String>();
					for (Xref xref : xrefs) {
						xrefStrings.add(xref.getIdref());
					}
					synonym.setXrefs(xrefStrings);
				}
				result.add(synonym);
			}
		}
		return result;
	}

	private static Map<String, String> convertMetaData(Frame frame) {
		Map<String, String> map = new HashMap<String, String>();
		addTag(frame, map, OboFormatTag.TAG_IS_ANONYMOUS);
		addTag(frame, map, OboFormatTag.TAG_NAMESPACE);
		addTag(frame, map, OboFormatTag.TAG_COMMENT);
		addTag(frame, map, OboFormatTag.TAG_CREATED_BY);
		addTag(frame, map, OboFormatTag.TAG_CREATION_DATE);
		addTag(frame, map, OboFormatTag.TAG_IS_OBSELETE);
		addTag(frame, map, OboFormatTag.TAG_REPLACED_BY);
		addTag(frame, map, OboFormatTag.TAG_CONSIDER);
		return map;
	}

	private static void addTag(Frame frame, Map<String, String> map, OboFormatTag oboTag) {
		final String tag = oboTag.getTag();
		String value = frame.getTagValue(tag, String.class);
		if (value != null) {
			map.put(tag, value);
		}
	}

	private static List<String> convertXrefs(Collection<Xref> tagXrefs) {
		List<String> result = null;
		if (tagXrefs != null && !tagXrefs.isEmpty()) {
			result = new ArrayList<String>(tagXrefs.size());
			for (Xref xref : tagXrefs) {
				result.add(xref.getIdref());
			}
		}
		return result;
	}

	public static CommitHistoryItem create(List<CommitObject<TermCommit>> terms,
			String user,
			Date date)
	{
		CommitHistoryItem item = new CommitHistoryItem();

		item.setTerms(translateTerms(terms));
		item.setUser(user);
		item.setDate(date);

		return item;
	}
	
	private static List<CommitedOntologyTerm> translateTerms(List<CommitObject<TermCommit>> terms)
	{
		List<CommitedOntologyTerm> result = null;
		if (terms != null && !terms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTerm>(terms.size());
			for (CommitObject<TermCommit> commitObject : terms) {
				CommitedOntologyTerm term = new CommitedOntologyTerm();
				
				OntologyTerm<ISynonym, IRelation> storedTerm = commitObject.getObject().getTerm();

				term.setId(storedTerm.getId());
				term.setLabel(storedTerm.getLabel());
				term.setDefinition(storedTerm.getDefinition());
				term.setDefXRef(storedTerm.getDefXRef());
				term.setMetaData(storedTerm.getMetaData());

				term.setSynonyms(translateSynonyms(storedTerm.getSynonyms()));
				term.setOperation(commitObject.getType());
				term.setRelations(translateRelations(storedTerm.getRelations()));
				term.setChanged(translateRelations(commitObject.getObject().getChanged()));
				result.add(term);
			}
		}
		return result;
	}

	private static List<CommitedOntologyTermSynonym> translateSynonyms(List<ISynonym> synonyms) {
		List<CommitedOntologyTermSynonym> result = null;
		if (synonyms != null && !synonyms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTermSynonym>(synonyms.size());
			for (ISynonym synonym : synonyms) {
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

	private static CommitedOntologyTermRelation translateRelation(IRelation relation)
	{
		CommitedOntologyTermRelation rel = new CommitedOntologyTermRelation();
		rel.setProperties(relation.getProperties());
		rel.setSource(relation.getSource());
		rel.setTarget(relation.getTarget());
		rel.setTargetLabel(relation.getTargetLabel());
		return rel;
	}

}
