package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.impl.AbstractScmIRIMapper.FileAwareReadOnlyScm;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import owltools.io.CatalogXmlIRIMapper;

public abstract class AbstractScmIRIMapper<T extends FileAwareReadOnlyScm> implements
	OWLOntologyIRIMapper
{
	// generated
	private static final long serialVersionUID = 2768324941427474031L;

	private static final Logger logger = Logger.getLogger(AbstractScmIRIMapper.class);
	
	private final T scm;
	private final String catalogXml;
	private final Map<IRI, String> mappedFiles;
	private final Set<IRI> updateTriggers;

	/**
	 * @param scm
	 * @param mappedFiles
	 * @param updateTriggers
	 * @param catalogXml optional catalog XML file, null if not defined
	 */
	protected AbstractScmIRIMapper(T scm,
			Map<IRI, String> mappedFiles,
			Set<IRI> updateTriggers,
			String catalogXml)
	{
		this.scm = scm;
		this.mappedFiles = mappedFiles;
		this.updateTriggers = updateTriggers;
		this.catalogXml = catalogXml;
		if (catalogXml != null) {
			try {
				File catalogXmlFile = scm.retrieveFile(catalogXml);
				new owltools.io.CatalogXmlIRIMapper(catalogXmlFile);
			} catch (IOException exception) {
				logger.error("Could not create catalog-xml mapper: "+catalogXml, exception);
				throw new RuntimeException(exception);
			}
		}
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		synchronized (scm) {
			try {
				if (updateTriggers.contains(ontologyIRI)) {
					scm.updateSCM();
				}
				IRI iri = null;
				if (mappedFiles.containsKey(ontologyIRI)) {
					String scmFile = mappedFiles.get(ontologyIRI);
					iri = IRI.create(scm.retrieveFile(scmFile).getAbsoluteFile());
				}
				else if(catalogXml != null) {
					File catalogXmlFile = scm.retrieveFile(catalogXml);
					CatalogXmlIRIMapper catalogXMLMapper = new owltools.io.CatalogXmlIRIMapper(catalogXmlFile);
					iri = catalogXMLMapper.getDocumentIRI(ontologyIRI);
				}
				return iri;
			} catch (IOException exception) {
				logger.error("Could not map an IRI to a local file: "+ontologyIRI, exception);
			}
		}
		return null;
	}

	protected static interface FileAwareReadOnlyScm {

		public File retrieveFile(String file) throws IOException;

		public void updateSCM() throws IOException;

	}
}
