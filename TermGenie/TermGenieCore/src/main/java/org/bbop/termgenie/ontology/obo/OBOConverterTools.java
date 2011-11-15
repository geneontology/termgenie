package org.bbop.termgenie.ontology.obo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Xref;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLClass;

public class OBOConverterTools {
	
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
	
	public static void addDefinition(Frame frame, String def, List<String> xrefs) {
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
		if (xrefs != null) {
			List<Xref> xrefList = new ArrayList<Xref>(xrefs.size());
			for (String idRef : xrefs) {
				xrefList.add(new Xref(idRef));
			}
			cl.setXrefs(xrefList);
		}
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
	
	public static void fillChangedRelations(OBODoc oboDoc,
			List<Frame> changed,
			List<String> modIds)
	{
		if (changed != null && !changed.isEmpty()) {
			for(Frame changedFrame : changed) {
				String modId = changedFrame.getId();
				modIds.add(modId);
				Frame frame = oboDoc.getTermFrame(modId);
				if (frame != null) {
					Collection<Clause> clauses = frame.getClauses();
					
					// remove old relations, except disjoint_from
					Iterator<Clause> iterator = clauses.iterator();
					while (iterator.hasNext()) {
						Clause clause = iterator.next();
						String tag = clause.getTag();
						if (updateRelations.contains(tag)) {
							iterator.remove();
						}
					}
					
					// add updated relations
					List<Clause> relations = getRelations(changedFrame);
					for (Clause newRelation : relations) {
						String tag = newRelation.getTag();
						if (updateRelations.contains(tag)) {
							clauses.add(newRelation);
						}
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
	
	private static void addClauses(List<Clause> clauses, Collection<Clause> clauses2) {
		if (clauses2 != null && !clauses2.isEmpty()) {
			clauses.addAll(clauses2);
		}
	}
}
