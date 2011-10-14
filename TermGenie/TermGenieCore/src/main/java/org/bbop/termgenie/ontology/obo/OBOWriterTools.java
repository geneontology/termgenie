package org.bbop.termgenie.ontology.obo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.writer.OBOFormatWriter;


public class OBOWriterTools {

	private static final OBOFormatWriter oboWriter = new OBOFormatWriter();
	
	public static String writeTerms(Collection<String> ids, OBODoc oboDoc) throws IOException {
		StringWriter stringWriter = new StringWriter();
		BufferedWriter writer = new BufferedWriter(stringWriter);
		for (String id : ids) {
			Frame termFrame = oboDoc.getTermFrame(id);
			oboWriter.write(termFrame, writer, oboDoc);
			writer.append('\n');
		}
		writer.close();
		return stringWriter.getBuffer().toString();
	}
	
	public static String writeTerm(String id, OBODoc oboDoc) throws IOException {
		return writeTerms(Collections.singleton(id), oboDoc);
	}
}
