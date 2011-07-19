package org.bbop.termgenie.core.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.TermTemplate;

public interface TermTemplateIO {

	public void writeTemplates(Collection<TermTemplate> templates, BufferedWriter writer)
			throws IOException;

	public void writeTemplates(Collection<TermTemplate> templates, File outputFile);

	public List<TermTemplate> readTemplates(BufferedReader reader) throws IOException;

}