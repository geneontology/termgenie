package org.bbop.termgenie.client;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class OntologySelectionPanel extends FlowPanel{
	
	public OntologySelectionPanel(List<String> ontologies) {
		super();
		setWidth("100%");
		Label lblSelectOntology = new InlineLabel("Select Ontology");
		lblSelectOntology.setWordWrap(false);
		add(lblSelectOntology);
		lblSelectOntology.setWidth("40px");
		ListBox ontologySelectionBox = new ListBox();
		for (String ontology : ontologies) {
			ontologySelectionBox.addItem(ontology);
		}
		add(ontologySelectionBox);
	}
}
