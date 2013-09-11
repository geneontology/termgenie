package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.tools.Pair;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

public class OboTools {
	
	public static Frame createTermFrame(String id) {
		Frame frame = new Frame(FrameType.TERM);
		addTermId(frame, id);
		return frame;
	}
	
	public static Frame createTermFrame(OWLClass cls) {
		return createTermFrame(Owl2Obo.getIdentifier(cls.getIRI()));
	}

	public static Frame createTermFrame(String id, String label) {
		Frame frame = createTermFrame(id);
		addTermLabel(frame, label);
		return frame;
	}

	public static void addTermId(Frame frame, String id) {
		frame.setId(id);
		frame.addClause(new Clause(OboFormatTag.TAG_ID, id));
	}
	
	public static void addTermId(Frame frame, OWLClass cls) {
		addTermId(frame, Owl2Obo.getIdentifier(cls.getIRI()));
	}
	
	public static void addTermLabel(Frame frame, String label) {
		frame.addClause(new Clause(OboFormatTag.TAG_NAME, label));
	}
	
	public static void addDefinition(Frame frame, String def, Collection<String> xrefs) {
		Clause cl = new Clause(OboFormatTag.TAG_DEF, def);
		if (xrefs != null) {
			List<Xref> xrefList = new ArrayList<Xref>(xrefs.size());
			for (String idRef : xrefs) {
				xrefList.add(new Xref(idRef));
			}
			cl.setXrefs(xrefList);
		}
		frame.addClause(cl);
	}
	
	public static void addSynonym(Frame frame, String label, String scope, Collection<String> xrefs) {
		Clause cl = new Clause(OboFormatTag.TAG_SYNONYM, label);
		if (scope != null) {
			cl.addValue(scope);
		}
		List<Xref> xrefList;
		if (xrefs != null) {
			xrefList = new ArrayList<Xref>(xrefs.size());
			for (String idRef : xrefs) {
				xrefList.add(new Xref(idRef));
			}
			
		}
		else {
			xrefList = Collections.emptyList();
		}
		cl.setXrefs(xrefList);
		frame.addClause(cl);
	}
	
	public static void addObsolete(Frame frame, boolean obsolete) {
		if (obsolete) {
			Clause cl = new Clause(OboFormatTag.TAG_IS_OBSELETE);
			cl.setValue(Boolean.TRUE);
			frame.addClause(cl);
		}
	}
	
	public static boolean isScope(String s) {
		return OboFormatTag.TAG_EXACT.getTag().equals(s) ||
				OboFormatTag.TAG_NARROW.getTag().equals(s) ||
				OboFormatTag.TAG_RELATED.getTag().equals(s) ||
				OboFormatTag.TAG_BROAD.getTag().equals(s);
	}
	
	
	public static boolean isObsolete(Clause clause) {
		if (clause != null) {
			Object value = clause.getValue();
			if (value != null) {
				if (value instanceof Boolean) {
					return ((Boolean) value).booleanValue();
				}
				else if (value instanceof String) {
					String s = (String) value;
					return Boolean.TRUE.toString().equals(s);
				}
			}
		}
		return false;
	}
	
	public static boolean isObsolete(Frame frame) {
		return isObsolete(frame.getClause(OboFormatTag.TAG_IS_OBSELETE));
	}
	
	private static final Set<String> updateRelations = createUpdateRelations();
	
	private static Set<String> createUpdateRelations() {
		HashSet<String> set = new HashSet<String>();
		set.add(OboFormatTag.TAG_IS_A.getTag());
		set.add(OboFormatTag.TAG_INTERSECTION_OF.getTag());
		set.add(OboFormatTag.TAG_UNION_OF.getTag());
		// Do NOT add disjoint_from
		set.add(OboFormatTag.TAG_RELATIONSHIP.getTag());
		return Collections.unmodifiableSet(set);
	}
	
	private static final Set<String> allRelations = createAllRelations();
	
	private static Set<String> createAllRelations() {
		HashSet<String> set = new HashSet<String>();
		set.add(OboFormatTag.TAG_IS_A.getTag());
		set.add(OboFormatTag.TAG_INTERSECTION_OF.getTag());
		set.add(OboFormatTag.TAG_UNION_OF.getTag());
		set.add(OboFormatTag.TAG_DISJOINT_FROM.getTag());
		set.add(OboFormatTag.TAG_RELATIONSHIP.getTag());
		return Collections.unmodifiableSet(set);
	}
	
	public static void fillChangedRelations(OBODoc oboDoc,
			List<Pair<Frame, Set<OWLAxiom>>> changed,
			List<String> modIds)
	{
		if (changed != null && !changed.isEmpty()) {
			for(Pair<Frame, Set<OWLAxiom>> pair : changed) {
				Frame changedFrame = pair.getOne();
				String modId = changedFrame.getId();
				if (modIds != null) {
					modIds.add(modId);
				}
				Frame frame = oboDoc.getTermFrame(modId);
				if (frame == null) {
					// this can happen for terms, which are only in the main file,
					// but now have a cross-product in the xp file.
					frame = new Frame(FrameType.TERM);
					frame.setId(modId);
					frame.addClause(new Clause(OboFormatTag.TAG_ID, modId));
					try {
						oboDoc.addTermFrame(frame);
					} catch (FrameMergeException exception) {
						// This should not be possible.
						// We only create a term, if there is no previous one.
					}
				}
				else {
					// remove old relations, except disjoint_from
					removeRelations(frame, updateRelations);
				}
				
				// add updated relations
				List<Clause> relations = getRelations(changedFrame);
				for (Clause newRelation : relations) {
					String tag = newRelation.getTag();
					if (updateRelations.contains(tag)) {
						frame.addClause(newRelation);
					}
				}
			}
		}
	}

	public static List<Clause> getRelations(Frame frame) {
		List<Clause> result = new ArrayList<Clause>();
		addClauses(result, frame.getClauses(OboFormatTag.TAG_IS_A));
		addClauses(result, frame.getClauses(OboFormatTag.TAG_INTERSECTION_OF));
		addClauses(result, frame.getClauses(OboFormatTag.TAG_UNION_OF));
		addClauses(result, frame.getClauses(OboFormatTag.TAG_DISJOINT_FROM));
		addClauses(result, frame.getClauses(OboFormatTag.TAG_RELATIONSHIP));
		return result;
	}
	
	public static void removeAllRelations(Frame frame) {
		removeRelations(frame, allRelations);
	}
	
	public static void removeRelations(Frame frame, Set<String> tags) {
		Collection<Clause> clauses = frame.getClauses();
		Iterator<Clause> iterator = clauses.iterator();
		while (iterator.hasNext()) {
			Clause clause = iterator.next();
			String tag = clause.getTag();
			if (tags.contains(tag)) {
				iterator.remove();
			}
		}
	}
	
	private static void addClauses(List<Clause> clauses, Collection<Clause> clauses2) {
		if (clauses2 != null && !clauses2.isEmpty()) {
			clauses.addAll(clauses2);
		}
	}
	
	public static void updateClauseValues(Frame frame, OboFormatTag tag, Object...values) {
		if (frame != null && tag != null) {
			Clause clause = frame.getClause(tag);
			if (clause == null) {
				clause = new Clause(tag);
				frame.addClause(clause);
			}
			List<Object> newValues = new ArrayList<Object>(values.length);
			for(Object value : values) {
				newValues.add(value);
			}
			clause.setValues(newValues);
		}
	}
}
