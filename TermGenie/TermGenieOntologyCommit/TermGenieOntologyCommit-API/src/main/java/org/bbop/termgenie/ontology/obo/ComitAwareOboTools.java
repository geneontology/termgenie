package org.bbop.termgenie.ontology.obo;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.CommitInfo.TermCommit;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;

public class ComitAwareOboTools extends OboTools {

	private static final Logger logger = Logger.getLogger(ComitAwareOboTools.class);

	public static boolean handleTerm(TermCommit termCommit,
			Modification modification,
			OBODoc oboDoc)
	{
		return handleTerm(termCommit.getTerm(), termCommit.getChanged(), modification, oboDoc);
	}

	public static boolean handleTerm(Frame term,
			List<Frame> changed,
			Modification mode,
			OBODoc obodoc)
	{
		fillChangedRelations(obodoc, changed, null);
		String id = term.getId();
		Frame frame = obodoc.getTermFrame(id);
		switch (mode) {
			case add: {
				if (frame != null) {
					return false;
				}
				try {
					obodoc.addFrame(term);
					return true;
				} catch (FrameMergeException exception) {
					logger.warn("Could not add new frame.", exception);
					return false;
				}
			}
			case modify: {
				if (frame == null) {
					return false;
				}
				try {
					merge(frame, term);
					return true;
				} catch (FrameMergeException exception) {
					logger.warn("Could not apply changes to frame.", exception);
					return false;
				}
			}
			case remove:
				if (frame == null) {
					return false;
				}
				Collection<Frame> frames = obodoc.getTermFrames();
				frames.remove(frame);
				return true;

			default:
				return false;
		}
	}

	private static void merge(Frame target, Frame addOn) throws FrameMergeException {
		if (target == addOn) return;

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

}
