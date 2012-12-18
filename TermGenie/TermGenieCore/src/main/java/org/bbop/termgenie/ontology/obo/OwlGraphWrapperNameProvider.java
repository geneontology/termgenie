package org.bbop.termgenie.ontology.obo;

import java.util.Set;

import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.graph.OWLGraphWrapper;


public class OwlGraphWrapperNameProvider extends owltools.io.ParserWrapper.OWLGraphWrapperNameProvider {

	/**
	 * @param wrapper
	 */
	public OwlGraphWrapperNameProvider(OWLGraphWrapper wrapper) {
		super(wrapper, extractDefaultNamespace(wrapper));
	}

	static String extractDefaultNamespace(OWLGraphWrapper graph) {
		final OWLAnnotationProperty defaultNamespaceProperty = graph.getAnnotationProperty(OboFormatTag.TAG_DEFAULT_NAMESPACE.getTag());
		OWLOntology ontology = graph.getSourceOntology();
		String namespace = null;
		Set<OWLAnnotation> annotations = ontology.getAnnotations();
		for (OWLAnnotation annotation : annotations) {
			OWLAnnotationProperty property = annotation.getProperty();
			if (defaultNamespaceProperty.getIRI().equals(property.getIRI())) {
				OWLAnnotationValue value = annotation.getValue();
				if (value instanceof OWLLiteral) {
					namespace = ((OWLLiteral) value).getLiteral();
				}
			}
		}
		return namespace;
	}
	
}
