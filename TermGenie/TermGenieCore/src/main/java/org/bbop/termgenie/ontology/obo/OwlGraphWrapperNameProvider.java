package org.bbop.termgenie.ontology.obo;

import java.util.Set;

import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import owltools.graph.OWLGraphWrapper;


public class OwlGraphWrapperNameProvider implements NameProvider {

	private final OWLGraphWrapper wrapper;
	private final String defaultOboNamespace;
	
	/**
	 * @param wrapper
	 */
	public OwlGraphWrapperNameProvider(OWLGraphWrapper wrapper) {
		super();
		this.wrapper = wrapper;
		final OWLAnnotationProperty defaultNamespaceProperty = wrapper.getAnnotationProperty(OboFormatTag.TAG_DEFAULT_NAMESPACE.getTag());
		OWLOntology ontology = wrapper.getSourceOntology();
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
		defaultOboNamespace = namespace;
	}

	@Override
	public String getName(String id) {
		String name = null;
		OWLObject owlObject = wrapper.getOWLObjectByIdentifier(id);
		if (owlObject != null) {
			name = wrapper.getLabel(owlObject);
		}
		return name;
	}

	@Override
	public String getDefaultOboNamespace() {
		return defaultOboNamespace;
	}

}
