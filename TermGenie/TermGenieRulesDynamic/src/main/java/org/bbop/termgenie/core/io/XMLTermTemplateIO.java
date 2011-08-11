package org.bbop.termgenie.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.bbop.termgenie.core.TermTemplate;
import org.bbop.termgenie.ontology.OntologyConfiguration;

import com.google.inject.Inject;

public class XMLTermTemplateIO implements TermTemplateIO {

	private final XMLTermTemplateIOReader reader;
	private final XMLTermTemplateIOWriter writer;

	@Inject
	XMLTermTemplateIO(OntologyConfiguration ontologyConfiguration) {
		super();
		reader = new XMLTermTemplateIOReader(ontologyConfiguration);
		writer = new XMLTermTemplateIOWriter();
	}
	
	@Override
	public void writeTemplates(Collection<TermTemplate> templates, OutputStream outputStream) throws IOException {
		writer.writeTemplates(templates, outputStream);
	}

	@Override
	public List<TermTemplate> readTemplates(InputStream inputStream) throws IOException {
		return reader.readTemplates(inputStream);
	}

}
