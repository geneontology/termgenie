package org.bbop.termgenie.ontology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.bbop.termgenie.ontology.entities.CommitHistoryItem;
import org.bbop.termgenie.ontology.entities.CommitedOntologyTerm;
import org.bbop.termgenie.ontology.entities.SimpleCommitedOntologyTerm;
import org.bbop.termgenie.ontology.obo.OboWriterTools;
import org.bbop.termgenie.ontology.obo.OwlStringTools;
import org.bbop.termgenie.tools.Pair;
import org.bbop.termgenie.user.UserData;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.OWLAxiom;

public class CommitHistoryTools {

	private static final Logger logger = Logger.getLogger(CommitHistoryTools.class);
	
	private CommitHistoryTools() {
		// no instances allowed
	}
	
	public static CommitedOntologyTerm create(Frame frame, Modification operation, String owlAxioms, String pattern) {
		CommitedOntologyTerm term = create(frame, operation, pattern);
		term.setAxioms(owlAxioms);
		return term;
	}
	
	private static CommitedOntologyTerm create(Frame frame, Modification operation, String pattern) {
		CommitedOntologyTerm term = new CommitedOntologyTerm();
		term.setId(frame.getId());
		term.setOperation(operation);
		term.setLabel(frame.getTagValue(OboFormatTag.TAG_NAME, String.class));
		term.setPattern(pattern);
		try {
			term.setObo(OboWriterTools.writeFrame(frame, null));
		} catch (IOException exception) {
			logger.error("Could not translate term: "+frame.getId(), exception);
			return null;
		}
		return term;
	}
	
	public static boolean update(CommitedOntologyTerm term, Frame frame, Set<OWLAxiom> axioms, Modification operation) {
		return update(term, frame, create(axioms), operation);
	}
	
	public static boolean update(CommitedOntologyTerm term, Frame frame, String owlAxioms, Modification operation) {
		try {
			String obo = OboWriterTools.writeFrame(frame, null);
			term.setObo(obo);
		} catch (IOException exception) {
			logger.error("Could not translate term: "+frame.getId(), exception);
			return false;
		}
		term.setOperation(operation);
		term.setAxioms(owlAxioms);
		String label = frame.getTagValue(OboFormatTag.TAG_NAME, String.class);
		if (label != null) {
			term.setLabel(label);
		}
		return true;
	}
	
	public static SimpleCommitedOntologyTerm createSimple(Pair<Frame, Set<OWLAxiom>> pair, Modification operation) {
		Frame frame = pair.getOne();
		SimpleCommitedOntologyTerm term = new SimpleCommitedOntologyTerm();
		term.setId(frame.getId());
		term.setOperation(operation);
		term.setAxioms(create(pair.getTwo()));
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
		item.setSavedBy(userData.getScmAlias());
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
				CommitedOntologyTerm term = create(frame, commitObject.getType(), commitObject.getObject().getPattern());
				term.setAxioms(create(commitObject.getObject().getOwlAxioms()));
				List<SimpleCommitedOntologyTerm> changed = null;
				List<Pair<Frame, Set<OWLAxiom>>> changedFrames = termCommit.getChanged();
				if (changedFrames != null && !changedFrames.isEmpty()) {
					changed = new ArrayList<SimpleCommitedOntologyTerm>(changedFrames.size());
					for(Pair<Frame, Set<OWLAxiom>> pair : changedFrames) {
						changed.add(createSimple(pair, Modification.modify));
					}
				}
				term.setChanged(changed);
				
				result.add(term);
			}
		}
		return result;
	}

	
	private static String create(Set<OWLAxiom> owlAxioms) {
		if (owlAxioms == null || owlAxioms.isEmpty()) {
			return null;
		}
		return OwlStringTools.translateAxiomsToString(owlAxioms);
	}

	public static List<CommitObject<TermCommit>> translate(CommitHistoryItem item) {
		List<CommitObject<TermCommit>> result = new ArrayList<CommitObject<TermCommit>>();
		List<CommitedOntologyTerm> terms = item.getTerms();
		for (CommitedOntologyTerm commitedOntologyTerm : terms) {
			Frame term = translate(commitedOntologyTerm.getId(), commitedOntologyTerm.getObo());
			Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(commitedOntologyTerm.getAxioms());
			if (term != null) {
				List<Pair<Frame, Set<OWLAxiom>>> changed = translateSimple(commitedOntologyTerm.getChanged());
				TermCommit termCommit = new TermCommit(term, axioms, changed, commitedOntologyTerm.getPattern());
				CommitObject<TermCommit> commitObject = new CommitObject<TermCommit>(termCommit, commitedOntologyTerm.getOperation());
				result.add(commitObject);
			}
		}
		return result;
	}
	
	public static List<Pair<Frame, Set<OWLAxiom>>> translate(List<CommitedOntologyTerm> changed) {
		List<Pair<Frame, Set<OWLAxiom>>> result = null;
		if (changed != null && !changed.isEmpty()) {
			result = new ArrayList<Pair<Frame, Set<OWLAxiom>>>(changed.size());
			for(CommitedOntologyTerm term : changed) {
				Frame frame = translate(term.getId(), term.getObo());
				Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(term.getAxioms());
				if (frame != null) {
					result.add(new Pair<Frame, Set<OWLAxiom>>(frame, axioms));
				}
			}
		}
		return result;
	}
	
	public static List<Pair<Frame, Set<OWLAxiom>>> translateSimple(List<SimpleCommitedOntologyTerm> changed) {
		List<Pair<Frame, Set<OWLAxiom>>> result = null;
		if (changed != null && !changed.isEmpty()) {
			result = new ArrayList<Pair<Frame, Set<OWLAxiom>>>(changed.size());
			for(SimpleCommitedOntologyTerm term : changed) {
				Frame frame = translate(term.getId(), term.getObo());
				Set<OWLAxiom> axioms = OwlStringTools.translateStringToAxioms(term.getAxioms());
				if (frame != null) {
					result.add(new Pair<Frame, Set<OWLAxiom>>(frame, axioms));
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
