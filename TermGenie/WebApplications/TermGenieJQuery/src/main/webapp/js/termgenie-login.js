/*
 * Login panel which is added to the top right corner of the TermGenie 
 * web-site. Uses Persona (BrowserID) and requires at least one counterpart
 * rpc method on the server).
 * 
 * The loginPanel requires two parameters: jsonService and mySession.
 * 
 * jsonService is the rpc method function object
 * 
 * mySession session handling method function object
 * 
 * Depends:
 * 	jquery.ui.dialog.js
 *  termgenie-logging.js
 */
(function($){
	
	/**
	 * Handle all elements of the login process.
	 * 
	 * @returns methods for the login panel
	 */
	$.extend({"LoginPanel" : function(jsonService, mySession, globalLoginCallback, globalLogoutCallback) {
		
		var panel = createLoginPanel();
		var userInfo = null;
		
		return {
			/**
			 * Check if the session is logged in. 
			 * 
			 * @returns boolean
			 */
			isLoggedIn: function() {
				return userInfo !== null;
			},
			
			/**
			 * Retrieve the username.
			 * Only defined, if the user is logged in.
			 * 
			 * @returns {String} username or null
			 */
			getCredentials: function() {
				if (userInfo !== null) {
					return userInfo.username;
				}
				return null;
			}
		};
		
		function setUserInfo(username) {
			userInfo = {
				'username': username
			};
		}
		
		function clearUserInfo() {
			userInfo = null;
		}
		
		/**
		 * send the server the logout RPC call and clear JavaScript of user 
		 * information.
		 */
		function logout() {
			// request sessionId and then try to logout on server
			mySession.getSessionId(function(sessionId){
				jsonService.user.logout({
					params: [sessionId],
					onSuccess: function(){
						if (globalLogoutCallback && jQuery.isFunction(globalLogoutCallback)) {
							globalLogoutCallback();
						}
					},
					onException: function(e) {
						jQuery.logSystemError('Could not logout session', e, true);
					}
				});	
			});
			clearUserInfo();
		}
		
		/**
		 * Create the panel for the user information, login and logout 
		 * buttons to the site.
		 */
		function createLoginPanel() {
			var elem = jQuery('<div style="width: 200px"></div>');
			elem.appendTo('body');
			elem.css({
				display:'block',
				position:'absolute',
				top:10,
				right:10,
				width:'200px',
				background:'#eee',
				border:'1px solid #ddd'
			});
			var loginClickElem = jQuery('<span class="myClickable">Log in</span>');
			elem.append(loginClickElem);
			
			var logoutClickElem = jQuery('<span class="myClickable">Log out</span>');
			var displayUsernameElem = jQuery('<div class="termgenie-username"></div>');
			
			var loginDialogPanel = LoginDialogPanel();
			
			loginClickElem.click(function(){
                // http://localhost:8080/termgenie/gh-authenticate
                // loginDialogPanel.open();
				window.location.href = "http://localhost:8080/termgenie/gh-authenticate";
			});
			
			logoutClickElem.click(function(){
				logout();
				logoutClickElem.detach();
				displayUsernameElem.detach();
				elem.append(loginClickElem);
			});
			
			// check if session is already authenticated
			isAuthenticated(function(username){ // onSuccess
				if (username && username !== null) {
					setSuccessfulLogin(username);
				}
			}, function(e){ // onError
				jQuery.logSystemError('Could not check for authentication status', e, true);
			});
			
			return elem;
			
			/**
			 * Check if the current session is already authenticated.
			 */
			function isAuthenticated(onSuccess, onException) {
				// request sessionId and then check for login status on server
				mySession.getSessionId(function(sessionId){
					// use json-rpc for checking authentication status
					jsonService.user.isAuthenticated({
						params: [sessionId],
						onSuccess: onSuccess,
						onException: onException
					});	
				});
			}
			
			/**
			 * After a successful authentication call this method to update the 
			 * displayed information and provide the logout button.
			 * 
			 * @param username string
			 */
			function setSuccessfulLogin(username) {
				setUserInfo(username);
				// on success replace with username and logout button
				loginClickElem.detach();
				elem.append(displayUsernameElem);
				elem.append(logoutClickElem);
				displayUsernameElem.text('Logged in as: '+username);
				if(globalLoginCallback && jQuery.isFunction(globalLoginCallback)) {
					globalLoginCallback();
				}
			}
			
			/**
			 * Create a dialog for the login process.
			 * 
			 * @returns functions { open() }
			 */
			function LoginDialogPanel() {
				var loginPanel = jQuery('<div></div>');
				loginPanel.append('<div style="padding-bottom:10px">Committing generated terms requires an authorized user. Please log-in using:</div>');
				
				var loginMessagePanel = jQuery('<div style="padding-left:20px;padding-top: 10px;"></div>');
				
				// create the two options
				var browserIdPanel = createBrowserIDPanel(loginMessagePanel);
				
				// use change events to hide details 
				browserIdPanel.inputElem.change(function(){
					browserIdPanel.show();
					loginMessagePanel.empty();
				});
				
				// append components to dialog
				loginPanel.append(browserIdPanel.elem);
				loginPanel.append(loginMessagePanel);
				
				loginPanel.appendTo('body');
				
				// create modal dialog using jQuery
				loginPanel.dialog({
					title: 'Login for TermGenie',
					autoOpen: false,
					width: 450,
					modal: true,
					buttons: {
						"Log In": function() {
							var successCallback = function() {
								closeLoginPanel();
							}
							if (browserIdPanel.inputElem.is(':checked')) {
								browserIdPanel.login(successCallback);
							}
						},
						"Cancel": function() {
							closeLoginPanel();
						}
					}
				});
				
				return {
					open: function() {
						loginPanel.dialog('open');
					}
				}
				
				function closeLoginPanel() {
					loginPanel.dialog( "close" );
					loginMessagePanel.empty();
				}
				
				/**
				 * Create the DOM elements and functions for BrowserID authentication. 
				 * 
				 * @param reporter div appending messages for the user
				 */
				function createBrowserIDPanel(reporter) {
					var elem = jQuery('<div style="padding-bottom:10px"></div>');
					var inputElem = jQuery('<input type="radio" name="LoginDialogPanelInput" value="browserID" checked="checked" />')
					var inputElemDiv = jQuery('<div></div>');
					elem.append(inputElemDiv);
					inputElemDiv.append(inputElem);
					inputElemDiv.append(jQuery('<label>Persona (BrowserID)</label>'));
					
					var detailsDiv = jQuery('<div style="padding-left: 20px;padding-top: 5px;"></div>');
					detailsDiv.append("Clicking on the 'Log In' button will open a new window for the login process using Persona (BrowserID).");
					elem.append(detailsDiv);
					
					return {
						elem: elem,
						inputElem: inputElem,
						login: callBrowserID,
						hide: function(){
							detailsDiv.hide();
						},
						show: function() {
							detailsDiv.show();
						}
					};
					
					/**
					 * Execute RPC call for assertion verification on the server.
					 * 
					 * @param assertion {String}
					 * @param onSuccess function
					 * @param onException function
					 */
					function authenticateBrowserId(assertion, onSuccess, onException) {
						mySession.getSessionId(function(sessionId){
							// use json-rpc for the browserID verification
							jsonService.browserid.verifyAssertion({
								params: [sessionId, assertion],
								onSuccess: onSuccess,
								onException: onException
							});	
						});
					}
					
					/**
					 * Request the Persona (formerly known as BrowserID) via JavaScript method call.
					 * 
					 * @param successCallback function called after successful authentication
					 */
					function callBrowserID(successCallback) {
						reporter.empty();
						reporter.append(createBusyMessage('Calling Persona (BrowserID) in new Window.'));
						// This function comes from https://browserid.org/include.js.
					    // If the user successfully supplies an email address (and thus)
					    // an assertion, we'll send it to our server so we can check it
					    // out and get user data out of it if it's okay.
						navigator.id.get(function(assertion) {
					    	reporter.empty();
					    	if(assertion){
					    		reporter.append(createBusyMessage('Verifying Persona (BrowserID) on Server.'));
					    		authenticateBrowserId(assertion, function(userdata){ // on success
					    			reporter.empty();
									if (userdata && userdata !== null) {
										if(userdata.error && userdata.error !== null) {
											// set error message
											jQuery.logSystemError('Login service call failed', userdata.error);
										}
										else {
											setSuccessfulLogin(userdata.screenname);
										}
										successCallback();
									}
									else {
										jQuery.logSystemError('Persona (BrowserID) verification failed.');
									}
					    		},
					    		function(e){ // on error
					    			reporter.empty();
									jQuery.logSystemError('Persona (BrowserID) login service call failed',e);
					    		});
					    	}else{
					    		// set error message
					    		reporter.append('<div>Login via Persona (BrowserID) not successful.</div>');
					    	}
					    });
					}
				}
			}
		}
	}
	});
	
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
})(jQuery);