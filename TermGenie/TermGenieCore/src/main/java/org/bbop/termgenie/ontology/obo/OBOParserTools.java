package org.bbop.termgenie.ontology.obo;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;


public class OBOParserTools {
	
	public static Clause parseClause(String clause) {
		Clause result = new Clause();
		OBOFormatParser p = new OBOFormatParser();
		BufferedReader reader = new BufferedReader(new StringReader(clause));
		p.setReader(reader);
		p.parseTermFrameClause(result);
		IOUtils.closeQuietly(reader);
		return result;
	}
	
	public static void parseClauses(Frame frame, List<String> clauses) {
		if (clauses != null && !clauses.isEmpty()) {
			for (String clause : clauses) {
				frame.addClause(parseClause(clause));
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
