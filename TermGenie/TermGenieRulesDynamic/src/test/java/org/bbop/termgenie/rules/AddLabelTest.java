package org.bbop.termgenie.rules;

import static org.junit.Assert.*;

import org.bbop.termgenie.ontology.obo.OboTools;
import org.bbop.termgenie.rules.AbstractTermCreationTools.OWLChangeTracker;
import org.junit.Test;
import org.obolibrary.obo2owl.Obo2OWLConstants;
import org.obolibrary.obo2owl.Obo2Owl;
import org.obolibrary.obo2owl.Owl2Obo;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;


public class AddLabelTest {

	@Test
	public void test() throws Exception {
		OBODoc obodoc = new OBODoc();
		obodoc.setHeaderFrame(new Frame(FrameType.HEADER));
		Frame frame = OboTools.createTermFrame("GO:TEMP-LL-01");
		obodoc.addFrame(frame);
		
		Obo2Owl obo2Owl = new Obo2Owl();
		OWLOntology owlOntology = obo2Owl.convert(obodoc);
		
		OWLChangeTracker changeTracker = new OWLChangeTracker(owlOntology);
		IRI newOwlClass = obo2Owl.oboIdToIRI(frame.getId());
		TermCreationToolsMDef.addLabel(newOwlClass, "Test-Label", changeTracker);
		
		IRI iri = IRI.create(Obo2OWLConstants.DEFAULT_IRI_PREFIX+"GO_TEMP-foo-02");
		TermCreationToolsMDef.addClass(iri, changeTracker);
		TermCreationToolsMDef.addLabel(iri, "Test-Label2", changeTracker);
		
		
		Owl2Obo owl2Obo = new Owl2Obo();
		OBODoc oboDoc2 = owl2Obo.convert(owlOntology);
		Frame frame2 = oboDoc2.getTermFrame(frame.getId());
		assertEquals("Test-Label", frame2.getTagValue(OboFormatTag.TAG_NAME));
		
		
		Frame frame3 = oboDoc2.getTermFrame(Owl2Obo.getIdentifier(iri));
		assertEquals("Test-Label2", frame3.getTagValue(OboFormatTag.TAG_NAME));
	}

}
