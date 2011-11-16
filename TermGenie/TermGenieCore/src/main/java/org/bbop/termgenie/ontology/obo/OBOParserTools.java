package org.bbop.termgenie.ontology.obo;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.parser.OBOFormatParser;


public class OBOParserTools {
	
	/**
	 * Parse a single line as term clause. Ignores id clause lines
	 * 
	 * @param clause
	 * @return {@link Clause} or null
	 */
	public static Clause parseClause(String clause) {
		if (clause == null || clause.startsWith(OboFormatTag.TAG_ID.getTag()+": ")) {
			return null;
		}
		Clause result = new Clause();
		OBOFormatParser p = new OBOFormatParser();
		BufferedReader reader = new BufferedReader(new StringReader(clause));
		p.setReader(reader);
		p.parseTermFrameClause(result);
		IOUtils.closeQuietly(reader);
		if (result.getValue() == null) {
			return result;
		}
		return result;
	}
	
	/**
	 * Parse a list of single lines as term clause. Ignores id clause lines. 
	 * Adds the clauses to the frame.
	 * 
	 * @param frame
	 * @param clauses
	 */
	public static void parseClauses(Frame frame, List<String> clauses) {
		if (clauses != null && !clauses.isEmpty()) {
			for (String clause : clauses) {
				Clause parsedClause = parseClause(clause);
				if (parsedClause != null) {
					frame.addClause(parsedClause);
				}
			}
		}
	}
	
	public static Frame parseFrame(String id, String obo) {
		OBOFormatParser p = new OBOFormatParser();
		BufferedReader reader = new BufferedReader(new StringReader(obo));
		p.setReader(reader);
		OBODoc obodoc = new OBODoc();
		p.parseTermFrame(obodoc);
		Frame termFrame = obodoc.getTermFrame(id);
		IOUtils.closeQuietly(reader);
		return termFrame;
	}
}
