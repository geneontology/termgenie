package org.bbop.termgenie.ontology.obo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

/**
 * OBO specific {@link TermFilter} to write intersection tags to cross product
 * files.
 */
public class OboPatternSpecificTermFilter implements TermFilter<OBODoc> {

	private final Map<String, Integer> specialPatterns;

	public OboPatternSpecificTermFilter(Map<String, Integer> specialPatterns) {
		this.specialPatterns = specialPatterns;
	}

	@Override
	public List<CommitedOntologyTerm> filterTerms(CommitHistoryItem item,
			OBODoc targetOntology,
			List<OBODoc> allOntologies,
			int position)
	{
		if (requiresSpecialHandling(item, targetOntology) == false) {
			if (position == 0) {
				return item.getTerms();
			}
			return null;
		}
		return filter(item, position, allOntologies.get(0));
	}

	private List<CommitedOntologyTerm> filter(CommitHistoryItem item, int position, OBODoc targetOntology) {
		List<CommitedOntologyTerm> allFiltered = new ArrayList<CommitedOntologyTerm>();
		for (CommitedOntologyTerm term : item.getTerms()) {
			String pattern = term.getPattern();
			if (pattern == null || specialPatterns.get(pattern) == null) {
				handleDefaultPatterns(position == 0, targetOntology, allFiltered, term);
				continue;
			}
			int requiredPosition = specialPatterns.get(pattern);
			if (position != 0 && position != requiredPosition) {
				// not the main ontology and not the required sub ontology:
				// return nothing
				continue;
			}
			boolean isMain = position == 0;
			// check that term contains references to external ontologies
			// use id prefix of OBO identifier for that
			Frame frame = OboParserTools.parseFrame(term.getId(), term.getObo());
			boolean doSplitAndRemoveMain = requiresSplit(frame, targetOntology);
			
			// also check in the changed terms for that, add or preserve existing split
			Map<SimpleCommitedOntologyTerm, Frame> filteringChanges = null;
			List<SimpleCommitedOntologyTerm> changedTerms = term.getChanged();
			if (changedTerms != null && !changedTerms.isEmpty()) {
				filteringChanges = new HashMap<SimpleCommitedOntologyTerm, Frame>();
				for (SimpleCommitedOntologyTerm changedTerm : changedTerms) {
					Frame changedFrame = OboParserTools.parseFrame(changedTerm.getId(), changedTerm.getObo());
					boolean doSplitAndRemoveChanged = requiresSplit(changedFrame, targetOntology);
					if (doSplitAndRemoveChanged) {
						filteringChanges.put(changedTerm, changedFrame);
					}
				}
			}
			
			// if no splits are required, only return terms for main ontology (position zero)
			if (!doSplitAndRemoveMain && (filteringChanges == null || filteringChanges.isEmpty())) {
				if (position == 0) {
					allFiltered.add(term);
				}
				continue;
			}

			// split and remove
			try {
				CommitedOntologyTerm filtered = new CommitedOntologyTerm();
				filtered.setAxioms(term.getAxioms());
				filtered.setId(term.getId());
				filtered.setLabel(term.getLabel());
				filtered.setOperation(term.getOperation());
				filtered.setPattern(term.getPattern());

				// process main term
				if (doSplitAndRemoveMain) {
					Frame newFrame = filterFrame(isMain, frame, targetOntology);
					filtered.setObo(OboWriterTools.writeFrame(newFrame, null));
				}
				else {
					filtered.setObo(term.getObo());
				}
				
				// process changes to other terms
				if (filteringChanges != null && !filteringChanges.isEmpty() && changedTerms != null) {
					// create a new list of changes
					List<SimpleCommitedOntologyTerm> newChangedTerms = new ArrayList<SimpleCommitedOntologyTerm>(changedTerms.size());
					for (SimpleCommitedOntologyTerm changedTerm : changedTerms) {
						Frame changedFrame = filteringChanges.get(changedTerm);
						if (changedFrame != null) {
							Frame newFrame = filterFrame(isMain, changedFrame, targetOntology);
							SimpleCommitedOntologyTerm newChangedTerm = new SimpleCommitedOntologyTerm();
							newChangedTerm.setId(changedTerm.getId());
							newChangedTerm.setOperation(changedTerm.getOperation());
							newChangedTerm.setAxioms(changedTerm.getAxioms());
							newChangedTerm.setUuid(changedTerm.getUuid());
							newChangedTerm.setObo(OboWriterTools.writeFrame(newFrame, null));
							newChangedTerms.add(newChangedTerm);
						}
						else {
							// unmodified changes are only applied to the main ontology
							if (position == 0) {
								newChangedTerms.add(changedTerm);
							}
						}
					}
					if (!newChangedTerms.isEmpty()) {
						// add modified list to filtered term
						filtered.setChanged(newChangedTerms);
					}
				}
				else {
					// if no modifications in the change list are required just put them in the main one
					if (position == 0) {
						filtered.setChanged(term.getChanged());
					}
				}
				allFiltered.add(filtered);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
		if (allFiltered.isEmpty()) {
			return null;
		}
		return allFiltered;
	}

	protected void handleDefaultPatterns(boolean isMain, OBODoc targetOntology, List<CommitedOntologyTerm> allFiltered,
			CommitedOntologyTerm term) throws RuntimeException
	{
		List<SimpleCommitedOntologyTerm> changed = term.getChanged();
		if (changed == null || changed.isEmpty()) {
			if (isMain) {
				allFiltered.add(term);
			}
		}
		else {
			// need check changed relations also, even if the submitted pattern does not require and update.
			boolean requiresReplace = false;
			List<SimpleCommitedOntologyTerm> newChangedTerms = new ArrayList<SimpleCommitedOntologyTerm>(changed.size());
			for (SimpleCommitedOntologyTerm changedTerm : changed) {
				Frame changedFrame = OboParserTools.parseFrame(changedTerm.getId(), changedTerm.getObo());
				if (changedFrame == null) {
					continue;
				}
				Frame newFrame = filterFrame(isMain, changedFrame, targetOntology);
				if (changedFrame != newFrame) {
					requiresReplace = true;
					SimpleCommitedOntologyTerm newChangedTerm = new SimpleCommitedOntologyTerm();
					newChangedTerm.setId(changedTerm.getId());
					newChangedTerm.setOperation(changedTerm.getOperation());
					newChangedTerm.setAxioms(changedTerm.getAxioms());
					newChangedTerm.setUuid(changedTerm.getUuid());
					try {
						newChangedTerm.setObo(OboWriterTools.writeFrame(newFrame, null));
					} catch (IOException exception) {
						throw new RuntimeException(exception);
					}
					newChangedTerms.add(newChangedTerm);
				}
				else {
					newChangedTerms.add(changedTerm);
				}
			}
			if (requiresReplace) {
				CommitedOntologyTerm filtered = new CommitedOntologyTerm();
				filtered.setAxioms(term.getAxioms());
				filtered.setId(term.getId());
				filtered.setLabel(term.getLabel());
				filtered.setOperation(term.getOperation());
				if (isMain) {
					filtered.setObo(term.getObo());
				}
				else {
					filtered.setObo("");
				}
				filtered.setPattern(term.getPattern());
				filtered.setChanged(newChangedTerms);
				allFiltered.add(filtered);
			}
			else {
				allFiltered.add(term);
			}
		}
	}

	private Frame filterFrame(boolean isMain, final Frame original, OBODoc targetOntology) {
		
		final boolean hasExternalIntersection = hasIntersectionOfWithExternalIdOrUnknowRelation(original, targetOntology);
		final boolean hasExternalRelations = hasRelationshipWithExternalIdOrUnknowRelation(original, targetOntology);
		final Collection<Clause> originalIntersections = original.getClauses(OboFormatTag.TAG_INTERSECTION_OF);
		
		Frame newFrame = new Frame(original.getType());
		newFrame.setId(original.getId());
		
		for (Clause clause : original.getClauses()) {
			String tag = clause.getTag();
			boolean add = false;
			if (OboFormatTag.TAG_ID.getTag().equals(tag)) {
				// always add the id to the newFrame
				add = true;
			}
			else if (OboFormatTag.TAG_INTERSECTION_OF.getTag().equals(tag)) {
				if (isMain) {
					add = !hasExternalIntersection;
				}
				else {
					add = hasExternalIntersection;
				}
			}
			else if (OboFormatTag.TAG_RELATIONSHIP.getTag().equals(tag)) {
				if (isMain) {
					if (hasExternalRelations == false) {
						add = true;
					}
					else {
						add = !hasExternalIdOrUnknowRelation(clause, original, targetOntology);
					}
				}
				else {
					if (hasExternalRelations) {
						// only add if they are not redundant with an intersectionOf clause
						if (isRedundantRelationShipClause(clause, originalIntersections) == false) {
							if (hasExternalIdOrUnknowRelation(clause, original, targetOntology)) {
								add = true;
							}
						}
					}
				}
			}
			else {
				add = isMain;
			}
			
			if (add) {
				newFrame.addClause(clause);
			}
		}
		if (original.getClauses().size() == newFrame.getClauses().size()) {
			return original;
		}
		return newFrame;
	}
	
	private boolean isRedundantRelationShipClause(Clause clause, Collection<Clause> intersectionClauses) {
		Object rel = clause.getValue();
		Object id = clause.getValue2();
		for (Clause intersection : intersectionClauses) {
			Collection<Object> values = intersection.getValues();
			if (values.size() == 2) {
				if (rel.equals(intersection.getValue()) && id.equals(intersection.getValue2())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return true, if the given frame needs to be split.
	 * 
	 * @param frame
	 * @param targetOntology
	 * @return boolean
	 */
	protected boolean requiresSplit(Frame frame, OBODoc targetOntology) {
		if (frame == null) {
			return false;
		}
		// hook to overwrite the split criteria
		boolean requiresSplit = hasIntersectionOfWithExternalIdOrUnknowRelation(frame, targetOntology)
				|| hasRelationshipWithExternalIdOrUnknowRelation(frame, targetOntology);
		return requiresSplit;
	}
	
	private boolean hasIntersectionOfWithExternalIdOrUnknowRelation(Frame frame, OBODoc targetOntology) {
		return hasExternalIdOrUnknowRelation(frame, targetOntology, OboFormatTag.TAG_INTERSECTION_OF);
	}
	
	private boolean hasRelationshipWithExternalIdOrUnknowRelation(Frame frame, OBODoc targetOntology) {
		return hasExternalIdOrUnknowRelation(frame, targetOntology, OboFormatTag.TAG_RELATIONSHIP);
	}

	/**
	 * Return true, if the frame contains a clause of type tag, which uses an
	 * external Id or unknown relationship.
	 * 
	 * @param frame
	 * @param targetOntology
	 * @param tag
	 * @return boolean
	 */
	private boolean hasExternalIdOrUnknowRelation(Frame frame, OBODoc targetOntology, OboFormatTag tag) {
		boolean matches = false;
		for (Clause clause : frame.getClauses(tag)) {
			String targetId = clause.getValue2(String.class);
			if (targetId == null) {
				targetId = clause.getValue(String.class);
			}
			else {
				String rel = clause.getValue(String.class);
				if (rel != null && isUnkownRelation(rel, targetOntology)) {
					return true;
				}
			}
			if (targetId != null) {
				if(isExternalIdentifier(targetId, frame, targetOntology)) {
					return true;
				}
			}
		}
		return matches;
	}
	
	/**
	 * Return true, if the clause uses an external Id or unknown relationship.
	 * 
	 * @param clause
	 * @param frame
	 * @param targetOntology
	 * @return boolean
	 */
	private boolean hasExternalIdOrUnknowRelation(Clause clause, Frame frame, OBODoc targetOntology) {
		String targetId = clause.getValue2(String.class);
		if (targetId == null) {
			targetId = clause.getValue(String.class);
		}
		else {
			String rel = clause.getValue(String.class);
			if (rel != null && isUnkownRelation(rel, targetOntology)) {
				return true;
			}
		}
		if (targetId != null) {
			if(isExternalIdentifier(targetId, frame, targetOntology)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return true, if the relation is not defined in the main ontology.
	 * 
	 * @param id
	 * @param targetOntology
	 * @return boolean
	 */
	protected boolean isUnkownRelation(String id, OBODoc targetOntology) {
		boolean found = true;
		Frame typedefFrame = targetOntology.getTypedefFrame(id);
		if (typedefFrame != null) {
			found = false;
		}
		return found;
	}

	/**
	 * Return true, if the id is not defined (external) to the main ontology.
	 * 
	 * @param id
	 * @param frame the frame, where the id is used in
	 * @param targetOntology main ontology
	 * @return boolean
	 */
	protected boolean isExternalIdentifier(String id, Frame frame, OBODoc targetOntology) {
		final String prefix = getIdPrefix(frame);
		return id.startsWith(prefix) == false;
	}
	
	protected String getIdPrefix(Frame frame) {
		String id = frame.getId();
		String prefix = "";
		int colonPos = id.indexOf(':');
		if (colonPos > 0) {
			prefix = id.substring(0, colonPos);
		}
		return prefix;
	}

	private boolean requiresSpecialHandling(CommitHistoryItem item, OBODoc targetOntology) {
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			String pattern = term.getPattern();
			if (pattern != null) {
				Integer pos = specialPatterns.get(pattern);
				if (pos != null) {
					return true;
				}
			}
			List<SimpleCommitedOntologyTerm> changed = term.getChanged();
			if (changed != null && !changed.isEmpty()) {
				for (SimpleCommitedOntologyTerm changedTerm : changed) {
					Frame changedFrame = OboParserTools.parseFrame(changedTerm.getId(), changedTerm.getObo());
					if(requiresSplit(changedFrame, targetOntology)){
						return true;
					}
				}
			}
		}
		return false;
	}
}
