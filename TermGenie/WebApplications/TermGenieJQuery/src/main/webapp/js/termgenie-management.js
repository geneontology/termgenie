/**
 * Setup term review page.
 * 
 * @returns empty object
 */
function TermGenieManagement(){
	
	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainConfigurationPanel = jQuery('#MainConfigurationPanel');
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
	              'management.isAuthorized',
	              'management.getModuleDetails']
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
		checkUserPermissions(function(hasPermission){ // on success
			if (hasPermission === true) {
				startManagementEntries();
			}
			else {
				setInsufficientUserRightsMessage(myLoginPanel.getCredentials());
			}
		}, function(e) { // on error
			jQuery.logSystemError('Could not check user permissions on server',e);
			return true;
		});
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainConfigurationPanel.empty();
		mainControlPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function setInsufficientUserRightsMessage(username) {
		mainMessagePanel.append('The current user ('+username+') is not allowed to use this review feature.');
	}
	
	function checkUserPermissions(onSuccess, onError) {
		// request sessionId and then check user permissions
		mySession.getSessionId(function(sessionId){
			jsonService.management.isAuthorized({
				params: [sessionId],
				onSuccess: onSuccess,
				onException: onError
			});	
		});
	}
	
	/**
	 * Start loading the available review entries from server.
	 */
	function startManagementEntries() {
		// add busy message
		mainConfigurationPanel.append(createBusyMessage('Retrieving management data from server.'));
		// request sessionId and then try to load commits for review
		mySession.getSessionId(function(sessionId){
			jsonService.management.getModuleDetails({
				params: [sessionId],
				onSuccess: function(entries){
					// empty the current content
					mainConfigurationPanel.empty();
					if (entries && entries.length > 0 && jQuery.isArray(entries)) {
						createModuleDetailsPanel(entries);
					}
				},
				onException: function(e) {
					// empty the current content
					mainConfigurationPanel.empty();
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
	
	function createModuleDetailsPanel(details) {
		mainConfigurationPanel.append('<h2>Module Configuration</h2>')
		jQuery.each(details, function(index, module){
			mainConfigurationPanel.append('<h3>'+module.moduleName+'</h3>');
			var content = '<table>';
			if(module.parameters) {
				content += '<tr><td class="termgenie-module-table-header">Name</td><td class="termgenie-module-table-header">Value</td></tr>';
				jQuery.each(module.parameters, function(parameterName, parameterValue){
					content += '<tr><td>'+parameterName+'</td><td>'+parameterValue+'</td></tr>';
				});
			}
			if(module.implementations) {
				content += '<tr><td class="termgenie-module-table-header">Interface</td><td class="termgenie-module-table-header">Implementation</td></tr>';
				jQuery.each(module.implementations, function(implementationName, implementationValue){
					content += '<tr><td>'+implementationName+'</td><td>'+implementationValue+'</td></tr>';
				});
			}
			if(module.provides) {
				content += '<tr><td class="termgenie-module-table-header">Provides Implementations</td></tr>';
				jQuery.each(module.provides, function(providesIndex, providesValue){
					content += '<tr><td>'+providesValue.one+'</td>';
					if (providesValue.two) {
						content += '<td>'+providesValue.two+'</td>';
					}
					content += '</tr>';
				});
			}
			content += '</table>';
			mainConfigurationPanel.append(content);
			if (module.additionalData) {
				var additionalTable = jQuery('<table></table>');
				additionalTable.css('margin-top', '10pt');
				additionalTable.append('<td colspan="2" class="termgenie-module-table-header">Additional Data</td>');
				jQuery.each(module.additionalData, function(additionalDataIndex, additionalDataPair){
					var additionalRow = jQuery('<tr></tr>');
					additionalRow.append('<td>'+additionalDataPair.one+'</td>');
					var additionalCell = jQuery('<td class="termgenie-module-table-special-cell"></td>');
					var additionalPre = jQuery('<pre></pre>');
					additionalCell.append(additionalPre);
					additionalPre.text(additionalDataPair.two);
					additionalRow.append(additionalCell);
					additionalTable.append(additionalRow);
				});
				mainConfigurationPanel.append(additionalTable);
			}
		});
	}
	
	return {
		// empty object to hide internal functionality
	};
}
// actual call in jQuery to execute the TermGenie management scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	TermGenieManagement();
});
