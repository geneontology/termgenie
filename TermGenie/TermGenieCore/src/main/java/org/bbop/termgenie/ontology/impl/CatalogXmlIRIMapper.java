package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.bbop.termgenie.ontology.IRIMapper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CatalogXmlIRIMapper implements IRIMapper {

	
	private final IRIMapper fallBackIRIMapper;
	private final String catalogXml;
	
	private final OWLOntologyIRIMapper catalogXMLMapper;

	@Inject
	public CatalogXmlIRIMapper(IRIMapper fallBackIRIMapper, String catalogXml) {
		if (fallBackIRIMapper == null) {
			throw new IllegalArgumentException("IRIMapper may never be null");
		}
		this.fallBackIRIMapper = fallBackIRIMapper;
		this.catalogXml = catalogXml;
		catalogXMLMapper = createCatalogMapper();
	}

	private OWLOntologyIRIMapper createCatalogMapper() throws RuntimeException
	{
		try {
			File file = new File(catalogXml);
			return new owltools.io.CatalogXmlIRIMapper(file);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		IRI iri = catalogXMLMapper.getDocumentIRI(ontologyIRI);
		if (iri != null) {
			return iri;
		}
		iri = fallBackIRIMapper.getDocumentIRI(ontologyIRI);
		return iri;
	}

	@Override
	public URL mapUrl(String url) {
		try {
			IRI iri = catalogXMLMapper.getDocumentIRI(IRI.create(url));
			if (iri != null) {
				return iri.toURI().toURL();
			}
			URL result = fallBackIRIMapper.mapUrl(url);
			return result;
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}

}
