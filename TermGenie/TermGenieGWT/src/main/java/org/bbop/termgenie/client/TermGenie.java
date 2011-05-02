package org.bbop.termgenie.client;

import java.util.List;

import org.bbop.termgenie.services.OntologyServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TermGenie implements EntryPoint {

	private FlowPanel ontologySelectionPanel;

	private HorizontalPanel userPanel;

	private FlowPanel allTermListPanel;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		RootPanel rootPanel = RootPanel.get("mainlayoutbody");
		
		final DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
		dockPanel.setSize("99%", "99%");
		
		rootPanel.add(dockPanel, 1, 1);
		
		AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> ontologies) {
				// Top selection choice: Decide to which ontology the terms are added.
				FlowPanel ontologySelectionPanel = getOntologySelectionPanel(ontologies);
				dockPanel.addNorth(decorateWidget(ontologySelectionPanel), 40.0d);
				
				// User credentials and final submit button for adding a term
				HorizontalPanel userPanel = getUserPanel();
				dockPanel.addSouth(decorateWidget(userPanel), 100.0d);
				
				// Add main content to the middle of the dock panel
				FlowPanel allTermListPanel = getAllTermListPanel();
				dockPanel.add(decorateWidget(allTermListPanel));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				InternalErrorPanel panel = new InternalErrorPanel(caught);
				dockPanel.add(panel);
			}
		};
		OntologyServiceAsync.Util.getInstance().getAvailableOntologies(callback);
	}
	
	private Widget decorateWidget(Widget widget) {
//		DecoratorPanel decoratorPanel = new DecoratorPanel();
//		decoratorPanel.setWidget(widget);
//		decoratorPanel.setSize("100%", "100%");
//		return decoratorPanel;
		return widget;
	}

	private synchronized FlowPanel getAllTermListPanel() {
		if (allTermListPanel == null) {
			allTermListPanel = new FlowPanel();
			allTermListPanel.setSize("100%", "100%");
			FlowPanel allTermListHeaderPanel = new FlowPanel();
			allTermListPanel.add(allTermListHeaderPanel);
			Label lblSelectTemplate = new InlineLabel("Select Template");
			allTermListHeaderPanel.add(lblSelectTemplate);
			ListBox comboBox = new ListBox();
			comboBox.addItem("all_regulation");
			comboBox.addItem("all_regulation_mf");
			comboBox.addItem("involved_in");
			comboBox.addItem("takes_place_in");
			comboBox.addItem("part_of_cell_component");
			comboBox.addItem("protein_binding");
			comboBox.addItem("metazoan_development");
			comboBox.addItem("metazoan_morphogenesis");
			comboBox.addItem("plant_development");
			comboBox.addItem("plant_morphogenesis");
			comboBox.addItem("structural_protein_complex");
			allTermListHeaderPanel.add(comboBox);
			Button templateAddButton = new Button("templateAddButton");
			templateAddButton.setText("Add template");
			allTermListHeaderPanel.add(templateAddButton);
			FlowPanel TermExample1 = new FlowPanel();
			allTermListPanel.add(TermExample1);
			Label lblTakesplacein = new Label("takes_place_in");
			TermExample1.add(lblTakesplacein);
			Grid grid = new Grid(4, 7);
			TermExample1.add(grid);
			grid.setCellSpacing(2);
			grid.setCellPadding(2);
			Label lblRequired = new Label("Required");
			grid.setWidget(0, 1, lblRequired);
			Label lblOptional = new Label("Optional");
			grid.setWidget(0, 3, lblOptional);
			Label lblBiologicalProcess = new Label("biological process");
			grid.setWidget(1, 1, lblBiologicalProcess);
			Label lblCellularcomponent = new Label("cellular_component");
			grid.setWidget(1, 2, lblCellularcomponent);
			Label lblName = new Label("name");
			grid.setWidget(1, 3, lblName);
			Label lblDef = new Label("def");
			grid.setWidget(1, 4, lblDef);
			Label lblDefref = new Label("def_ref");
			grid.setWidget(1, 5, lblDefref);
			Label lblComment = new Label("comment");
			grid.setWidget(1, 6, lblComment);
			PushButton pshbtnRemove = new PushButton("remove");
			grid.setWidget(2, 0, pshbtnRemove);
			SuggestBox suggestBox = new SuggestBox();
			grid.setWidget(2, 1, suggestBox);
			SuggestBox suggestBox_1 = new SuggestBox();
			grid.setWidget(2, 2, suggestBox_1);
			TextBox textBox = new TextBox();
			textBox.setVisibleLength(10);
			grid.setWidget(2, 3, textBox);
			TextBox textBox_1 = new TextBox();
			textBox_1.setVisibleLength(10);
			grid.setWidget(2, 4, textBox_1);
			TextBox textBox_2 = new TextBox();
			textBox_2.setVisibleLength(10);
			grid.setWidget(2, 5, textBox_2);
			TextBox textBox_3 = new TextBox();
			textBox_3.setVisibleLength(10);
			grid.setWidget(2, 6, textBox_3);
			PushButton pshbtnAdd = new PushButton("add");
			grid.setWidget(3, 0, pshbtnAdd);
			FlowPanel TermExample2 = new FlowPanel();
			allTermListPanel.add(TermExample2);
			Label lblAllregulation = new Label("all_regulation");
			TermExample2.add(lblAllregulation);
			Grid grid_1 = new Grid(4, 4);
			grid_1.setCellSpacing(2);
			grid_1.setCellPadding(2);
			TermExample2.add(grid_1);
			Label label_1 = new Label("Required");
			grid_1.setWidget(0, 1, label_1);
			Label label_2 = new Label("Optional");
			grid_1.setWidget(0, 3, label_2);
			Label lblPrefix = new Label("prefix");
			grid_1.setWidget(1, 1, lblPrefix);
			Label lblBiologicalProcess_1 = new Label("biological process");
			grid_1.setWidget(1, 2, lblBiologicalProcess_1);
			Label lblDefxref = new Label("def_xref");
			grid_1.setWidget(1, 3, lblDefxref);
			PushButton pushButton = new PushButton("remove");
			grid_1.setWidget(2, 0, pushButton);
			VerticalPanel verticalPanel = new VerticalPanel();
			verticalPanel.setSpacing(2);
			grid_1.setWidget(2, 1, verticalPanel);
			CheckBox rdbtnRegulation = new CheckBox("regulation");
			verticalPanel.add(rdbtnRegulation);
			CheckBox rdbtnNegativeregulation = new CheckBox("negative_regulation");
			verticalPanel.add(rdbtnNegativeregulation);
			CheckBox chckbxPositiveregulation = new CheckBox("positive_regulation");
			verticalPanel.add(chckbxPositiveregulation);
			SuggestBox suggestBox_3 = new SuggestBox();
			grid_1.setWidget(2, 2, suggestBox_3);
			TextBox textBox_4 = new TextBox();
			textBox_4.setVisibleLength(10);
			grid_1.setWidget(2, 3, textBox_4);
			PushButton pushButton_1 = new PushButton("add");
			grid_1.setWidget(3, 0, pushButton_1);
		}
		return allTermListPanel;
	}

	private synchronized HorizontalPanel getUserPanel() {
		if (userPanel == null) {
			userPanel = new HorizontalPanel();
			userPanel.setSize("100%", "100px");
			Grid userCredentialGrid = new Grid();
			userPanel.add(userCredentialGrid);
			userCredentialGrid.resize(2, 2);
			userCredentialGrid.setCellSpacing(3);
			userCredentialGrid.setCellPadding(3);
			Label lblUsername = new Label("Username");
			userCredentialGrid.setWidget(0, 0, lblUsername);
			TextBox usernameTextBox = new TextBox();
			usernameTextBox.setMaxLength(255);
			userCredentialGrid.setWidget(0, 1, usernameTextBox);
			userCredentialGrid.getCellFormatter().setHorizontalAlignment(0, 1,
					HasHorizontalAlignment.ALIGN_RIGHT);
			Label lblPassword = new Label("Password");
			userCredentialGrid.setWidget(1, 0, lblPassword);
			PasswordTextBox passwordTextBox = new PasswordTextBox();
			passwordTextBox.setMaxLength(255);
			userCredentialGrid.setWidget(1, 1, passwordTextBox);
			HorizontalPanel commitPanel = new HorizontalPanel();
			userPanel.add(commitPanel);
			commitPanel.setSpacing(10);
			CheckBox commitcheckbox = new CheckBox("commit");
			commitPanel.add(commitcheckbox);
			commitPanel.setCellVerticalAlignment(commitcheckbox, HasVerticalAlignment.ALIGN_MIDDLE);
			commitPanel.setCellHorizontalAlignment(commitcheckbox,
					HasHorizontalAlignment.ALIGN_CENTER);
			Button globalSubmitButton = new Button("GlobalSubmitButton");
			commitPanel.add(globalSubmitButton);
			commitPanel.setCellVerticalAlignment(globalSubmitButton,
					HasVerticalAlignment.ALIGN_MIDDLE);
			commitPanel.setCellHorizontalAlignment(globalSubmitButton,
					HasHorizontalAlignment.ALIGN_CENTER);
			globalSubmitButton.setText("Submit Request");
			globalSubmitButton.setSize("160px", "40px");
		}
		return userPanel;
	}

	private synchronized FlowPanel getOntologySelectionPanel(List<String> ontologies) {
		if (ontologySelectionPanel == null) {
			ontologySelectionPanel = new OntologySelectionPanel(ontologies);
		}
		return ontologySelectionPanel;
	}
}
