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
	
	function startLoadingReviewEntries() {
		// request sessionId and then try to load commits for review
		mySession.getSessionId(function(sessionId){
			jsonService.review.getPendingCommits({
				params: [sessionId],
				onSuccess: function(entries){
					if (entries && entries.length > 0 && jQuery.isArray(entries)) {
						createCommitReviewPanel(entries);
					}
					else {
						setNoCommitsForReviewFoundMessage();
					}
				},
				onException: function(e) {
					jQuery.logSystemError('Could not retrieve commits for review from server',e);
					return true;
				}
			});	
		});
	}
	
	function createCommitReviewPanel(entries) {
		
		
		addCommitButton(function(){ // onClick
			// retrieved marked entries
			
			// create array
			
			// make rpc call
		});
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
