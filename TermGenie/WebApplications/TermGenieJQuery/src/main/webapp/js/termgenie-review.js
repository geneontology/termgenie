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
	              'review.commit',
	              'renderer.visualizeDiffTerms',
	              'progress.getProgress']
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
		// request sessionId and then check if the review feature is enabled
		jsonService.review.isEnabled({
			onSuccess: function(result) {
				if (result === true) {
					checkUserPermissions(function(hasPermission){ // on success
						if (hasPermission === true) {
							startLoadingReviewEntries();
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
	 * Widget for rendering process status messages provided by the TermGenie server.
	 * 
	 * @param parentElem parent DOM element
	 * @param limit max number of messages shown, if less or equals zero, all messages are shown
	 * @param renderDetails boolean flag, if true render optional message details
	 * @returns {
	 * 	  addMessages: function(messages)
	 *  }
	 */
	function ProgressInfoWidget(parentElem, limit, renderDetails) {
		
		var lineParent = jQuery('<div class="termgenie-progress-infos"></div>');
		parentElem.append(lineParent);
		
		return {
			addMessages: function(messages) {
				if (messages !== null) {
					jQuery.each(messages, function(index, progressMessage){
						var line = '<div>'
							+ '<span class="termgenie-progress-info-time">' + progressMessage.time + '</span>'
							+ '<span class="termgenie-progress-info-message">' + progressMessage.message + '</span>';
						if (renderDetails === true && progressMessage.details !== null) {
							line += '<div class="termgenie-progress-info-details">'
								+ '<span>Details:</span>'
								+ '<pre>'+progressMessage.details+'</pre>'
								+ '</div>';
						}
						line += '</div>'
						lineParent.append(line);
					});
					if (limit && limit > 0) {
						while (lineParent.children().length > limit) {
							lineParent.children().first().remove();
						}
					}
				}
			}
		};
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
			
			// email and date
			if (entry.email && entry.email.length > 0) {
				addRow(table, null, 'User email', '<span>'+entry.email+'</span>');
			}
			if (entry.date && entry.date.length > 0) {
				addRow(table, null, 'Date', '<span>'+entry.date+'</span>');
			}
			addEditableCommitMessage(entry, table);
			
			// button for visualizing the future hierarchy
			var renderHierarchyButton = jQuery('<button>Generate Image</button>');
			addRow(table, null, 'Term Hierarchy', renderHierarchyButton);
			
			renderHierarchyButton.click(function(){
				jsonService.renderer.visualizeDiffTerms({
					params: [entry.diffs],
					onSuccess: function(result) {
						if(result.success === true) {
							window.open(result.message);
						}
						else {
							jQuery.logUserMessage('Render Hierarchy service call failed', result.message);
							jQuery.openLogPanel();
						}
						
					},
					onException: function(e) {
						jQuery.logSystemError('Render Hierarchy service call failed', e);
					}
				});
			});
			
			// render each diff
			jQuery.each(entry.diffs, function(diffIndex, diff){
				var preDiff = jQuery('<pre>'+diff.diff+'</pre>');
				var operation = jQuery('<div></div>');
				if (diff.operation === 0) {
					// add
					operation.append('<div>Add</div>');
				}
				else if (diff.operation === 1) {
					// modify
					operation.append('<div>Modify</div>');
				}
				else if (diff.operation === 2) {
					// remove
					operation.append('<div>Remove</div>');
				}
				
				var obsolete = false;
				if(diff.isObsolete && diff.isObsolete === true) {
					obsolete = true;
				}
				
				var diffRelationRows = [];
				
				if(obsolete !== true) {
					var editButton = jQuery('<button>Edit term</button>');
					operation.append(editButton);
					editButton.click(function(){
						var editDialog = jQuery('<div style="width:100%;heigth:100%;display: block;"></div>');
						var editField = jQuery('<textarea rows="16" cols="40" style="width:100%;heigth:250px;font-family:monospace;white-space: nowrap;">'+diff.diff+'</textarea>');
						editDialog.append(editField);
						editDialog.dialog({
							title: "Term Editor",
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
									preDiff.empty();
									preDiff.append(diff.diff);
									$( this ).dialog( "close" );
								},
								"Cancel": function() {
									$( this ).dialog( "close" );
								}
							}
						});
					});
					var obsoleteButton = jQuery('<button>Make Obsolete</button>');
					operation.append(obsoleteButton);
					
					obsoleteButton.click(function(){
						editButton.remove();
						obsoleteButton.remove();
						operation.append('<div class="termgenie-obsolete-term">Obsolete</div>');
						diff.isObsolete = true;
						diff.obsoleteComment = 'This term was obsoleted at the TermGenie Gatekeeper stage.';
						diff.relations = null; // also remove all the changed relations
						jQuery.each(diffRelationRows, function(rowIndex, tr){
							tr.remove();
						});
						diff.modified = true;
					});
				}
				else {
					operation.append('<div class="termgenie-obsolete-term">Obsolete</div>');
				}
				
				addRow(table, null, operation, preDiff);
				
				if (diff.relations && diff.relations !== null && diff.relations.length > 0) {
					jQuery.each(diff.relations, function(relIndex, jsonChange){
						if (jsonChange.changes && jsonChange.changes !== null && jsonChange.changes.length > 0) {
							var relString = '<pre>';
							jQuery.each(jsonChange.changes, function(changeIndex, change){
								relString += change;
							});
							relString += '\n</pre>';
							var desc = 'Modified relations for term <span class="termgenie-pre">'+jsonChange.id+'</span>';
							if (jsonChange.label && jsonChange.label !== null) {
								desc += ' with label: <span class="termgenie-pre">' + jsonChange.label+'</span>';
							}
							var tr = addRow(table, null, desc, relString);
							diffRelationRows.push(tr);
						}
					});
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
		
		function addEditableCommitMessage(entry, table) {
			if (entry.commitMessage && entry.commitMessage.length > 0) {
				var descriptionColumn = jQuery('<div>Commit Message</div>')
				var editButton = jQuery('<button>Edit Message</button>');
				descriptionColumn.append(editButton);
				var wholeMessageColumn = jQuery('<div></div>');
				var secondaryMessage = jQuery('<div class="hint-content">No need to add IDs, they will be added to the commit message by TermGenie during commit.</div>');
				var messageColumn = jQuery('<div>'+entry.commitMessage+'</div>');
				wholeMessageColumn.append(messageColumn);
				wholeMessageColumn.append(secondaryMessage);
				editButton.click(function(){
					var editDialog = jQuery('<div style="width:100%;heigth:100%;display: block;"></div>');
					var editField = jQuery('<textarea rows="16" cols="40" style="width:100%;heigth:250px;">'+entry.commitMessage+'</textarea>');
					editDialog.append(editField);
					editDialog.dialog({
						title: "Commit Message Editor",
						resizable: true,
						height:450,
						width: 600,
						minHeight: 200,
						minWidth: 200,
						modal: true,
						buttons: {
							"Change": function() {
								entry.commitMessage = editField.val();
								entry.commitMessageChanged = true;
								messageColumn.empty();
								messageColumn.append(entry.commitMessage);
								$( this ).dialog( "close" );
							},
							"Cancel": function() {
								$( this ).dialog( "close" );
							}
						}
					});
				});
				addRow(table, null, descriptionColumn, wholeMessageColumn);
			}
		}
		
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
			return tr;
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
		var busyMessage = jQuery(createBusyMessage('Executing commit on server.'));
		mainReviewPanel.append(busyMessage);
		var progressInfo = ProgressInfoWidget(busyMessage, 10, true);
		
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
				},
				onProgress: function(uuid) {
					jsonService.progress.getProgress({
						params:[uuid],
						onSuccess: function(messages) {
							progressInfo.addMessages(messages);
						}
					});
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
			var elem = jQuery('<div></div>');
			var elemContent = 'Status for commit #' + detail.historyId + ': ';
			if (detail.success === true) {
				elemContent += 'Success';
			}
			else {
				elemContent += 'Failure';
			}
			if (detail.message && detail.message.length > 0) {
				elemContent += '<br/>Message:';
				elemContent += details.message; 
			}
			if (detail.terms && detail.terms.length > 0) {
				elemContent += '<ul>';
				jQuery.each(detail.terms, function(termIndex, term){
					elemContent += '<li style="font-family:monospace;">';
					if (term.isObsolete === true) {
						elemContent += '<span class="term-label-obsolete">(obsolete)</span> ';
					}
					elemContent += 'ID: '+ term.tempId + ' Label: ' + term.label + '</li>';
				});
				elemContent += '</ul>';
			}
			elem.append(elemContent);
			if (detail.diff && detail.diff !== null) {
				var diffElem = jQuery('<div class="termgenie-changes-to-ontology"></div>');
				var diffElemHeader = jQuery('<div>Changes to ontology (Click to open or close)</div>');
				diffElem.append(diffElemHeader);
				var diffElemContent = jQuery('<pre></pre>');
				diffElem.append(diffElemContent);
				diffElemContent.append(detail.diff);
				elem.append(diffElem);
				
				// add toggle functionality to diff header
				diffElemHeader.click(function(){
					diffElemContent.toggle('fold');
				});
				
				// hidden as default
				diffElemContent.hide();
			}
			
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
