package org.bbop.termgenie.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.TermTemplate;

public interface TermTemplateIO {

	/**
	 * Write templates to a given outputStream.
	 * 
	 * @param templates
	 * @param outputStream
	 */
	public void writeTemplates(Collection<TermTemplate> templates, OutputStream outputStream);

	/**
	 * Parse the templates from a given input stream
	 * 
	 * @param inputStream
	 * @return list of templates or null
	 * @throws IOException
	 */
	public List<TermTemplate> readTemplates(InputStream inputStream) throws IOException;

}