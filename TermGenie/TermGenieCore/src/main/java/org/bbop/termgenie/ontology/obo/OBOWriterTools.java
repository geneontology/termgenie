package org.bbop.termgenie.ontology.obo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter;


public class OBOWriterTools {

	private static final OBOFormatWriter oboWriter = new OBOFormatWriter();
	
	public static String writeTerms(Collection<String> ids, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		for (String id : ids) {
			Frame termFrame = oboDoc.getTermFrame(id);
			oboWriter.write(termFrame, writer, oboDoc);
		}
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static String writeTerm(String id, OBODoc oboDoc) throws IOException {
		return writeTerms(Collections.singleton(id), oboDoc);
	}
	
	public static String writeFrame(Frame frame) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		oboWriter.write(frame, writer, null);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static String writeRelations(String id, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		
		final Frame termFrame = oboDoc.getTermFrame(id);
		writeTags(writer, termFrame, oboDoc, OboFormatTag.TAG_IS_A, OboFormatTag.TAG_INTERSECTION_OF, OboFormatTag.TAG_UNION_OF, OboFormatTag.TAG_DISJOINT_FROM, OboFormatTag.TAG_RELATED);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static void writeTags(BufferedWriter writer, Frame frame, OBODoc oboDoc, OboFormatTag...tags) throws IOException {
		
		for (OboFormatTag tag : tags) {
			Collection<Clause> clauses = frame.getClauses(tag);
			if (clauses != null && !clauses.isEmpty()) {
				for (Clause clause : clauses) {
					oboWriter.write(clause, writer, oboDoc);						
				}
			}	
		}
	}
	
	public static String writeClause(Clause clause, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		oboWriter.write(clause, writer, oboDoc);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
}
