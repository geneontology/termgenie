package org.bbop.termgenie.services.review;

import org.bbop.termgenie.ontology.OntologyCommitReviewPipelineStages;
import org.bbop.termgenie.ontology.OntologyLoader;
import org.bbop.termgenie.permissions.UserPermissions;
import org.bbop.termgenie.services.InternalSessionHandler;
import org.bbop.termgenie.services.review.JsonCommitReviewEntry.JsonDiff;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OboTermCommitReviewServiceImpl extends TermCommitReviewServiceImpl {

	@Inject
	OboTermCommitReviewServiceImpl(InternalSessionHandler sessionHandler,
			UserPermissions permissions,
			OntologyLoader loader,
			OntologyCommitReviewPipelineStages stages)
	{
		super(sessionHandler, permissions, loader, stages);
	}

	/**
	 * GO specific updates of terms after marking them as obsolete
	 * 
	 * @param jsonDiff
	 * @param frame
	 */
	@Override
	protected void handleObsoleteOboFrame(JsonDiff jsonDiff, Frame frame) {
		super.handleObsoleteOboFrame(jsonDiff, frame);
		
		// Prefix Definition with "OBSOLETE. "
		Clause defClause = frame.getClause(OboFormatTag.TAG_DEF);
		if (defClause != null) {
			String value = defClause.getValue(String.class);
			if (value != null) {
				if (!value.startsWith("OBSOLETE.")) {
					value = "OBSOLETE. " + value;
				}
			}
			else {
				value = "OBSOLETE.";
			}
			defClause.setValue(value);
		}
		else {
			frame.addClause(new Clause(OboFormatTag.TAG_DEF, "OBSOLETE."));
		}
		
		// prefix obsolete term name with 'obsolete '
		Clause lblFrame = frame.getClause(OboFormatTag.TAG_NAME);
		if (lblFrame != null) {
			String lbl = lblFrame.getValue(String.class);
			if (lbl != null) {
				if (lbl.startsWith("obsolete") == false) {
					lbl = "obsolete " + lbl;
					lblFrame.setValue(lbl);
				}
			}
		}
		
		// add comment about obsolete
		Clause commentClause = frame.getClause(OboFormatTag.TAG_COMMENT);
		String comment = jsonDiff.getObsoleteComment();
		if (commentClause == null && comment != null && !comment.isEmpty()) {
			// only add the comment if there wasn't any before and its non-empty
			commentClause = new Clause(OboFormatTag.TAG_COMMENT, comment);
			frame.addClause(commentClause);
		}
	}

}
