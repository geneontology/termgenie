package org.bbop.termgenie.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

class ManchesterSyntaxTool {

	private final OWLDataFactory dataFactory;
	private OWLEntityChecker entityChecker;
	private SimpleShortFormProvider shortFormProvider;

	ManchesterSyntaxTool(OWLOntology inputOntology, Collection<OWLOntology> auxiliaryOntologies) {
		OWLOntologyManager manager = inputOntology.getOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		shortFormProvider = new SimpleShortFormProvider();
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(inputOntology);
		if (auxiliaryOntologies != null && !auxiliaryOntologies.isEmpty()) {
			ontologies.addAll(auxiliaryOntologies);
		}
		entityChecker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(manager, ontologies, shortFormProvider));
	}
	
	String mapOwlObject(OWLEntity entity) {
		return shortFormProvider.getShortForm(entity);
	}

	OWLClassExpression parseManchesterExpression(String expression) throws ParserException {

		ManchesterOWLSyntaxEditorParser parser = createParser(expression);
		OWLClassExpression ce = parser.parseClassExpression();
		return ce;
	}

	private ManchesterOWLSyntaxEditorParser createParser(String expression) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, expression);
		parser.setOWLEntityChecker(entityChecker);
		return parser;
	}
}