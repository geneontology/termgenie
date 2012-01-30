package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
	
	private OWLOntologyIRIMapper catalogXMLMapper;
	private File catalogFile = null;
	private URL catalogURL = null;

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
			URL url = fallBackIRIMapper.mapUrl(catalogXml);
			if (url.getProtocol() == null || "file".equals(url.getProtocol())) {
				File file = new File(url.toURI());
				catalogFile = file;
				catalogURL = null;
				return new owltools.io.CatalogXmlIRIMapper(catalogFile);
			}
			catalogFile = null;
			catalogURL = url;
			return new owltools.io.CatalogXmlIRIMapper(catalogURL);
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private synchronized void updateCatalogMapper()
	{
		// TODO make this more efficient?
		// Goal, only reload if file/URL (content) was changed. URLs do not have a 
		// reliable last changed date. MD5?
		catalogXMLMapper = createCatalogMapper();
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		updateCatalogMapper();
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
			updateCatalogMapper();
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
