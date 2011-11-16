package org.bbop.termgenie.ontology.obo;

import org.obolibrary.oboformat.writer.OBOFormatWriter.NameProvider;
import org.semanticweb.owlapi.model.OWLObject;

import owltools.graph.OWLGraphWrapper;


public class OwlGraphWrapperNameProvider implements NameProvider {

	private final OWLGraphWrapper wrapper;
	
	/**
	 * @param wrapper
	 */
	public OwlGraphWrapperNameProvider(OWLGraphWrapper wrapper) {
		super();
		this.wrapper = wrapper;
	}

	@Override
	public String getName(String id) {
		String name = null;
		OWLObject owlObject = wrapper.getOWLObjectByIdentifier(id);
		if (owlObject != null) {
			name = wrapper.getLabel(owlObject);
		}
		return name ;
	}

}
