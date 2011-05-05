package org.bbop.termgenie.client;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class OntologySelectionPanel extends FlowPanel{
	
	private final ListBox ontologySelectionBox;
	private final Button selectOntologyButton;
	private final List<String> ontologies;

	public OntologySelectionPanel(List<String> ontologies) {
		super();
		this.ontologies = ontologies;
		setWidth("100%");
		
		// create internal widgets
		ontologySelectionBox = new ListBox();
		Label lblSelectOntology = new InlineLabel("Select Ontology");
		selectOntologyButton = new Button("Select");
		
		// configure internal widgets
		lblSelectOntology.setWordWrap(false);
		lblSelectOntology.setWidth("40px");
		
		for (String ontology : ontologies) {
			ontologySelectionBox.addItem(ontology);
		}
		
		// add internal widgets
		add(lblSelectOntology);
		add(ontologySelectionBox);
		add(selectOntologyButton);
	}
	
	public String getSelectedOntology() {
		int selectedIndex = ontologySelectionBox.getSelectedIndex();
		return ontologies.get(selectedIndex);
	}
	
	public void addSelectListener(ClickHandler handler) {
		selectOntologyButton.addClickHandler(handler);
	}
}
