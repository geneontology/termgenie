package org.bbop.termgenie.client;

import java.util.List;

import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.services.OntologyServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TermGenie implements EntryPoint {

	private OntologySelectionPanel ontologySelectionPanel;

	private UserPanel userPanel;

	private AllTermListPanel allTermListPanel;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		AsyncCallback<List<String>> callback = new LoggingCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> ontologies) {
				// Top selection choice: Decide to which ontology the terms are added.
				final OntologySelectionPanel ontologySelectionPanel = getOntologySelectionPanel(ontologies);
				RootPanel.get("selectOntology").add(decorateWidget(ontologySelectionPanel));
				
				// User credentials and final submit button for adding a term
				final UserPanel userPanel = getUserPanel();
				RootPanel.get("submitAndCredentials").add(decorateWidget(userPanel));
				
				// Add main content to the middle of the dock panel
				final AllTermListPanel allTermListPanel = getAllTermListPanel();
				RootPanel rootPanel = RootPanel.get("selectTemplatesAndCreateTerms");
				rootPanel.add(decorateWidget(new ScrollPanel(allTermListPanel)));
				
				// create Handlers
				createOntologSelectionHandler(ontologySelectionPanel, allTermListPanel);
				createSubmitHandler(userPanel);
			}

		};
		OntologyServiceAsync.Util.getInstance().getAvailableOntologies(callback);
	}
	
	protected Widget decorateWidget(Widget widget) {
//		DecoratorPanel decoratorPanel = new DecoratorPanel();
//		decoratorPanel.setWidget(widget);
//		decoratorPanel.setSize("100%", "100%");
//		return decoratorPanel;
		return widget;
	}

	private synchronized AllTermListPanel getAllTermListPanel() {
		if (allTermListPanel == null) {
			allTermListPanel = new AllTermListPanel();
		}
		return allTermListPanel;
	}

	private synchronized UserPanel getUserPanel() {
		if (userPanel == null) {
			userPanel = new UserPanel();
		}
		return userPanel;
	}

	private synchronized OntologySelectionPanel getOntologySelectionPanel(List<String> ontologies) {
		if (ontologySelectionPanel == null) {
			ontologySelectionPanel = new OntologySelectionPanel(ontologies);
		}
		return ontologySelectionPanel;
	}
	
	private void createSubmitHandler(UserPanel userPanel) {
		userPanel.addGobalSubmitButtonHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<Boolean> callback = new LoggingCallback<Boolean>() {
					
					@Override
					public void onSuccess(Boolean result) {
						// TODO Auto-generated method stub
						
					}
				};
				GenerateTermsServiceAsync.Util.getInstance().generateTerms(callback);
			}
		});
	}
	
	private void createOntologSelectionHandler(final OntologySelectionPanel ontologySelectionPanel, final AllTermListPanel allTermListPanel) {
		ontologySelectionPanel.addSelectListener(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String ontology = ontologySelectionPanel.getSelectedOntology();
				allTermListPanel.setSelectedOntology(ontology);
			}
		});
	}
}
