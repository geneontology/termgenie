/**
 * Setup term review page.
 * 
 * @returns empty object
 */
function TermGenieSubmissions(){
	
	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainContentPanel = jQuery('#MainContentPanel');
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
	              'recent.isEnabled',
	              'recent.getRecentTerms',
	              'openid.authRequest',
	              'browserid.verifyAssertion',
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
		// request check if the recent feature is enabled
		jsonService.recent.isEnabled({
			onSuccess: function(result) {
				if (result === true) {
					startLoadingReviewEntries();
				}
				else {
					setRecentSubmissionsDisabledMessage();
				}
			},
			onException: function(e) {
				jQuery.logSystemError('Could not check recent submissions feature on server',e);
				return true;
			}
		});
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainContentPanel.empty();
		mainControlPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function setRecentSubmissionsDisabledMessage() {
		mainMessagePanel.append('The recent submissions feature is not enabled for this TermGenie server.');
	}
	
	function startLoadingReviewEntries() {
		mainMessagePanel.append('Start loading recent terms.');
		mySession.getSessionId(function(sessionId){
			jsonService.recent.getRecentTerms({
				params: [sessionId],
				onSuccess: function(result) {
					mainMessagePanel.empty();
					if (result !== null) {
						renderRecentItems(result);
					}
					else {
						renderNoRecentItems();
					}
				},
				onException: function(e) {
					mainMessagePanel.empty();
					jQuery.logSystemError('Could not retrieve recent submissions from server',e);
					return true;
				}
			});
		});
	}
	
	function renderNoRecentItems() {
		mainMessagePanel.append('There are no recent submissions.');
	}
	
	function renderRecentItems(items) {
		mainContentPanel.empty();
		mainContentPanel.append('<div>There are '+items.length+' recent submissions:</div>')
		var mytable = jQuery('<table cellpadding="0" cellspacing="0" border="0" class="display"></table>');
		mainContentPanel.append(mytable);
		mytable.dataTable({
			"aaData":items,
			"aoColumns": [
			    {"sTitle": "Date", "mData":"date","bSearchable": false},
			    {"sTitle": "Committed", "mData":"committed", "sWidth": "20px","bSearchable": false},
			    {"sTitle": "Name", "mData":"lbl"},
			    {"sTitle": "Who", "mData":"user"},
			    {"sTitle": "Pattern", "mData":"pattern"}
//			    {"sTitle": "OBO", "mData":"content", "mRender": function ( data, type, row ) {
//                    return '<pre>'+data +'</pre>';
//                }}
			 ],
			 aaSorting: [[0,'desc']],
			 "oLanguage": {
		        "sSearch": "Search all columns:"
		      }
		});
	}
}
//actual call in jQuery to execute the activate the recent submissions display
//after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	TermGenieSubmissions();
});
