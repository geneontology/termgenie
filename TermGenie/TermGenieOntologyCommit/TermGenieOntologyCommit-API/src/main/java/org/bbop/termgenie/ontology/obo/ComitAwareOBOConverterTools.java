package org.bbop.termgenie.ontology.obo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class ComitAwareOBOConverterTools extends OBOConverterTools {

	private static final Logger logger = Logger.getLogger(ComitAwareOBOConverterTools.class);

	public static enum LoadState {
		addSuccess,
		addMerge,
		addRedundant,
		addError,
		modifySuccess,
		modifyRedundant,
		modifyMissing,
		modifyError,
		removeSuccess,
		removeMissing,
		unknown;
		
		public static boolean isSuccess(LoadState state) {
			switch (state) {
				case addSuccess:
				case modifySuccess:
				case removeSuccess:
					return true;

				default:
					return false;
			}
		}
		
		public static boolean isMerge(LoadState state) {
			return state == LoadState.addMerge || state == LoadState.modifyMissing;
		}
		
		public static boolean isRedundant(LoadState state) {
			switch (state) {
				case addRedundant:
				case modifyRedundant:
				case removeMissing:
					return true;

				default:
					return false;
			}
		}
		
		public static boolean isError(LoadState  state) {
			return state == LoadState.addError || state == LoadState.modifyError || state == LoadState.unknown;
		}
	}
	
	public static LoadState handleTerm(OntologyTerm<? extends ISynonym, ? extends  IRelation> term,
			List<? extends IRelation> changed,
			Modification mode,
			OBODoc obodoc)
	{
		String id = term.getId();
		Frame frame = obodoc.getTermFrame(id);
		switch (mode) {
			case add: {
				LoadState state = LoadState.addSuccess;
				Frame newFrame = new Frame(FrameType.TERM);
				if (frame != null) {
					state = LoadState.addMerge;
				}
				fillOBO(newFrame, term);
				if (isRedundant(newFrame, frame)) {
					state = LoadState.addRedundant;
				}
				try {
					if (frame == null) {
						obodoc.addFrame(newFrame);
					}
					else {
						merge(frame, newFrame);
					}
					return state;
				} catch (FrameMergeException exception) {
					logger.error("Could not add new term to ontology: " + id, exception);
					return LoadState.addError;
				}
			}
			case modify: {
				if (frame == null) {
					return LoadState.modifyMissing;
				}
				try {
					Frame modFrame = new Frame(FrameType.TERM);
					fillOBO(modFrame, term);
					if (frameEquals(modFrame, frame)) {
						return LoadState.modifyRedundant;
					}
					merge(frame, modFrame);
					return LoadState.modifySuccess;
				} catch (FrameMergeException exception) {
					logger.warn("Could not apply changes to frame.", exception);
					return LoadState.modifyError;
				}
			}
			case remove:
				if (frame == null) {
					return LoadState.removeMissing;
				}
				Collection<Frame> frames = obodoc.getTermFrames();
				frames.remove(frame);
				return LoadState.removeSuccess;

			default:
				return LoadState.unknown;
		}
	}
	
	public static LoadState handleTerm(TermCommit termCommit,
			Modification mode,
			OBODoc obodoc)
	{
		return handleTerm(termCommit.getTerm(), termCommit.getChanged(), mode, obodoc);
	}
	
	private static void merge(Frame target, Frame addOn) throws FrameMergeException {
		if(target == addOn)
			return;
		
		if (!addOn.getId().equals(target.getId())) {
			throw new FrameMergeException("ids do not match");
		}
		if (!addOn.getType().equals(target.getType())) {
			throw new FrameMergeException("frame types do not match");
		}
		for (Clause addOnClause : addOn.getClauses()) {
			mergeClauses(target, addOnClause);
		}
	}

	private static void mergeClauses(Frame target, Clause addOnClause) {
		Collection<Clause> clauses = target.getClauses(addOnClause.getTag());
		boolean merge = true;
		for (Clause clause : clauses) {
			if (clause.equals(addOnClause)) {
				merge = false;
				break;
			}
		}
		if (merge) {
			target.addClause(addOnClause);
		}
	}
	
	private static boolean isRedundant(Frame newFrame, Frame frame) {
		if (frame == null) {
			return false;
		}
		if (newFrame == frame) {
			return true;
		}
		if (!objEquals(newFrame.getType(), frame.getType())) {
			return false;
		}
		if (!objEquals(newFrame.getId(), frame.getId())) {
			return false;
		}
		Set<String> newTags = newFrame.getTags();
		Set<String> tags = frame.getTags();
		if (!tags.containsAll(newTags)) {
			return false;
		}
		for (String tag : newTags) {
			Collection<Clause> newClauses = newFrame.getClauses(tag);
			Collection<Clause> clauses = frame.getClauses(tag);
			for (Clause newClause : newClauses) {
				boolean found = false;
				for (Clause clause : clauses) {
					if (newClause.equals(clause)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean frameEquals(Frame newFrame, Frame frame) {
		if (frame == null) {
			return false;
		}
		if (newFrame == frame) {
			return true;
		}
		if (!objEquals(newFrame.getType(), frame.getType())) {
			return false;
		}
		if (!objEquals(newFrame.getId(), frame.getId())) {
			return false;
		}
		Set<String> newTags = newFrame.getTags();
		Set<String> tags = frame.getTags();
		if (!tags.containsAll(newTags)) {
			return false;
		}
		if (!newTags.containsAll(tags)) {
			return false;
		}
		for (String tag : tags) {
			Collection<Clause> newClauses = newFrame.getClauses(tag);
			Collection<Clause> clauses = frame.getClauses(tag);
			if (newClauses.size() != clauses.size()) {
				return false;
			}
			for (Clause newClause : newClauses) {
				boolean found = false;
				for (Clause clause : clauses) {
					if (newClause.equals(clause)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}

	
	private static <T> boolean objEquals(T s1, T s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null) {
			return false;
		}
		return s1.equals(s2);
	}
}
