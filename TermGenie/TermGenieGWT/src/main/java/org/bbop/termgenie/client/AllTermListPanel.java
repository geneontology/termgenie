package org.bbop.termgenie.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.services.OntologyService.TermSuggestion;
import org.bbop.termgenie.services.OntologyServiceAsync;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AllTermListPanel extends VerticalPanel {

	private final static Map<String, SuggestOracle> oracles = new HashMap<String, SuggestOracle>();
	private TemplateSelector templateSelector = null;
	private TermTemplateWidgetList termTemplateWidgets = null;
	
	private final class TemplateSelector extends FlowPanel {
	
		private final GWTTermTemplate[] templates;
		private final ListBox comboBox;
		private final Button templateAddButton;
		
		public TemplateSelector(final GWTTermTemplate[] templates) {
			super();
			this.templates = templates;
			
			// create internal widgets
			final Label lblSelectTemplate = new InlineLabel("Select Template");
			comboBox = new ListBox();
			templateAddButton = new Button("Add template");
			
			// configure internal widgets
			for (GWTTermTemplate termTemplate : templates) {
				comboBox.addItem(termTemplate.getName());
			}

			// add internal widgets
			add(lblSelectTemplate);
			add(comboBox);
			add(templateAddButton);
		}
		
		/**
		 * @return the currently selected template
		 */
		public GWTTermTemplate getSelectedTermTemplate() {
			return templates[comboBox.getSelectedIndex()];
		}
		
		/**
		 * @param handler register a handler for the selection
		 */
		public void addTemplateSelectorHandler(ClickHandler handler) {
			templateAddButton.addClickHandler(handler);
		}
	}
	
	private final class TermTemplateWidgetList extends VerticalPanel {
		
		private final LinkedHashMap<String, TermTemplateWidget> panels;
		
		public TermTemplateWidgetList() {
			super();
			panels = new LinkedHashMap<String, TermTemplateWidget>();
			
//			FlowPanel example1 = new FlowPanel();
//			add(example1);
//			Label lblTakesplacein = new Label("takes_place_in");
//			example1.add(lblTakesplacein);
//			Grid grid = new Grid(4, 7);
//			example1.add(grid);
//			grid.setCellSpacing(2);
//			grid.setCellPadding(2);
//			Label lblRequired = new Label("Required");
//			grid.setWidget(0, 1, lblRequired);
//			Label lblOptional = new Label("Optional");
//			grid.setWidget(0, 3, lblOptional);
//			Label lblBiologicalProcess = new Label("biological process");
//			grid.setWidget(1, 1, lblBiologicalProcess);
//			Label lblCellularcomponent = new Label("cellular_component");
//			grid.setWidget(1, 2, lblCellularcomponent);
//			Label lblName = new Label("name");
//			grid.setWidget(1, 3, lblName);
//			Label lblDef = new Label("def");
//			grid.setWidget(1, 4, lblDef);
//			Label lblDefref = new Label("def_ref");
//			grid.setWidget(1, 5, lblDefref);
//			Label lblComment = new Label("comment");
//			grid.setWidget(1, 6, lblComment);
//			PushButton pshbtnRemove = new PushButton("remove");
//			grid.setWidget(2, 0, pshbtnRemove);
//			SuggestBox suggestBox = new SuggestBox();
//			grid.setWidget(2, 1, suggestBox);
//			SuggestBox suggestBox_1 = new SuggestBox();
//			grid.setWidget(2, 2, suggestBox_1);
//			TextBox textBox = new TextBox();
//			textBox.setVisibleLength(10);
//			grid.setWidget(2, 3, textBox);
//			TextBox textBox_1 = new TextBox();
//			textBox_1.setVisibleLength(10);
//			grid.setWidget(2, 4, textBox_1);
//			TextBox textBox_2 = new TextBox();
//			textBox_2.setVisibleLength(10);
//			grid.setWidget(2, 5, textBox_2);
//			TextBox textBox_3 = new TextBox();
//			textBox_3.setVisibleLength(10);
//			grid.setWidget(2, 6, textBox_3);
//			PushButton pshbtnAdd = new PushButton("add");
//			grid.setWidget(3, 0, pshbtnAdd);
//			
//			
//			FlowPanel example2 = new FlowPanel();
//			add(example2);
//			
//			Label lblAllregulation = new Label("all_regulation");
//			example2.add(lblAllregulation);
//			Grid grid_1 = new Grid(4, 4);
//			grid_1.setCellSpacing(2);
//			grid_1.setCellPadding(2);
//			example2.add(grid_1);
//			Label label_1 = new Label("Required");
//			grid_1.setWidget(0, 1, label_1);
//			Label label_2 = new Label("Optional");
//			grid_1.setWidget(0, 3, label_2);
//			Label lblPrefix = new Label("prefix");
//			grid_1.setWidget(1, 1, lblPrefix);
//			Label lblBiologicalProcess_1 = new Label("biological process");
//			grid_1.setWidget(1, 2, lblBiologicalProcess_1);
//			Label lblDefxref = new Label("def_xref");
//			grid_1.setWidget(1, 3, lblDefxref);
//			PushButton pushButton = new PushButton("remove");
//			grid_1.setWidget(2, 0, pushButton);
//			VerticalPanel verticalPanel = new VerticalPanel();
//			verticalPanel.setSpacing(2);
//			grid_1.setWidget(2, 1, verticalPanel);
//			CheckBox rdbtnRegulation = new CheckBox("regulation");
//			verticalPanel.add(rdbtnRegulation);
//			CheckBox rdbtnNegativeregulation = new CheckBox("negative_regulation");
//			verticalPanel.add(rdbtnNegativeregulation);
//			CheckBox chckbxPositiveregulation = new CheckBox("positive_regulation");
//			verticalPanel.add(chckbxPositiveregulation);
//			SuggestBox suggestBox_3 = new SuggestBox();
//			grid_1.setWidget(2, 2, suggestBox_3);
//			TextBox textBox_4 = new TextBox();
//			textBox_4.setVisibleLength(10);
//			grid_1.setWidget(2, 3, textBox_4);
//			PushButton pushButton_1 = new PushButton("add");
//			grid_1.setWidget(3, 0, pushButton_1);
		}

		public synchronized void addTermTemplate(GWTTermTemplate termTemplate) {
			TermTemplateWidget panel = panels.get(termTemplate.getName());
			if (panel == null) {
				panel = new TermTemplateWidget(termTemplate);
				panels.put(termTemplate.getName(), panel);
				add(panel);
			}
			else {
				panel.appendRow();
			}
		}
	}
	
	private final class TermTemplateWidget extends FlowPanel {
		
		private final GWTTermTemplate template;
		private ArrayList<ArrayList<Widget>> table = new ArrayList<ArrayList<Widget>>();
		private final Label lblRequired = new Label("Required");
		private final Label lblOptional = new Label("Optional");
		private final Button addRowButton = new Button("add");
		private final Grid grid;

		TermTemplateWidget(GWTTermTemplate template) {
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
			table.add(new ArrayList<Widget>(0));
			table.add(new ArrayList<Widget>(0));
			
			// add the first data row
			appendRow();
		}

		private void addRow(int rowPos) {
			PushButton pshbtnRemove = new PushButton("remove");
			grid.setWidget(rowPos, 0, pshbtnRemove);
			
			GWTTemplateField[] fields = template.getFields();
			
			ArrayList<Widget> widgets = new ArrayList<Widget>(fields.length);
			table.add(widgets);
			
			for (int i = 0; i < fields.length; i++) {
				GWTTemplateField field = fields[i];
				String ontology = field.getOntology();
				Widget widget;
				if (ontology != null) {
					SuggestOracle oracle = getSuggestOracle(ontology);
					widget = new SuggestBox(oracle);
				}
				else {
					TextBox textBox = new TextBox();
					textBox.setVisibleLength(10);
					widget = textBox;
				}
				grid.setWidget(rowPos, i+1, widget);
				widgets.add(widget);
			}
			grid.setWidget(rowPos+1, 0, addRowButton);
		}

		void appendRow() {
			grid.resizeRows(grid.getRowCount()+1);
			addRow(table.size());
		}
	}
	
	

	public AllTermListPanel() {
		super();
		setSize("100%", "100%");
		// keep empty
		// use #setSelectedOntology() to populate the panel.
	}
	
	private synchronized void setTemplateSelector(TemplateSelector selector) {
		setVisible(false);
		// remove old widgets
		if (templateSelector != null) {
			// remove old widget
			remove(templateSelector);
		}
		if (termTemplateWidgets != null) {
			// clear current list of term template widgets.
			remove(termTemplateWidgets);
		}
		// create new widgets
		templateSelector = selector;
		termTemplateWidgets = new TermTemplateWidgetList();
		
		// create and register handlers
		selector.addTemplateSelectorHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				GWTTermTemplate termTemplate = templateSelector.getSelectedTermTemplate();
				termTemplateWidgets.addTermTemplate(termTemplate);
			}
		});
		
		// add widgets to panel
		add(selector);
		add(termTemplateWidgets);
		setVisible(true);
	}
	
	public void setSelectedOntology(String ontology) {
		AsyncCallback<GWTTermTemplate[]> callback = new LoggingCallback<GWTTermTemplate[]>() {

			@Override
			public void onSuccess(GWTTermTemplate[] result) {
				setTemplateSelector(new TemplateSelector(result));
			}
		};
		GenerateTermsServiceAsync.Util.getInstance().getAvailableGWTTermTemplates(ontology, callback);
	}
	
	private static SuggestOracle getSuggestOracle(final String ontology) {
		synchronized (oracles) {
			SuggestOracle suggestOracle = oracles.get(ontology);
			if (suggestOracle == null) {
				suggestOracle = new SuggestOracle() {

					@Override
					public void requestSuggestions(final Request request, final Callback callback) {
						String query = request.getQuery();
						AsyncCallback<List<TermSuggestion>> t = new LoggingCallback<List<TermSuggestion>>() {
							
							@Override
							public void onSuccess(List<TermSuggestion> result) {
								callback.onSuggestionsReady(request, new Response(result));
							}
						};
						OntologyServiceAsync.Util.getInstance().autocompleteQuery(query, ontology, t);
						
					}
				};
				oracles.put(ontology, suggestOracle);
			}
			return suggestOracle;
		}
	}
	
	
}
