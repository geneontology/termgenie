(function($){
	var hasSession = false;
	var waitForInit = [];
	var sessionId = null;
	
	$.extend({"TermGenieSessionManager" : function(jsonService) {
		
		// default create a new session or re-use existing one
		createSession();
		
		return {
			getSessionId: function(callback) {
				if (hasSession) {
					// use this manadory call for the session id 
					// to keep track of the last time the 
					// session was updated.
					// TODO Use this to reset counters for automatic keep alive pings to the server
					// updatelastcall
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
				callback(sessionId);
			});
			waitForInit = [];
		}
		
		function createSession() {
			// use json-rpc to create a new session
			jsonService.user.createSession({
				onSuccess: function(result) {
					setSession(result);
				},
				onException: function(e) {
					jQuery.logSystemError('Could not create a session. Please try reloading the page.',e);
				}
			});
		}
	}});
})(jQuery);