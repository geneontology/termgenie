/**
 * Setup term review page.
 * 
 * @returns empty object
 */
function TermGenieReview(){
	
	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainReviewPanel = jQuery('#MainReviewPanel');
	var mainControlPanel = jQuery('#MainControlPanel');
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
	              'review.isEnabled',
	              'review.isAuthorized',
	              'review.getPendingCommits',
	              'review.commit']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonService, true);
	
	// Sessions
	var mySession = jQuery.TermGenieSessionManager(jsonService);
	
	// global elements for this site
	var myLoginPanel = jQuery.LoginPanel(jsonService, mySession, onLogin, onLogout);
	
	function onLogin() {
		mainMessagePanel.empty();
		// request sessionId and then check if the review feature is enabled
		jsonService.review.isEnabled({
			onSuccess: function(result) {
				if (result === true) {
					checkUserPermissions(function(hasPermission){ // on success
						if (hasPermission === true) {
							startLoadingReviewEntries();
						}
						else {
							setInsufficientUserRightsMessage(mySession.getCredentials());
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
				jQuery.logSystemError('Could not check review feature on server',e);
				return true;
			}
		});	
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainReviewPanel.empty();
		mainControlPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function setReviewDisabledMessage() {
		mainMessagePanel.append('The review feature is not enabled for this TermGenie server.');
	}
	
	function setInsufficientUserRightsMessage(username) {
		mainMessagePanel.append('The current user ('+username+') is not allowed to use this review feature.');
	}
	
	function setNoCommitsForReviewFoundMessage() {
		mainMessagePanel.append('There are currently no commits waiting to be approved.');
	}
	
	function checkUserPermissions(onSuccess, onError) {
		// request sessionId and then check user permissions
		mySession.getSessionId(function(sessionId){
			jsonService.review.isAuthorized({
				params: [sessionId],
				onSuccess: onSuccess,
				onException: onError
			});	
		});
	}
	
	/**
	 * Start loading the available review entries from server.
	 */
	function startLoadingReviewEntries() {
		// add busy message
		mainReviewPanel.append(createBusyMessage('Retrieving commits from server.'));
		// request sessionId and then try to load commits for review
		mySession.getSessionId(function(sessionId){
			jsonService.review.getPendingCommits({
				params: [sessionId],
				onSuccess: function(entries){
					// empty the current content
					mainReviewPanel.empty();
					if (entries && entries.length > 0 && jQuery.isArray(entries)) {
						createCommitReviewPanel(entries);
					}
					else {
						setNoCommitsForReviewFoundMessage();
					}
				},
				onException: function(e) {
					// empty the current content
					mainReviewPanel.empty();
					jQuery.logSystemError('Could not retrieve commits for review from server',e);
					return true;
				}
			});	
		});
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
	
	/**
	 * Create the review panel for the given list of entries. 
	 * 
	 * @param entries JsonCommitReviewEntry[]
	 */
	function createCommitReviewPanel(entries) {
		if (entries.length > 1) {
			mainReviewPanel.append('<div>There are '+entries.length+' pending commits to review.</div>');
		}
		else  {
			mainReviewPanel.append('<div>There is one pending commit to review.</div>');
		}
		
		var checkboxes = [];
		
		var table = jQuery('<table class="termgenie-layout-table termgenie-commit-review-main-table" cellSpacing="0" cellPadding="0"></table>');
		
		// setup list of entries
		jQuery.each(entries, function(index, entry){
			
			var checkbox = jQuery('<input type="checkbox" />');
			checkboxes.push(checkbox);
			addRow(table, checkbox, 'Details for commit #'+(entry.historyId), null);
			
			// user and date
			if (entry.user && entry.user.length > 0) {
				addRow(table, null, 'User', '<span>'+entry.user+'</span>');
			}
			if (entry.date && entry.date.length > 0) {
				addRow(table, null, 'Date', '<span>'+entry.date+'</span>');
			}
			
			jQuery.each(entry.diffs, function(diffIndex, diff){
				var operation = null;
				if (diff.operation === 0) {
					// add
					operation = jQuery('<div></div>');
					operation.append('<div>Add</div>');
					var editButton = jQuery('<button>Edit term</button>');
					operation.append(editButton);
					editButton.click(function(){
						var editDialog = jQuery('<div style="width:100%;heigth:100%;display: block;"></div>');
						var editField = jQuery('<textarea rows="16" cols="40" style="width:100%;heigth:250px;font-family:monospace;white-space: nowrap;">'+diff.diff+'</textarea>');
						editDialog.append(editField);
						editDialog.dialog({
							title: "Editor",
							resizable: true,
							height:450,
							width: 600,
							minHeight: 200,
							minWidth: 200,
							modal: true,
							buttons: {
								"Change": function() {
									diff.modified = true;
									diff.diff = editField.val();
									$( this ).dialog( "close" );
								},
								"Cancel": function() {
									$( this ).dialog( "close" );
								}
							}
						});
					});
				}
				else if (diff.operation === 1) {
					// modify
					operation = 'Modify';
				}
				else if (diff.operation === 2) {
					// remove
					operation = 'Remove';
				}
				if(operation !== null) {
					addRow(table, null, operation, '<pre>'+diff.diff+'</pre>');
				}
			});
		});
		mainReviewPanel.append(table);
		
		// add commit button
		addCommitButton(function(){ // onClick
			var reviewedEntries = [];
			
			// retrieved marked entries
			jQuery.each(entries, function(index, entry){
				var isSelected = checkboxes[index].is(':checked');
				if (isSelected === true) {
					reviewedEntries.push(entry);
				}
			});
			if (reviewedEntries.length > 0) {
				// set messages and lock commit button
				
				// commit
				executeCommitOnServer(reviewedEntries);
			}
			else {
				// set error message
				jQuery.logUserMessage('Please select at least one pending commit to proceed.');
			}
		});
		
		/**
		 * Add a new row to the table.
		 * 
		 * @param table target table
		 * @param col1 elem in col1
		 * @param col2 elem in col2
		 * @param col3 elem in col3
		 */
		function addRow(table, col1, col2, col3) {
			var tr = jQuery('<tr></tr>');
			var td;
			table.append(tr);
			// add column one 
			if (col1 && col1 !== null) {
				td = jQuery('<td></td>');
				td.append(col1);
				tr.append(td);
			}
			else {
				tr.append('<td></td>');
			}
			// add column two
			if (col2 && col2 !== null) {
				// make col2 spanning two columns, 
				// if col3 is null (not undefined)
				if (col3 === null) {
					td = jQuery('<td colspan="2"></td>'); 
				}
				else {
					td = jQuery('<td></td>');
				}
				td.append(col2);
				tr.append(td);
			}
			else {
				tr.append('<td></td>');
			}
			// add column three
			if (col3 && col3 !== null) {
				td = jQuery('<td></td>');
				td.append(col3);
				tr.append(td);
			}
		}
	}
	
	/**
	 * Internal flag for commit button.
	 * 
	 * #addCommitButton
	 * #disableCommitButton
	 */
	var commitEnabled = false;
	
	/**
	 * Add a commit button with a given onclick function.
	 * 
	 * @param onclick function to be executed on click
	 */
	function addCommitButton(onclick) {
		var commitButton = jQuery('<button>Commit</button>');
		commitButton.click(function(event){
			if (commitEnabled === true) {
				onclick(event);
			}
		});
		mainControlPanel.append(commitButton);
		commitEnabled = true;
	}
	
	/**
	 * Disable the commit button, via internal flag (commitEnabled).
	 */
	function disableCommitButton() {
		commitEnabled = false;
	}
	
	/**
	 * Execute the commit for the selected list of entries.
	 * 
	 * @param entries JsonCommitReviewEntry[]
	 */
	function executeCommitOnServer(entries) {
		// disable submit button
		disableCommitButton();
		
		// set busy message
		mainReviewPanel.empty();
		mainReviewPanel.append(createBusyMessage('Executing commit on server.'));
		
		// make rpc call
		mySession.getSessionId(function(sessionId){
			jsonService.review.commit({
				params: [sessionId, entries],
				onSuccess: function(result){
					mainReviewPanel.empty();
					mainControlPanel.empty();
					if (result && result.success && result.success === true) {
						renderCommitReviewSuccess(result.details);
					}
					else {
						jQuery.logSystemError('Could not commit terms.', result.message);
					}
					mainReviewPanel.append('<div>Reload page to restart commit review process.</div>');
				},
				onException: function(e) {
					mainControlPanel.empty();
					mainReviewPanel.empty();
					mainReviewPanel.append('Reload page to restart commit review process.');
					jQuery.logSystemError('Could not commit terms.',e);
					return true;
				}
			});	
		});
	}
	
	/**
	 * Render the success part of the review commit result.
	 * 
	 * @param details JsonCommitDetails[]
	 */
	function renderCommitReviewSuccess(details) {
		if (details.length > 1) {
			mainReviewPanel.append('<div>There have been '+details.length+' separate commit operations:</div>');
		}
		jQuery.each(details, function(index, detail){
			var elem = '<div>Status for commit #' + detail.historyId + ': ';
			if (detail.success === true) {
				elem += 'Success';
			}
			else {
				elem += 'Failure';
			}
			if (detail.message && detail.message.length > 0) {
				elem += '<br/>Message:';
				elem += details.message; 
			}
			if (detail.terms && detail.terms.length > 0) {
				elem += '<ul>';
				jQuery.each(detail.terms, function(termIndex, term){
					elem += '<li style="font-family:monospace;">ID: '+ term.tempId + ' Label: ' + term.label +'</li>';
				});
				elem += '</ul>';
			}
			elem += '</div>';
			mainReviewPanel.append(elem);
		});
	}
	
	return {
		// empty object to hide internal functionality
	};
}
// actual call in jQuery to execute the TermGenie review scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	TermGenieReview();
});
