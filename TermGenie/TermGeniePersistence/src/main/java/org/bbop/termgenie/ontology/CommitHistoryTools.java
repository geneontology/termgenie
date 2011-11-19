package org.bbop.termgenie.ontology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;

public class CommitHistoryTools {

	private static final Logger logger = Logger.getLogger(CommitHistoryTools.class);
	
	private CommitHistoryTools() {
		// no instances allowed
	}
	
	public static CommitedOntologyTerm create(Frame frame, Modification operation) {
		CommitedOntologyTerm term = new CommitedOntologyTerm();
		term.setId(frame.getId());
		term.setOperation(operation);
		term.setLabel(frame.getTagValue(OboFormatTag.TAG_NAME, String.class));
		try {
			term.setObo(OboWriterTools.writeFrame(frame, null));
		} catch (IOException exception) {
			logger.error("Could not translate term: "+frame.getId(), exception);
			return null;
		}
		return term;
	}
	
	public static SimpleCommitedOntologyTerm createSimple(Frame frame, Modification operation) {
		SimpleCommitedOntologyTerm term = new SimpleCommitedOntologyTerm();
		term.setId(frame.getId());
		term.setOperation(operation);
		try {
			term.setObo(OboWriterTools.writeFrame(frame, null));
		} catch (IOException exception) {
			logger.error("Could not translate term: "+frame.getId(), exception);
			return null;
		}
		return term;
	}

	public static CommitHistoryItem create(List<CommitObject<TermCommit>> terms,
			String commitMessage,
			UserData userData,
			Date date)
	{
		CommitHistoryItem item = new CommitHistoryItem();

		item.setTerms(translateTerms(terms));
		item.setCommitMessage(commitMessage);
		item.setEmail(userData.getEmail());
		item.setDate(date);

		return item;
	}
	
	private static List<CommitedOntologyTerm> translateTerms(List<CommitObject<TermCommit>> terms)
	{
		List<CommitedOntologyTerm> result = null;
		if (terms != null && !terms.isEmpty()) {
			result = new ArrayList<CommitedOntologyTerm>(terms.size());
			for (CommitObject<TermCommit> commitObject : terms) {
				TermCommit termCommit = commitObject.getObject();
				Frame frame = termCommit.getTerm();
				CommitedOntologyTerm term = create(frame, commitObject.getType());
				List<SimpleCommitedOntologyTerm> changed = null;
				List<Frame> changedFrames = termCommit.getChanged();
				if (changedFrames != null && !changedFrames.isEmpty()) {
					changed = new ArrayList<SimpleCommitedOntologyTerm>(changedFrames.size());
					for(Frame changedFrame : changedFrames) {
						changed.add(createSimple(changedFrame, Modification.modify));
					}
				}
				term.setChanged(changed);
				
				result.add(term);
			}
		}
		return result;
	}

	
	public static List<CommitObject<TermCommit>> translate(CommitHistoryItem item) {
		List<CommitObject<TermCommit>> result = new ArrayList<CommitObject<TermCommit>>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm commitedOntologyTerm : terms) {
			Frame term = translate(commitedOntologyTerm.getId(), commitedOntologyTerm.getObo());
			if (term != null) {
				List<Frame> changed = translateSimple(commitedOntologyTerm.getChanged());
				TermCommit termCommit = new TermCommit(term, changed);
				CommitObject<TermCommit> commitObject = new CommitObject<TermCommit>(termCommit, commitedOntologyTerm.getOperation());
				result.add(commitObject);
			}
		}
		return result;
	}
	
	public static List<Frame> translate(List<CommitedOntologyTerm> changed) {
		List<Frame> result = null;
		if (changed != null && !changed.isEmpty()) {
			result = new ArrayList<Frame>(changed.size());
			for(CommitedOntologyTerm term : changed) {
				Frame frame = translate(term.getId(), term.getObo());
				if (frame != null) {
					result.add(frame);
				}
			}
		}
		return result;
	}
	
	public static List<Frame> translateSimple(List<SimpleCommitedOntologyTerm> changed) {
		List<Frame> result = null;
		if (changed != null && !changed.isEmpty()) {
			result = new ArrayList<Frame>(changed.size());
			for(SimpleCommitedOntologyTerm term : changed) {
				Frame frame = translate(term.getId(), term.getObo());
				if (frame != null) {
					result.add(frame);
				}
			}
		}
		return result;
	}

	public static Frame translate(String id, String obo) {
		OBOFormatParser p = new OBOFormatParser();
		OBODoc obodoc = new OBODoc();
		BufferedReader r = new BufferedReader(new StringReader(obo));
		p.setReader(r);
		p.parseTermFrame(obodoc);
		try {
			r.close();
		} catch (IOException exception) {
			logger.error("Could not close stream.", exception);
		}
		return obodoc.getTermFrame(id);
	}
}
