function termgenieReview(){
	
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
		mySession.getSessionId(function(sessionId){
			jsonService.review.isEnabled({
				onSuccess: function(result) {
					if (result === true) {
						
					}
					else {
						mainMessagePanel.append('The review feature is not enabled for this TermGenie server.');
					}
				},
				onException: function(e) {
					jQuery.logSystemError('Could not check review feature on server',e);
					return true;
				}
			});	
		});
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainReviewPanel.empty();
		mainControlPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function addCommitButton(onclick) {
		var commitButton = jQuery('<button>Commit</button>');
		commitButton.click(onclick);
		mainControlPanel.append(commitButton);
	}
	
	return {
		// empty object to hide internal functionality
	};
}
// actual call in jQuery to execute the TermGenie review scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	termgenieReview();
});
