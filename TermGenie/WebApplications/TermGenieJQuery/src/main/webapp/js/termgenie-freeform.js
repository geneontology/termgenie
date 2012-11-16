/**
 * Setup the TermGenie management page.
 * 
 * @returns empty object
 */
function TermGenieFreeForm(){
	
	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainConfigurationPanel = jQuery('#MainConfigurationPanel');
	var defaultErrorMessage = mainMessagePanel.children().first();
	
	//create proxy for json rpc
	var jsonService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: true,
	    methods: ['user.createSession',
	              'user.logout',
	              'user.isAuthenticated',
	              'user.keepSessionAlive',
	              'user.getValue',
	              'user.setValue',
	              'user.getValues',
	              'user.setValues',
	              'openid.authRequest',
	              'browserid.verifyAssertion',
	              'freeform.isEnabled',
	              'freeform.canView',
	              'freeform.getAvailableNamespaces,'
	              'freeform.autocomplete',
	              'freeform.validate']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonService, true);
	
	// add link to error console
	jQuery('#termgenie-error-console-link').click(function(){
		jQuery.openLogPanel();
	});
	
	// Sessions
	var mySession = jQuery.TermGenieSessionManager(jsonService);
	
	// global elements for this site
	var myLoginPanel = jQuery.LoginPanel(jsonService, mySession, onLogin, onLogout);
	
	function onLogin() {
		mainMessagePanel.empty();
		// request sessionId and then check if the free form feature is enabled
		jsonService.freeform.isEnabled({
			onSuccess: function(result) {
				if (result === true) {
					checkUserPermissions(function(hasPermission){ // on success
						if (hasPermission === true) {
							startFreeForm();
						}
						else {
							setInsufficientUserRightsMessage(myLoginPanel.getCredentials());
						}
					}, function(e) { // on error
						jQuery.logSystemError('Could not check user permissions on server',e);
						return true;
					});
				}
				else {
					setReviewDisabledMessage();
				}
			},
			onException: function(e) {
				jQuery.logSystemError('Could not check free form feature on server',e);
				return true;
			}
		});	
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainConfigurationPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function setReviewDisabledMessage() {
		mainMessagePanel.append('The free form feature is not enabled for this TermGenie server.');
	}
	
	function setInsufficientUserRightsMessage(username) {
		mainMessagePanel.append('The current user ('+username+') is not allowed to use the free form feature.');
	}
	
	function checkUserPermissions(onSuccess, onError) {
		// request sessionId and then check user permissions
		mySession.getSessionId(function(sessionId){
			jsonService.freeform.canView({
				params: [sessionId],
				onSuccess: onSuccess,
				onException: onError
			});	
		});
	}
	
	/**
	 * Start free form input.
	 */
	function startFreeForm() {
		// TODO place holder, implement proper free form input elements 
		mainConfigurationPanel.load('TermGenieFreeFormContent.html', function() {
			var myAccordion = MyAccordion('#accordion');
			
			mySession.getSessionId(function(sessionId){
				jsonService.freeform.canView({
					params: [sessionId],
					onSuccess: function(oboNamespaces) {
						if (oboNamespaces && oboNamespaces !== null && oboNamespaces.length >= 0) {
							populateFreeFormInput(oboNamespaces);
						}
						else {
							jQuery.logSystemError('Retrieved OBO namespaces are empty.', e);
						}
					},
					onException: function(e) {
						jQuery.logSystemError('Could not retrieve OBO namespaces from server', e);
					}
				});	
			});
		});
		
		function populateFreeFormInput(oboNamespaces) {
			// namespace selector
			var namespaceCell = jQuery('#free-form-input-namespace-cell');
			
			// relations
			// only available after a namespace has been selected
			
			// is_a with obo namespace aware auto-complete
			
			// part_of
			
			// def xrefs

			// synonyms
			
			// validate input button
		}

		// review panel
			// not editable!
			// require user to tick a check-box
			// active submit button
		
		// result panel
			// new ids
		
		/**
		 * Provide an 3-tab Accordion with the additional functionality to 
		 * enable/disable individual panes for click events.
		 * 
		 * @param id html-id for the accordian div tag
		 * 
		 * @returns methods for the accordion
		 */
		function MyAccordion(id) {
			// private variables;
			var selections = {};
			selections.Pane_0 = true;
			selections.Pane_1 = false;
			selections.Pane_2 = false;
			
			jQuery(id).accordion({ clearStyle: true, autoHeight: false, event: "" });
			
			// implement a custom click function
			// allow only to open panes, which are enabled in the selections object
			jQuery(id+' h3').click(function() {
				var idx = jQuery(id+' h3').index(this);
				var activate = selections["Pane_" + idx];
				if (activate) {
					jQuery(id).accordion("activate", idx);
				}
			});
			
			return {
				/**
				 * Active the specified panel.
				 * 
				 * @param pos position to activate (zero-based)
				 */
				activatePane : function(pos) {
					jQuery(id).accordion("activate", pos);
				},
				
				/**
				 * Set the status of a pane.
				 * 
				 * @param pos position to activate (zero-based)
				 * @param state boolean
				 */
				setPaneState : function(pos, state) {
					selections["Pane_" + pos] = state;
				},
			
				/**
				 * Enable a pane for click events.
				 * 
				 * @param pos position to enable (zero-based)
				 */
				enablePane : function(pos) {
					selections["Pane_" + pos] = true;
				},
				
				/**
				 * Disable a pane for click events.
				 * 
				 * @param pos position to disable (zero-based)
				 */
				disablePane : function(pos) {
					selections["Pane_" + pos] = false;
				}
			};
		};
	}
	
	/**
	 * Create a styled busy message div with and additional text for details. 
	 * 
	 * @param additionalText
	 * @returns String
	 */
	function createBusyMessage(additionalText) {
		return '<div class="termgenie-busy-message">'+
			'<img src="icon/wait26trans.gif" alt="Busy Icon"/>'+
			'<span class="termgenie-busy-message-text">Please wait.</span>'+
			'<div class="termgenie-busy-additional-text">'+additionalText+'</div><div>';
	}
	
	
	return {
		// empty object to hide internal functionality
	};
}
// actual call in jQuery to execute the TermGenie free form scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie free form.
	TermGenieFreeForm();
});
