package org.bbop.termgenie.client.helper;

import java.util.ArrayList;

import org.bbop.termgenie.client.helper.DataInputField.AutoCompleteInputField;
import org.bbop.termgenie.client.helper.DataInputField.ListAutoCompleteInputField;
import org.bbop.termgenie.client.helper.DataInputField.PrefixAutoCompleteInputField;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for rendering the input fields for a given term template.
 */
public class TermTemplateWidget extends FlowPanel {
	
	private final GWTTermTemplate template;
	private ArrayList<ArrayList<DataInputField>> table = new ArrayList<ArrayList<DataInputField>>();
	private final Label lblRequired = new Label("Required");
	private final Label lblOptional = new Label("Optional");
	private final Button addRowButton = new Button("add");
	private final Grid grid;

	public TermTemplateWidget(GWTTermTemplate template) {
		super();
		this.template = template;
		grid = new Grid(4,template.getFields().length+1);
		
		Label label = new Label(template.getName());
		
		// add to parent
		add(label);
		add(grid);
		
		// configure internal widgets
		grid.setCellSpacing(2);
		grid.setCellPadding(2);
		
		GWTTemplateField[] fields = template.getFields();
		// set labels
		grid.setWidget(0, 1, lblRequired);
		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].isRequired()) {
				grid.setWidget(0, i+1, lblOptional);
			}
			grid.setWidget(1, i+1, new Label(fields[i].getName()));
		}
		
		// add the first data row
		appendRow();
	}

	private void addRow(final int rowPos) {
		PushButton removeButton = new PushButton("remove");
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				synchronized (grid) {
					grid.removeRow(rowPos);
					table.remove(rowPos-2);
					if (table.isEmpty()) {
						TermTemplateWidget.this.setVisible(false);
						// TODO remove it also from the hashmap in the parent panel!
						TermTemplateWidget.this.removeFromParent();
					}
				}
			}
		});
		
		grid.setWidget(rowPos, 0, removeButton);
		
		GWTTemplateField[] fields = template.getFields();
		
		ArrayList<DataInputField> dataFields = new ArrayList<DataInputField>(fields.length);
		table.add(dataFields);
		
		for (int i = 0; i < fields.length; i++) {
			GWTTemplateField field = fields[i];
			String ontology = field.getOntology();
			Widget widget;
			DataInputField dataField;
			if (ontology != null) {
				SuggestOracle oracle = AutoCompleteHelper.getSuggestOracle(ontology);
				GWTCardinality cardinality = field.getCardinality();
				if (cardinality.getMin() == 1 && cardinality.getMax() == 1) {
					String[] functionalPrefixes = field.getFunctionalPrefixes();
					if (functionalPrefixes != null && functionalPrefixes.length > 0) {
						//  simple auto complete with prefixes
						PrefixAutoCompleteInputField instance = new PrefixAutoCompleteInputField(oracle, functionalPrefixes);
						widget = instance;
						dataField = instance;
					}
					else {
						// simple with auto complete
						AutoCompleteInputField instance = new AutoCompleteInputField(oracle);
						widget = instance;
						dataField = instance;
					}
				}
				else {
					// lists
					ListAutoCompleteInputField instance = new ListAutoCompleteInputField(oracle, cardinality);
					widget = instance;
					dataField = instance;
				}
			}
			else {
				// simple no auto complete
				DataInputField.TextFieldInput textBox = new DataInputField.TextFieldInput();
				widget = textBox;
				dataField = textBox;
			}
			grid.setWidget(rowPos, i+1, widget);
			dataFields.add(dataField);
		}
		grid.setWidget(rowPos+1, 0, addRowButton);
	}

	/**
	 * Add an additional row for further template instances of the same type.
	 */
	public void appendRow() {
		synchronized (grid) {
			grid.resizeRows(grid.getRowCount() + 1);
			addRow(table.size());
		}
	}
}