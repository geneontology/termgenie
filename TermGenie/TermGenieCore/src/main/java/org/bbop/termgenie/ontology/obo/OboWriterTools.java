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
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.obolibrary.oboformat.writer.OBOFormatWriter.OBODocNameProvider;


public class OboWriterTools {

	private static final OBOFormatWriter oboWriter = new OBOFormatWriter();
	
	public static String writeTerms(Collection<String> ids, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		NameProvider nameProvider = new OBODocNameProvider(oboDoc);
		for (String id : ids) {
			Frame termFrame = oboDoc.getTermFrame(id);
			oboWriter.write(termFrame, writer, nameProvider);
		}
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static String writeTerm(String id, OBODoc oboDoc) throws IOException {
		return writeTerms(Collections.singleton(id), oboDoc);
	}
	
	public static String writeFrame(Frame frame, NameProvider nameProvider) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		oboWriter.write(frame, writer, nameProvider);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static String writeRelations(String id, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		
		final Frame termFrame = oboDoc.getTermFrame(id);
		NameProvider nameProvider = new OBODocNameProvider(oboDoc);
		writeTags(writer, termFrame, nameProvider, OboFormatTag.TAG_IS_A, OboFormatTag.TAG_INTERSECTION_OF, OboFormatTag.TAG_UNION_OF, OboFormatTag.TAG_DISJOINT_FROM, OboFormatTag.TAG_RELATED);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static void writeTags(BufferedWriter writer, Frame frame, NameProvider nameProvider, OboFormatTag...tags) throws IOException {
		
		for (OboFormatTag tag : tags) {
			Collection<Clause> clauses = frame.getClauses(tag);
			if (clauses != null && !clauses.isEmpty()) {
				for (Clause clause : clauses) {
					oboWriter.write(clause, writer, nameProvider);						
				}
			}	
		}
	}
	
	public static String writeClause(Clause clause, NameProvider nameProvider) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		oboWriter.write(clause, writer, nameProvider);
		writer.close();
		return stringWriter.getBuffer().toString();
	}
}
