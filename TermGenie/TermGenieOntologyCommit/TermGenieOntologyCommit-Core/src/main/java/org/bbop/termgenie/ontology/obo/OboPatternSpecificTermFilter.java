package org.bbop.termgenie.ontology.obo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.ontology.TermFilter;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
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
		return filter(item, position);
	}

	private List<CommitedOntologyTerm> filter(CommitHistoryItem item, int position) {
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
			boolean doSplitAndRemove = false;
			for (Clause clause : frame.getClauses(OboFormatTag.TAG_INTERSECTION_OF)) {
				String targetId = clause.getValue2(String.class);
				if (targetId == null) {
					targetId = clause.getValue(String.class);
				}
				if (targetId != null) {
					if(isExternalIdentifier(targetId, frame)) {
						doSplitAndRemove = true;
						break;
					}
				}
			}
			if (!doSplitAndRemove) {
				if (position == 0) {
					allFiltered.add(term);
				}
			}

			// split and remove
			try {
				CommitedOntologyTerm filtered = new CommitedOntologyTerm();
				filtered.setAxioms(term.getAxioms());
				filtered.setId(term.getId());
				filtered.setLabel(term.getLabel());
				filtered.setOperation(term.getOperation());

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
				filtered.setObo(OboWriterTools.writeFrame(newFrame, null));
				allFiltered.add(filtered);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
		return allFiltered;
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
