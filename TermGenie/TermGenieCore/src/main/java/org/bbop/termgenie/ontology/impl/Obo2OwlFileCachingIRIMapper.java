package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bbop.termgenie.ontology.IRIMapper;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * {@link IRIMapper}, which uses a file cache for URLs with the additional
 * option to load OWL ontologies from an OBO URL. This is useful for
 * OWL-imports, where the OWL is derived from an OBO source.
 */
@Singleton
public class Obo2OwlFileCachingIRIMapper extends FileCachingIRIMapper {

	private final Map<String, String> oboSources;

	@Inject
	Obo2OwlFileCachingIRIMapper(@Named("FileCachingIRIMapperLocalCache") String localCache,
			@Named("FileCachingIRIMapperPeriod") long period,
			@Named("FileCachingIRIMapperTimeUnit") TimeUnit unit,
			@Named("Obo2OwlFileCachingIRIMapperOBOSources") Map<String, String> oboSources)
	{
		super(localCache, period, unit);
		if (oboSources == null) {
			oboSources = Collections.emptyMap();
		}
		this.oboSources = oboSources;
	}

	@Override
	public URL mapUrl(String url) {
		String oboSource = oboSources.get(url);
		if (oboSource != null) {
			URL oboURL = super.mapUrl(oboSource);
			return getOWLFileForOBO(oboURL);
		}
		return super.mapUrl(url);
	}

	private URL getOWLFileForOBO(URL oboURL) {
		try {
			// check for OWL cacheFile
			File oboFile = new File(oboURL.getFile());
			File cacheFile = localCacheFile(oboFile);
			if (!isValid(cacheFile)) {
				// load and convert from OBO to OWL
				OBOFormatParser p = new OBOFormatParser();
				OBODoc oboDoc = p.parse(oboFile);
				Obo2Owl obo2Owl = new Obo2Owl();
				OWLOntology owlOntology = obo2Owl.convert(oboDoc);

				// write to cacheFile
				OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
				manager.saveOntology(owlOntology, IRI.create(cacheFile));

				// set cache File valid
				setValid(cacheFile);
			}
			return cacheFile.toURI().toURL();
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyCreationException exception) {
			throw new RuntimeException(exception);
		} catch (OWLOntologyStorageException exception) {
			throw new RuntimeException(exception);
		}
	}

}
