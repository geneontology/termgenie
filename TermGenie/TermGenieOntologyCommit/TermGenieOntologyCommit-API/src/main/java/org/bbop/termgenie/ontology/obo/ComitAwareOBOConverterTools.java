package org.bbop.termgenie.ontology.obo;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.OntologyTerm;
import org.bbop.termgenie.core.Ontology.Relation;
import org.bbop.termgenie.ontology.CommitObject.Modification;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

import owltools.graph.OWLGraphWrapper.ISynonym;

public class ComitAwareOBOConverterTools extends OBOConverterTools {

	private static final Logger logger = Logger.getLogger(ComitAwareOBOConverterTools.class);

	public static void handleRelation(IRelation relation, Modification mode, OBODoc obodoc) {
		String source = relation.getSource();
		Frame frame = obodoc.getTermFrame(source);
		switch (mode) {
			case add:
				if (frame == null) {
					logger.warn("Cannot add a relation for an unknown term: " + source);
				}
				else {
					fillRelation(frame, relation, source);
				}
				break;
			case modify:
				if (frame == null) {
					logger.error("Cannot modify a relation for an unknown term: " + source);
				} else {
					removeRelation(frame, relation);
					fillRelation(frame, relation, source);
				}
				break;
			case remove:
				if (frame == null) {
					logger.info("Skip removal of a relation for an unknown term: " + source);
				} else {
					removeRelation(frame, relation);
				}
				break;
			default:
				break;
		}
	}
	
	static void removeRelation(Frame frame, IRelation relation) {
		String type = Relation.getType(relation.getProperties());
		Collection<Clause> clauses = frame.getClauses();
		Iterator<Clause> iterator = clauses.iterator();
		while (iterator.hasNext()) {
			Clause clause = iterator.next();
			if (clause.getTag().equals(type)) {
				boolean remove = false;
				String value = relation.getTarget();
				String value2 = null;
				if (OboFormatTag.TAG_RELATIONSHIP.getTag().equals(type)) {
					value = Relation.getRelationShip(relation.getProperties());
					value2 = relation.getTarget();
				}
				else if (OboFormatTag.TAG_INTERSECTION_OF.getTag().equals(type)) {
					String relationShip = Relation.getRelationShip(relation.getProperties());
					if (relationShip != null) {
						value = relationShip;
						value2 = relation.getTarget();
					}
				}
				if (value.equals(clause.getValue())) {
					Collection<Object> values = clause.getValues();
					if (value2 == null) {
						remove = values.size() == 1;
					}
					else {
						if (values.size() > 1) {
							remove = value2.equals(clause.getValue2());
						}
					}
				}
				
				if (remove) {
					iterator.remove();
					return;
				}
			}
		}
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
