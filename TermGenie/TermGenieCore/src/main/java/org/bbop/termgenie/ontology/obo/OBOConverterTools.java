package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class OBOConverterTools {

	public static void fillOBO(Frame frame, OntologyTerm<? extends ISynonym, ? extends IRelation> term) {
		String id = term.getId();
		frame.setId(id);
		frame.addClause(new Clause(OboFormatTag.TAG_ID, id));
		frame.addClause(new Clause(OboFormatTag.TAG_NAME, term.getLabel()));
		String definition = term.getDefinition();
		if (definition != null) {
			Clause cl = new Clause(OboFormatTag.TAG_DEF, definition);
			List<String> defXRef = term.getDefXRef();
			if (defXRef != null && !defXRef.isEmpty()) {
				for (String xref : defXRef) {
					cl.addXref(new Xref(xref));
				}
			}
			frame.addClause(cl);
		}
		Map<String, String> metaData = term.getMetaData();
		if (metaData != null && !metaData.isEmpty()) {
			for (Entry<String, String> entry : metaData.entrySet()) {
				frame.addClause(new Clause(entry.getKey(), entry.getValue()));
			}
		}
		fillSynonyms(frame, term.getSynonyms());
		fillRelations(frame, term.getRelations(), id);
	}

	public static void fillSynonyms(Frame frame, List<? extends ISynonym> synonyms) {
		if (synonyms != null && !synonyms.isEmpty()) {
			for (ISynonym termSynonym : synonyms) {
				Clause cl = new Clause(OboFormatTag.TAG_SYNONYM, termSynonym.getLabel());
				Set<String> defXRef = termSynonym.getXrefs();
				if (defXRef != null && !defXRef.isEmpty()) {
					for (String xref : defXRef) {
						cl.addXref(new Xref(xref));
					}
				}
				String scope = termSynonym.getScope();
				if (scope != null) {
					cl.addValue(scope);
				}
				String category = termSynonym.getCategory();
				if (category != null) {
					cl.addValue(category);
				}
				frame.addClause(cl);
			}
		}
	}
	
	public static void fillRelations(Frame frame, List<? extends IRelation> relations, String id) {
		if (relations != null && !relations.isEmpty()) {
			for (IRelation relation : relations) {
				fillRelation(frame, relation, id);
			}
		}
	}

	public static void fillRelation(Frame frame, IRelation relation, String id) {
		Clause clause = translateRelation(relation, id);
		if (clause != null) {
			frame.addClause(clause);
		}
	}
	
	public static List<Clause> translateRelations(List<IRelation> relations, String id) {
		List<Clause> result = new ArrayList<Clause>(relations.size());
		for(IRelation relation : relations) {
			Clause clause = translateRelation(relation, id);
			if (clause != null) {
				result.add(clause);
			}
		}
		return result;
	}
	
	public static Clause translateRelation(IRelation relation, String id) {
		Clause result = null;
		if (id == null || id.equals(relation.getSource())) {
			String target = relation.getTarget();
			Map<String, String> properties = relation.getProperties();
			if (properties != null && !properties.isEmpty()) {
				String type = Relation.getType(properties);
				result = createRelationClause(target, properties, type);
			}
		}
		return result;
	}

	private static Clause createRelationClause(String target,
			Map<String, String> properties,
			String type)
	{
		Clause cl = null;
		if (OboFormatTag.TAG_IS_A.getTag().equals(type)) {
			cl = new Clause(type, target);
		}
		else if (OboFormatTag.TAG_INTERSECTION_OF.getTag().equals(type)) {
			cl = new Clause(type);
			String relationShip = Relation.getRelationShip(properties);
			if (relationShip != null) {
				cl.addValue(relationShip);
			}
			cl.addValue(target);
		}
		else if (OboFormatTag.TAG_UNION_OF.getTag().equals(type)) {
			cl = new Clause(type, target);
		}
		else if (OboFormatTag.TAG_DISJOINT_FROM.getTag().equals(type)) {
			cl = new Clause(type, target);
		}
		else  if (OboFormatTag.TAG_RELATIONSHIP.getTag().equals(type)){
			cl = new Clause(type);
			cl.addValue(Relation.getRelationShip(properties));
			cl.addValue(target);
		}
		return cl;
	}
	
	public static List<IRelation> extractRelations(Frame termFrame, OBODoc oboDoc) {
		if (FrameType.TERM != termFrame.getType()) {
			return Collections.emptyList();
		}
		String source = termFrame.getId();
		List<IRelation> list = new ArrayList<IRelation>();
		addAllSimple(list, source, termFrame.getClauses(OboFormatTag.TAG_IS_A), oboDoc);
		addIntersections(list, source, termFrame.getClauses(OboFormatTag.TAG_INTERSECTION_OF), oboDoc);
		addAllSimple(list, source, termFrame.getClauses(OboFormatTag.TAG_UNION_OF), oboDoc);
		addAllSimple(list, source, termFrame.getClauses(OboFormatTag.TAG_DISJOINT_FROM), oboDoc);
		addRelations(list, source, termFrame.getClauses(OboFormatTag.TAG_RELATIONSHIP), oboDoc);
		return list;
	}

	private static void addAllSimple(List<IRelation> relations,
			String source,
			Collection<Clause> clauses,
			OBODoc oboDoc)
	{
		if (clauses != null && !clauses.isEmpty()) {
			for (Clause clause : clauses) {
				addSimple(relations, source, clause, oboDoc);
			}
		}
	}

	private static void addRelations(List<IRelation> list,
			String source,
			Collection<Clause> clauses,
			OBODoc oboDoc)
	{
		if (clauses != null && !clauses.isEmpty()) {
			for (Clause clause : clauses) {
				addLong(list, source, oboDoc, clause);
			}
		}
	}

	private static void addIntersections(List<IRelation> list,
			String source,
			Collection<Clause> clauses,
			OBODoc oboDoc)
	{
		if (clauses != null && !clauses.isEmpty()) {
			for (Clause clause : clauses) {
				if (clause.getValues().size() > 1) {
					addLong(list, source, oboDoc, clause);
				}
				else {
					addSimple(list, source, clause, oboDoc);
				}
			}
		}

	}

	private static void addSimple(List<IRelation> relations,
			String source,
			Clause clause,
			OBODoc oboDoc)
	{
		String target = clause.getValue(String.class);
		Map<String, String> properties = new HashMap<String, String>();
		Relation.setType(properties, clause.getTag());
		Relation rel = new Relation(source, target, getLabel(target, oboDoc), properties);
		relations.add(rel);
	}
	
	private static void addLong(List<IRelation> list, String source, OBODoc oboDoc, Clause clause)
	{
		String target = clause.getValue2(String.class);
		String label = getLabel(target, oboDoc);
		Map<String, String> properties = new HashMap<String, String>();
		Relation.setType(properties, clause.getTag(), clause.getValue(String.class));
		Relation rel = new Relation(source, target, label, properties);
		list.add(rel);
	}

	private static String getLabel(String id, OBODoc oboDoc) {
		String label = null;
		if (id != null && oboDoc != null) {
			Frame termFrame = oboDoc.getTermFrame(id);
			if (termFrame != null) {
				Clause clause = termFrame.getClause(OboFormatTag.TAG_NAME);
				if (clause != null) {
					label = clause.getValue(String.class);
				}
			}
		}
		return label;
	}
}
