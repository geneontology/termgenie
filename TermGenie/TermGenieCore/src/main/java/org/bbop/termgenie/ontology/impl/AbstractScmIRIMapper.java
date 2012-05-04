package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.bbop.termgenie.ontology.IRIMapper;
import org.semanticweb.owlapi.model.IRI;

import owltools.io.CatalogXmlIRIMapper;

public abstract class AbstractScmIRIMapper<T extends AbstractScmIRIMapper.FileAwareReadOnlyScm> implements
		IRIMapper
{

	private final IRIMapper fallBackIRIMapper;
	private final T scm;
	private final String catalogXml;
	private final Map<IRI, String> mappedFiles;

	/**
	 * @param fallBackIRIMapper
	 * @param scm
	 * @param catalogXml optional catalog XML file, null if not defined
	 */
	protected AbstractScmIRIMapper(IRIMapper fallBackIRIMapper,
			T scm,
			Map<IRI, String> mappedFiles,
			String catalogXml)
	{
		this.fallBackIRIMapper = fallBackIRIMapper;
		this.scm = scm;
		this.mappedFiles = mappedFiles;
		this.catalogXml = catalogXml;
		if (catalogXml != null) {
			try {
				File catalogXmlFile = scm.retrieveFile(catalogXml);
				new owltools.io.CatalogXmlIRIMapper(catalogXmlFile);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		synchronized (scm) {
			try {
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
				if (iri != null) {
					final URI uri = iri.toURI();
					final String scheme = uri.getScheme();
					if (scheme == null || "file".equals(scheme)) {
						File file = new File(uri);
						scm.updateFile(file);
					}
					return iri;
				}
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
		return fallBackIRIMapper.getDocumentIRI(ontologyIRI);
	}

	@Override
	public URL mapUrl(String url) {
		try {
			return getDocumentIRI(IRI.create(url)).toURI().toURL();
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	protected static interface FileAwareReadOnlyScm {

		public File retrieveFile(String file) throws IOException;

		public void updateFile(File file) throws IOException;

	}
}
