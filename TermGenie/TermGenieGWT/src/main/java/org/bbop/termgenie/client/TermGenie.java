package org.bbop.termgenie.client;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.services.OntologyServiceAsync;
import org.bbop.termgenie.services.ValidateUserCredentialServiceAsync;
import org.bbop.termgenie.shared.FieldValidator;
import org.bbop.termgenie.shared.FieldValidator.GWTValidationHint;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GenerationResponse;
import org.bbop.termgenie.shared.Pair;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

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
		// setup the available ontologies
		AsyncCallback<List<String>> callback = new LoggingCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> ontologies) {
				// selection: Decide to which ontology the terms are added.
				final OntologySelectionPanel ontologySelectionPanel = getOntologySelectionPanel(ontologies);
				RootPanel.get("selectOntology").add(ontologySelectionPanel);
				
				// Add main content to the middle of the dock panel
				final AllTermListPanel allTermListPanel = getAllTermListPanel();
				RootPanel rootPanel = RootPanel.get("selectTemplatesAndCreateTerms");
				rootPanel.add(new ScrollPanel(allTermListPanel));

				// User credentials and final submit button for adding a term
				final UserPanel userPanel = getUserPanel();
				RootPanel.get("submitAndCredentials").add(userPanel);
				
				// create Handlers
				createOntologSelectionHandler(ontologySelectionPanel, allTermListPanel);
				createSubmitHandler(userPanel);
			}
		};
		OntologyServiceAsync.Util.getInstance().getAvailableOntologies(callback);
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
	
	private synchronized OntologySelectionPanel getOntologySelectionPanel() {
		if (ontologySelectionPanel == null) {
			throw new RuntimeException("This panel is not yet initialized.");
		}
		return ontologySelectionPanel;
	}
	
	private void createSubmitHandler(UserPanel userPanel) {
		userPanel.addGobalSubmitButtonHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				processTermGenerationRequest();
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
	
	private void processTermGenerationRequest() {
		// gather input data from fields
		final String ontology = getOntologySelectionPanel().getSelectedOntology();
		final Pair<GWTTermTemplate,GWTTermGenerationParameter>[] allParameters = getAllParameters();
	
		List<GWTValidationHint> allErrors = new ArrayList<GWTValidationHint>();
		// validate input data
		for (Pair<GWTTermTemplate, GWTTermGenerationParameter> pair : allParameters) {
			List<GWTValidationHint> errors = FieldValidator.validateParameters(pair.getOne(), pair.getTwo());
			if (!errors.isEmpty()) {
				allErrors.addAll(errors);
			}
		}
	
		if (!allErrors.isEmpty()) {
			// TODO show errors				
			return;
		}
		
		
		final boolean doCommit = getUserPanel().doCommit();  
		// check if commit
		if (doCommit) {
			// user credentials
			final String username = getUserPanel().getUserName();
			final String password = getUserPanel().getPassword();
			
			// validate credentials
			AsyncCallback<Boolean> callback = new LoggingCallback<Boolean>() {
				
				@Override
				public void onSuccess(Boolean result) {
					// success?
					if (result.booleanValue() == false) {
						// TODO show error
						return;
					}
					// submit request to server
					submitTermGenerationRequest(ontology, allParameters, doCommit, username, password);
				}
			};
			ValidateUserCredentialServiceAsync.Util.getInstance().isValidUser(username, password, callback);
			return;
		}
		else {
			submitTermGenerationRequest(ontology, allParameters, doCommit, null, null);
			return;
		}
	}

	private void submitTermGenerationRequest(String ontology,
			Pair<GWTTermTemplate,GWTTermGenerationParameter>[] allParameters,
			boolean commit, String username, String password)
	{
		// submit request to server
		AsyncCallback<GenerationResponse> callback = new LoggingCallback<GenerationResponse>() {
			
			@Override
			public void onSuccess(GenerationResponse result) {
				// TODO Auto-generated method stub
				// success? 
				// what to do next?			
			}
		};
		GenerateTermsServiceAsync.Util.getInstance().generateTerms(ontology, allParameters, false, null, null, callback);
	}
	
	@SuppressWarnings("unchecked")
	private Pair<GWTTermTemplate, GWTTermGenerationParameter>[] getAllParameters() {
		return getAllTermListPanel().getAllParameters().toArray(new Pair[0]);
	}
}
