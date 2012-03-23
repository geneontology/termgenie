package org.bbop.termgenie.ontology.obo;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;


public class OwlStringToolsTest {

	@Test
	public void test() {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		OWLDataFactory factory = OwlStringTools.translationManager.getOWLDataFactory();
		OWLEntity owlEntity = factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/GO_0000001"));
		axioms.add(factory.getOWLDeclarationAxiom(owlEntity));
		
		String string = OwlStringTools.translateAxiomsToString(axioms);
		Set<OWLAxiom> axioms2 = OwlStringTools.translateStringToAxioms(string);
		assertEquals(axioms, axioms2);
	}

}
