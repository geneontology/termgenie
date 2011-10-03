package org.bbop.termgenie.ontology.obo;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class ComitAwareOBOConverterTools extends OBOConverterTools {

	private static final Logger logger = Logger.getLogger(ComitAwareOBOConverterTools.class);

	public static void handleRelation(IRelation relation, Modification mode, OBODoc obodoc) {
		// TODO Auto-generated method stub
	}

	public static boolean handleTerm(OntologyTerm<? extends ISynonym, ? extends IRelation> term,
			Modification mode,
			OBODoc obodoc)
	{
		String id = term.getId();
		Frame frame = obodoc.getTermFrame(id);
		switch (mode) {
			case add:
				if (frame != null) {
					logger.warn("Skipping already existing term from history: " + id);
					return false;
				}
				frame = new Frame(FrameType.TERM);
				fillOBO(frame, term);
				try {
					obodoc.addFrame(frame);
				} catch (FrameMergeException exception) {
					logger.error("Could not add new term to ontology: " + id, exception);
					return false;
				}
				break;
			case modify:
				if (frame == null) {
					logger.warn("Skipping modification of non-existing term from history: " + id);
					return false;
				}
				try {
					Frame modFrame = new Frame(FrameType.TERM);
					fillOBO(frame, term);
					frame.merge(modFrame);
				} catch (FrameMergeException exception) {
					logger.warn("Could not apply chages to frame.", exception);
					return false;
				}
				break;

			case remove:
				if (frame == null) {
					logger.warn("Skipping removal of non-existing term from history: " + id);
					return false;
				}
				Collection<Frame> frames = obodoc.getTermFrames();
				frames.remove(frame);
				break;

			default:
				// do nothing
				break;
		}
		return false;
	}

}
