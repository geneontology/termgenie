package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.IRIMapper;
import org.semanticweb.owlapi.model.IRI;


public abstract class AbstractScmAwareIRIMapper<T extends AbstractScmAwareIRIMapper.ReadOnlyScm> implements IRIMapper {
	
	private final IRIMapper fallBackIRIMapper;
	private final T scm;
	
	protected AbstractScmAwareIRIMapper(IRIMapper fallBackIRIMapper, T scm) {
		this.fallBackIRIMapper = fallBackIRIMapper;
		this.scm = scm;
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		URL url = mapUrl(ontologyIRI.toString());
		try {
			return IRI.create(url);
		} catch (URISyntaxException exception) {
			Logger.getLogger(getClass()).warn("Could not create IRI from URL: "+url, exception);
			throw new RuntimeException(exception);
		}
	}

	@Override
	public URL mapUrl(String url) {
		URL mapped;
		if (isScmMapped(url, scm)) {
			mapped = mapUrlUsingScm(url);
		}
		else {
			mapped = fallBackIRIMapper.mapUrl(url);
		}
		return mapped;
	}

	protected abstract boolean isScmMapped(String url, T scm);
	
	protected URL mapUrlUsingScm(String url) {
		synchronized (scm) {
			try {
				scm.update(url);
				return scm.getFile(url).toURI().toURL();
			} catch (MalformedURLException exception) {
				throw new RuntimeException(exception);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
	}
	
	protected static interface ReadOnlyScm {
		
		public void update(String url) throws IOException;
		
		public File getFile(String url) throws IOException;
	}
}
