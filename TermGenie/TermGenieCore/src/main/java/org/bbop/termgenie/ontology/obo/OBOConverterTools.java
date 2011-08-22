package org.bbop.termgenie.ontology.obo;

import java.util.List;
import java.util.Map;

import org.bbop.termgenie.core.Ontology.IRelation;
import org.bbop.termgenie.core.Ontology.Relation;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;

public class OBOConverterTools {

	public static void fillRelations(Frame frame, List<? extends IRelation> relations, String id) {
		if (relations != null && !relations.isEmpty()) {
			for (IRelation relation : relations) {
				fillRelation(frame, relation, id);
			}
		}
	}

	public static void fillRelation(Frame frame, IRelation relation, String id) {
		if (id == null || id.equals(relation.getSource())) {
			String target = relation.getTarget();
			Map<String, String> properties = relation.getProperties();
			if (properties != null && !properties.isEmpty()) {
				String type = Relation.getType(properties);
				Clause cl;
				if (OboFormatTag.TAG_IS_A.getTag().equals(type)) {
					cl = new Clause(type, target);
				}
				else if (OboFormatTag.TAG_INTERSECTION_OF.getTag().equals(type)) {
					cl = new Clause(type);
					String relationShip = Relation.getRelationShip(properties);
					if (relationShip != null) {
						cl.addValue(relationShip);
					}
					cl.addValue(target);
				}
				else if (OboFormatTag.TAG_UNION_OF.getTag().equals(type)) {
					cl = new Clause(type, target);
				}
				else if (OboFormatTag.TAG_DISJOINT_FROM.getTag().equals(type)) {
					cl = new Clause(type, target);
				}
				else {
					cl = new Clause(OboFormatTag.TAG_RELATIONSHIP.getTag());
					cl.addValue(type);
					cl.addValue(target);
				}
				cl.addValue("! "+relation.getTargetLabel());
				frame.addClause(cl);
			}
		}
	}
}
