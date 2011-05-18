package org.bbop.termgenie.client;

import static org.bbop.termgenie.shared.ErrorMessages.*;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.client.AllTermListPanel.AllExtractionResults;
import org.bbop.termgenie.services.GenerateTermsServiceAsync;
import org.bbop.termgenie.services.OntologyServiceAsync;
import org.bbop.termgenie.services.ValidateUserCredentialServiceAsync;
import org.bbop.termgenie.shared.GWTFieldValidator;
import org.bbop.termgenie.shared.GWTFieldValidator.GWTValidationHint;
import org.bbop.termgenie.shared.GWTGenerationResponse;
import org.bbop.termgenie.shared.GWTPair;
import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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
		
		MessagePanel.clearMessages();
		AllExtractionResults allExtractionResults = getAllTermListPanel().getAllParameters();
		// check if there were already some obvious errors.
		if (!allExtractionResults.success) {
			MessagePanel.popupError();
			return;
		}
		final GWTPair<GWTTermTemplate,GWTTermGenerationParameter>[] allParameters = allExtractionResults.getAllParameters();
	
		List<GWTValidationHint> allErrors = new ArrayList<GWTValidationHint>();
		// validate input data
		for (GWTPair<GWTTermTemplate, GWTTermGenerationParameter> pair : allParameters) {
			List<GWTValidationHint> errors = GWTFieldValidator.validateParameters(pair.getOne(), pair.getTwo());
			if (!errors.isEmpty()) {
				allErrors.addAll(errors);
			}
		}
	
		if (!allErrors.isEmpty()) {
			for (GWTValidationHint error : allErrors) {
				MessagePanel.addErrorMessage(createMessageWidget(error.getHint()));
			}
			MessagePanel.popupError();
			return;
		}
		
		
		final boolean doCommit = getUserPanel().doCommit();  
		// check if commit
		if (doCommit) {
			// user credentials
			final String username = getUserPanel().getUserName();
			if (username == null || username.isEmpty()) {
				MessagePanel.addErrorMessage(new Label(MISSING_USERNAME));
				MessagePanel.popupError();
			}
			final String password = getUserPanel().getPassword();
			
			// validate credentials
			AsyncCallback<Boolean> callback = new LoggingCallback<Boolean>() {
				
				@Override
				public void onSuccess(Boolean result) {
					// success?
					if (result.booleanValue() == false) {
						MessagePanel.addErrorMessage(new Label(UNKOWN_USERNAME_PASSWORD));
						MessagePanel.popupError();
						return;
					}
					// submit request to server
					submitTermGenerationRequest(ontology, allParameters, doCommit, username, password);
				}
			};
			ValidateUserCredentialServiceAsync.Util.getInstance().isValidUser(username, password, callback);
		}
		else {
			submitTermGenerationRequest(ontology, allParameters, doCommit, null, null);
		}
	}

	private void submitTermGenerationRequest(String ontology,
			GWTPair<GWTTermTemplate,GWTTermGenerationParameter>[] allParameters,
			final boolean commit, String username, String password)
	{
		// submit request to server
		AsyncCallback<GWTGenerationResponse> callback = new LoggingCallback<GWTGenerationResponse>() {
			
			@Override
			public void onSuccess(GWTGenerationResponse result) {
				if (result == null) {
					MessagePanel.addErrorMessage(new Label("No response was generated from the server."));
					MessagePanel.popupError();
					return;
				}
				GWTValidationHint[] errors = result.getErrors();
				if (errors != null && errors.length > 0) {
					for (GWTValidationHint error : errors) {
						MessagePanel.addErrorMessage(createMessageWidget(error.getHint()));
					}
					MessagePanel.popupError();
					return;
				}
				String[] generatedTerms = result.getGeneratedTerms();
				if (generatedTerms == null || generatedTerms.length == 0) {
					MessagePanel.addErrorMessage(new Label("No terms could be generated from your input."));
					MessagePanel.popupError();
					return;
				}
				for (String generatedTerm : generatedTerms) {
					MessagePanel.addSuccessMessage(createMessageWidget(generatedTerm));
				}
				MessagePanel.popupSuccess();
				
				// if commit was true, clear the templates, 
				// so that the user cannot generate them a second time
				// TODO decide if this is required
				if (commit) {
					getAllTermListPanel().clear();
				}
			}
		};
		GenerateTermsServiceAsync.Util.getInstance().generateTerms(ontology, allParameters, false, null, null, callback);
	}
	
	private static Widget createMessageWidget(String message) {
		return new HTML("<pre>"+message+"</pre>");
	}
}
