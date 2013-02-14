package org.bbop.termgenie.ontology.obo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OboParserTools;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
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
		if (requiresSpecialHandling(item) == false) {
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
				if (position == 0) {
					allFiltered.add(term);
				}
				continue;
			}
			int requiredPosition = specialPatterns.get(pattern);
			if (position != 0 && position != requiredPosition) {
				// not the main ontology and not the required sub ontology:
				// return nothing
				return null;
			}
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

				// process main term
				if (doSplitAndRemoveMain) {
					Frame newFrame = filterFrame(position, requiredPosition, frame);
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
							Frame newFrame = filterFrame(position, requiredPosition, changedFrame);
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
		return allFiltered;
	}

	private Frame filterFrame(int position, int requiredPosition, Frame frame) {
		Frame newFrame = new Frame(frame.getType());
		newFrame.setId(frame.getId());
		for (Clause clause : frame.getClauses()) {
			String tag = clause.getTag();
			if (OboFormatTag.TAG_ID.getTag().equals(tag)) {
				// always add the id to the newFrame
				newFrame.addClause(clause);
			}
			else if (OboFormatTag.TAG_INTERSECTION_OF.getTag().equals(tag)) {
				// only add intersection of clauses if position match
				// (xp file),
				// otherwise do nothing
				if (position == requiredPosition) {
					newFrame.addClause(clause);
				}
			}
			else if (OboFormatTag.TAG_RELATIONSHIP.getTag().equals(tag)) {
				// remove relationship tags, when writing to xp file
			}
			else if (position == 0) {
				// add the rest of the clause only to the main file
				newFrame.addClause(clause);
			}
		}
		return newFrame;
	}
	
	protected boolean requiresSplit(Frame frame, OBODoc targetOntology) {
		// hook to overwrite the split criteria
		return hasExternalIntersectionOrUnknowRelation(frame, targetOntology);
	}
	
	private boolean hasExternalIntersectionOrUnknowRelation(Frame frame, OBODoc targetOntology) {
		boolean hasExternal = false;
		for (Clause clause : frame.getClauses(OboFormatTag.TAG_INTERSECTION_OF)) {
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
				if(isExternalIdentifier(targetId, frame)) {
					return true;
				}
			}
		}
		return hasExternal;
	}
	
	protected boolean isUnkownRelation(String id, OBODoc targetOntology) {
		boolean found = true;
		Frame typedefFrame = targetOntology.getTypedefFrame(id);
		if (typedefFrame != null) {
			found = false;
		}
		return found;
	}

	protected boolean isExternalIdentifier(String id, Frame frame) {
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

	private boolean requiresSpecialHandling(CommitHistoryItem item) {
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm term : terms) {
			String pattern = term.getPattern();
			if (pattern != null) {
				Integer pos = specialPatterns.get(pattern);
				return pos != null;
			}
		}
		return false;
	}
}
