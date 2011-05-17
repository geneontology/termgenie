package org.bbop.termgenie.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bbop.termgenie.client.helper.TermTemplateWidget;
import org.bbop.termgenie.client.helper.TermTemplateWidget.ExtractionResult;
import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTPair;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Container for all active {@link TermTemplateWidget}.
 * 
 */
public class AllTermListPanel extends VerticalPanel {

	private TemplateSelector templateSelector = null;
	private TermTemplateWidgetList termTemplateWidgets = null;
	private String ontology = null;

	public AllTermListPanel() {
		super();
		setSize("100%", "100%");
		// keep empty
		// use #setSelectedOntology() to populate the panel.
	}

	/**
	 * Set the underlying ontology and request available term templates.
	 * Generate selection menu from this list.
	 * 
	 * @param ontology
	 */
	public void setSelectedOntology(String ontology) {
		if (this.ontology == null || !this.ontology.equals(ontology)) {
			AsyncCallback<GWTTermTemplate[]> callback = new LoggingCallback<GWTTermTemplate[]>() {

				@Override
				public void onSuccess(GWTTermTemplate[] result) {
					setTemplateSelector(new TemplateSelector(result));
				}
			};
			GenerateTermsServiceAsync.Util.getInstance().getAvailableGWTTermTemplates(ontology,
					callback);
			this.ontology = ontology;
		}
	}

	public void removeTemplate(GWTTermTemplate template) {
		if (termTemplateWidgets != null) {
			termTemplateWidgets.removeTemplate(template);
		}
	}
	
	AllExtractionResults getAllParameters() {
		if (termTemplateWidgets == null) {
			return AllExtractionResults.EMPTY;
		}
		return termTemplateWidgets.getAllParameters();
	}
	
	void clearAllTermTemplates() {
		if (termTemplateWidgets != null) {
			termTemplateWidgets.clear();
		}
	}
	
	/**
	 * Manage template selector panel.
	 */
	private final class TemplateSelector extends FlowPanel {

		private final GWTTermTemplate[] templates;
		private final ListBox comboBox;
		private final Button templateAddButton;

		TemplateSelector(final GWTTermTemplate[] templates) {
			super();
			addStyleName("select-template-header");
			this.templates = templates;

			// create internal widgets
			final Label lblSelectTemplate = new InlineLabel("Select Template");
			comboBox = new ListBox();
			comboBox.addStyleName("select-template-combobox");
			templateAddButton = new Button("Add template");

			// configure internal widgets
			// sort templates according to alphabet
			Arrays.sort(templates, new Comparator<GWTTermTemplate>() {

				@Override
				public int compare(GWTTermTemplate o1, GWTTermTemplate o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
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
		GWTTermTemplate getSelectedTermTemplate() {
			return templates[comboBox.getSelectedIndex()];
		}

		/**
		 * @param handler
		 *            register a handler for the selection
		 */
		void addTemplateSelectorHandler(ClickHandler handler) {
			templateAddButton.addClickHandler(handler);
		}
	}

	/**
	 * Manage term template list widgets in panel.
	 */
	private final class TermTemplateWidgetList extends VerticalPanel {

		private final LinkedHashMap<String, TermTemplateWidget> panels;

		TermTemplateWidgetList() {
			super();
			panels = new LinkedHashMap<String, TermTemplateWidget>();
		}

		synchronized void removeTemplate(GWTTermTemplate template) {
			TermTemplateWidget templateWidget = panels.remove(template.getName());
			if (templateWidget != null) {
				remove(templateWidget);
			}
		}

		synchronized void addTermTemplate(GWTTermTemplate termTemplate) {
			TermTemplateWidget panel = panels.get(termTemplate.getName());
			if (panel == null) {
				panel = new TermTemplateWidget(termTemplate, AllTermListPanel.this);
				panels.put(termTemplate.getName(), panel);
				add(panel);
			} else {
				panel.appendRow();
			}
		}
		
		public synchronized void clear() {
			panels.clear();
			super.clear();
		}
		
		AllExtractionResults getAllParameters() {
			List<GWTPair<GWTTermTemplate, GWTTermGenerationParameter>> result = 
				new ArrayList<GWTPair<GWTTermTemplate,GWTTermGenerationParameter>>();
			boolean success = true;
			for (String key : panels.keySet()) {
				TermTemplateWidget templateWidget = panels.get(key);
				GWTTermTemplate termTemplate = templateWidget.getGwtTermTemplate();
				ExtractionResult extractionResult = templateWidget.extractParameters();
				success = success && extractionResult.success;
				for (GWTTermGenerationParameter parameter : extractionResult.parameters) {
					result.add(new GWTPair<GWTTermTemplate, GWTTermGenerationParameter>(termTemplate, parameter));
				} 
			}
			return new AllExtractionResults(result, success);
		}
	}

	public static class AllExtractionResults {
		
		static final AllExtractionResults EMPTY = new AllExtractionResults(Collections.<GWTPair<GWTTermTemplate, GWTTermGenerationParameter>>emptyList(), false);
		
		final List<GWTPair<GWTTermTemplate, GWTTermGenerationParameter>> parameters;
		public final boolean success;
		
		/**
		 * @param parameters
		 * @param success
		 */
		AllExtractionResults(List<GWTPair<GWTTermTemplate, GWTTermGenerationParameter>> parameters, boolean success) {
			super();
			this.parameters = parameters;
			this.success = success;
		}
		
		@SuppressWarnings("unchecked")
		GWTPair<GWTTermTemplate, GWTTermGenerationParameter>[] getAllParameters() {
			return parameters.toArray(new GWTPair[parameters.size()]);
		}
	}
	
	/**
	 * If a new selector is set (e.g. ontology change), remove all the current
	 * content for this panel.
	 * 
	 * @param selector
	 */
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
}
