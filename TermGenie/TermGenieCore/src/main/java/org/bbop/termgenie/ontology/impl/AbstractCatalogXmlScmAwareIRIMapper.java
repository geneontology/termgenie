package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.bbop.termgenie.ontology.IRIMapper;
import org.semanticweb.owlapi.model.IRI;

import owltools.io.CatalogXmlIRIMapper;

public abstract class AbstractCatalogXmlScmAwareIRIMapper<T extends AbstractCatalogXmlScmAwareIRIMapper.FileAwareReadOnlyScm> implements
		IRIMapper
{

	private final IRIMapper fallBackIRIMapper;
	private final T scm;
	private final String catalogXml;

	protected AbstractCatalogXmlScmAwareIRIMapper(IRIMapper fallBackIRIMapper,
			T scm,
			String catalogXml)
	{
		this.fallBackIRIMapper = fallBackIRIMapper;
		this.scm = scm;
		this.catalogXml = catalogXml;
		try {
			File catalogXmlFile = scm.retrieveFile(catalogXml);
			new owltools.io.CatalogXmlIRIMapper(catalogXmlFile);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		IRI iri;
		synchronized (scm) {
			try {
				File catalogXmlFile = scm.retrieveFile(catalogXml);
				CatalogXmlIRIMapper catalogXMLMapper = new owltools.io.CatalogXmlIRIMapper(catalogXmlFile);
				iri = catalogXMLMapper.getDocumentIRI(ontologyIRI);
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
