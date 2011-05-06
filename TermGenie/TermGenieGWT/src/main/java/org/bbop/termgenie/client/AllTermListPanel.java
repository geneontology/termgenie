package org.bbop.termgenie.client;

import java.util.LinkedHashMap;

import org.bbop.termgenie.client.helper.TermTemplateWidget;
import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.shared.GWTTermTemplate;

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

	/**
	 * Manage template selector panel.
	 */
	private final class TemplateSelector extends FlowPanel {

		private final GWTTermTemplate[] templates;
		private final ListBox comboBox;
		private final Button templateAddButton;

		TemplateSelector(final GWTTermTemplate[] templates) {
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

		synchronized void addTermTemplate(GWTTermTemplate termTemplate) {
			TermTemplateWidget panel = panels.get(termTemplate.getName());
			if (panel == null) {
				panel = new TermTemplateWidget(termTemplate);
				panels.put(termTemplate.getName(), panel);
				add(panel);
			} else {
				panel.appendRow();
			}
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
