package org.bbop.termgenie.client.helper;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.client.AllTermListPanel;
import org.bbop.termgenie.client.MessagePanel;
import org.bbop.termgenie.client.helper.DataInputField.AutoCompleteInputField;
import org.bbop.termgenie.client.helper.DataInputField.ListAutoCompleteInputField;
import org.bbop.termgenie.client.helper.DataInputField.PrefixAutoCompleteInputField;
import org.bbop.termgenie.services.TermSuggestion;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.GenericSuggestOracle;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

/**
 * Widget for rendering the input fields for a given term template.
 */
public class TermTemplateWidget extends FlowPanel {
	
	private final GWTTermTemplate template;
	private ArrayList<ArrayList<DataInputField>> table = new ArrayList<ArrayList<DataInputField>>();
	private final Label lblRequired = new Label("Required");
	private final Label lblOptional = new Label("Optional");
	private final ModifyButtonsWidget modifyButtons;
	private final Grid grid;

	public TermTemplateWidget(final GWTTermTemplate template, final AllTermListPanel parent) {
		super();
		this.addStyleName("term-template-widget");
		this.template = template;
		// initial size of the Grid: two header rows plus footer row with add button
		grid = new Grid(3,template.getFields().length);
		grid.addStyleName("term-template-grid");
		
		// format footer like the normal data areas, as it will be used latter as such.
		grid.getRowFormatter().setVerticalAlign(2, HasVerticalAlignment.ALIGN_TOP);
		
		modifyButtons = new ModifyButtonsWidget("add template line", "remove template line");
		modifyButtons.addAddHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				appendRow();
			}
		});
		
		modifyButtons.addRemoveHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				deleteRow();
				if (isEmpty()) {
					parent.removeTemplate(template);
				}
			}
		});
		
		// add to parent
		Label templateLabel = new Label("Template: "+template.getName());
		templateLabel.addStyleName("template-header-title");
		add(templateLabel);
		add(grid);
		
		// configure internal widgets
		grid.setCellSpacing(2);
		grid.setCellPadding(2);
		
		GWTTemplateField[] fields = template.getFields();
		
		lblRequired.addStyleName("template-header-category");
		lblOptional.addStyleName("template-header-category");
		// set labels
		grid.setWidget(0, 0, lblRequired);
		boolean first = true;
		for (int i = 0; i < fields.length; i++) {
			GWTTemplateField field = fields[i];
			if (first && !field.isRequired()) {
				grid.setWidget(0, i, lblOptional);
				first = false;
			}
			Label nameLabel = new Label(field.getName());
			nameLabel.addStyleName("template-header-field-name");
			grid.setWidget(1, i, nameLabel);
		}
		
		// add the first data row
		appendRow();
	}

	private void addRow(int rowPos) {
		GWTTemplateField[] fields = template.getFields();
		
		ArrayList<DataInputField> dataFields = new ArrayList<DataInputField>(fields.length);
		table.add(dataFields);
		
		for (int i = 0; i < fields.length; i++) {
			GWTTemplateField field = fields[i];
			DataInputField dataField = createDataInputField(field);
			grid.setWidget(rowPos, i, dataField.getWidget());
			dataFields.add(dataField);
		}
		grid.setWidget(rowPos+1, 0, modifyButtons);
		
		// set the newly generated element into view
		dataFields.get(0).getWidget().getElement().scrollIntoView();
	}

	private DataInputField createDataInputField(GWTTemplateField field) {
		DataInputField dataField;
		GWTCardinality cardinality = field.getCardinality();
		if (field.hasOntologies()) {
			GenericSuggestOracle<TermSuggestion> oracle = AutoCompleteHelper.getSuggestOracle(field.getOntologies());
			if (cardinality.getMin() == 1 && cardinality.getMax() == 1) {
				String[] functionalPrefixes = field.getFunctionalPrefixes();
				if (functionalPrefixes != null && functionalPrefixes.length > 0) {
					//  simple, auto complete, prefixes
					dataField = new PrefixAutoCompleteInputField(oracle, functionalPrefixes);
				}
				else {
					// simple, auto complete
					dataField = new AutoCompleteInputField(oracle);
				}
			}
			else {
				// lists, auto complete
				dataField = new ListAutoCompleteInputField(oracle, cardinality, field.getName());
			}
		}
		else {
			if (cardinality.getMin() == 1 && cardinality.getMax() == 1) {
				// simple, no auto complete
				dataField = new DataInputField.TextFieldInput();
			}
			else {
				dataField = new DataInputField.ListInputField(cardinality, field.getName());
			}
		}
		return dataField;
	}

	/**
	 * Add an additional row for further template instances of the same type.
	 */
	public void appendRow() {
		synchronized (grid) {
			int rowCount = grid.getRowCount();
			grid.resizeRows(rowCount + 1);
			grid.getRowFormatter().setVerticalAlign(rowCount, HasVerticalAlignment.ALIGN_TOP);
			addRow(table.size()+2);
		}
	}
	
	/**
	 * Remove the last content row from the grid.
	 */
	public void deleteRow() {
		synchronized (grid) {
			if (!table.isEmpty()) {
				int rowCount = grid.getRowCount();
				grid.removeRow(rowCount-2);
				table.remove(table.size() - 1);
			}
		}
	}
	
	/**
	 * @return true, if there are no data row in the underlying table.
	 */
	public boolean isEmpty() {
		synchronized (grid) {
			return table.isEmpty();
		}
	}
	
	/**
	 * @return termTermplate
	 */
	public GWTTermTemplate getGwtTermTemplate() {
		return template;
	}
	
	/**
	 * @return list of parameters for the termTermplate
	 */
	public ExtractionResult extractParameters() {
		List<GWTTermGenerationParameter> result = new ArrayList<GWTTermGenerationParameter>();
		GWTTemplateField[] fields = template.getFields();
		boolean hasErrors = false;
		int lineCount = 0;
		for(ArrayList<DataInputField> dataFields : table) {
			lineCount++;
			GWTTermGenerationParameter parameter = new GWTTermGenerationParameter();
			for (int i = 0; i < fields.length; i++) {
				GWTTemplateField field = fields[i];
				DataInputField inputField = dataFields.get(i);
				boolean success = inputField.extractParameter(parameter, field);
				if (!success) {
					StringBuilder sb = new StringBuilder();
					sb.append("For template \"");
					sb.append(template.getName());
					sb.append("\", line number (");
					sb.append(lineCount);
					sb.append("), the input field \"");
					sb.append(field.getName());
					sb.append("\" has errors.");
					MessagePanel.addErrorMessage(new Label(sb.toString()));
					hasErrors = true;
				}
			}
			result.add(parameter);
		}
		return new ExtractionResult(result, !hasErrors);
	}
	
	public static class ExtractionResult {
		public final List<GWTTermGenerationParameter> parameters;
		public final boolean success;
		
		/**
		 * @param parameters
		 * @param success
		 */
		ExtractionResult(List<GWTTermGenerationParameter> parameters, boolean success) {
			super();
			this.parameters = parameters;
			this.success = success;
		}
	}
}