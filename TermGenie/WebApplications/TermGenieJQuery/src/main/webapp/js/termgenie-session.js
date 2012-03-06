(function($){
	var hasSession = false;
	var waitForInit = [];
	var sessionId = null;
	
	// Do NOT set lower than 1 minute
	var keepAliveIntervall =  1000 * 60 * 10; // 10 minutes in milliseconds
	
	var lastActive = jQuery.now();
	
	$.extend({"TermGenieSessionManager" : function(jsonService) {
		
		// default create a new session or re-use existing one
		createSession();
		
		return {
			getSessionId: function(callback) {
				if (hasSession) {
					// use this manadory call for the session id 
					// to keep track of the last time the 
					// session was updated.
					updateLastActive();
					callback(sessionId);
				}
				else {
					waitForInit.push(callback);
				}
			}
		}
		
		function setSession(result) {
			sessionId = result;
			hasSession = true;
			jQuery.each(waitForInit, function(index, callback){
				updateLastActive();
				callback(sessionId);
			});
			waitForInit = [];
		}
		
		function createSession() {
			// use json-rpc to create a new session
			jsonService.user.createSession({
				onSuccess: function(result) {
					setSession(result);
					startKeepAlive();
				},
				onException: function(e) {
					jQuery.logSystemError('Could not create a session. Please try reloading the page.',e);
				}
			});
		}
		
		function updateLastActive() {
			lastActive = jQuery.now();
		}
		
		function keepAlive() {
			if(jQuery.now() - lastActive > keepAliveIntervall){
				if (hasSession) {
					jsonService.user.keepSessionAlive({
						params: [sessionId],
						onSuccess: function(result) {
							if (result !== true) {
								jQuery.logUserMessage('Could not verify your session on the server. Please reload the webpage.');
							}
							else {
								updateLastActive();
							}
						},
						onException: function(e) {
							jQuery.logSystemError('An error occured while trying to keep the current session alive. Please try reloading the page.',e);
						}
					});
				}
			}
		}
		
		function startKeepAlive() {
			// intervalID hold the reference for removal with clearInterval
			var intervalID = setInterval(keepAlive, keepAliveIntervall); 
		}
	}});
})(jQuery);