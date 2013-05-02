/*
 * Login panel which is added to the top right corner of the TermGenie 
 * web-site. Uses Persona (BrowserID) or OpenID for authentication (Both authentication 
 * methods require at least one counterpart rpc method on the server).
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
				loginDialogPanel.open();
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
				loginPanel.append('<div style="padding-bottom:10px">Committing generated terms requires an authorized user. Please log-in using one of the following options:</div>');
				
				var loginMessagePanel = jQuery('<div style="padding-left:20px;padding-top: 10px;"></div>');
				
				// create the two options
				var browserIdPanel = createBrowserIDPanel(loginMessagePanel);
				var openIDPanel = createOpenIDPanel(loginMessagePanel);
				
				// use change events to hide details 
				browserIdPanel.inputElem.change(function(){
					openIDPanel.hide();
					browserIdPanel.show();
					loginMessagePanel.empty();
				});
				
				openIDPanel.inputElem.change(function(){
					browserIdPanel.hide();
					openIDPanel.show();
					loginMessagePanel.empty();
				});
				
				// append components to dialog
				loginPanel.append(browserIdPanel.elem);
				loginPanel.append(openIDPanel.elem);
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
							else if (openIDPanel.inputElem.is(':checked')) {
								openIDPanel.login(successCallback);
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
				
				/**
				 * Create the DOM elements and functions for OpenID authentication. 
				 * 
				 * @param reporter div appending messages for the user
				 */
				function createOpenIDPanel(reporter) {
					var elem = jQuery('<div></div>');
					var inputElem = jQuery('<input type="radio" name="LoginDialogPanelInput" value="openID" />')
					var inputElemDiv = jQuery('<div></div>');
					elem.append(inputElemDiv);
					inputElemDiv.append(inputElem);
					inputElemDiv.append(jQuery('<label>OpenID</label>'));
					
					var detailsDiv = jQuery('<div style="padding-left: 20px;padding-top: 5px;"></div>');
					elem.append(detailsDiv);
					detailsDiv.append('<div style="padding-bottom: 5px;>Enter your OpenID:</div>');
					var openIDInput = ParameterizedTextFieldInput(detailsDiv, 'text', "OpenID", 4);
					detailsDiv.append('<div style="padding-top: 5px;">Warning: As the login process with OpenID involves a redirect to the OpenID server, your current state of termgenie will be lost in this process.</div>');
				
					detailsDiv.hide();
					
					return {
						elem: elem,
						inputElem: inputElem,
						login: callOpenIDLogin,
						hide: function(){
							detailsDiv.hide();
						},
						show: function() {
							detailsDiv.show();
						}
					};
					
					/**
					 * Request the OpenID via RPC method call. Will redirect the page to the 
					 * OpenID server, if an OpenID server has been discovered from the given 
					 * openID.
					 * 
					 * @param successCallback function called after successful authentication
					 */
					function callOpenIDLogin(successCallback) {
						reporter.empty();
						var openIDCheck = openIDInput.check();
						if (openIDCheck.success !== true) {
							renderLoginErrors(openIDCheck.message);
							return success;
						}
						var openID = openIDCheck.value;
						reporter.append(createBusyMessage('Calling OpenID'));
						loginOpenID(openID, function(result){ // onSuccess
							reporter.empty();
							if (result && result !== null) {
								if(result.error && result.error !== null) {
									// set error message
									jQuery.logSystemError('Login service call failed', result.error);
									return;
								}
								else if (result.url && result.url !== null) {
									// successfull discovery of OpenID authority
									// redirect page for authentication
									// The openID server will redirect the user to the 
									// termgenie site (stage two of OpenID protocoll handled in a servlet) 
									// after a successfull authentication for the given openID.
									if (result.parameters === null) {
										window.location.href = result.url;
									}
									else {
										// use form to create post request
										var redirectForm = jQuery('<form action="'+result.url+'" method="post" accept-charset="utf-8"></form>');
										jQuery.each(result.parameters, function(index, value){
											redirectForm.append('<input type="hidden" name="'+value[0]+'" value="'+value[1]+'"/>');	
										});
										redirectForm.appendTo(loginMessagePanel);
										redirectForm.submit();
									}
								}
								successCallback();
							}
							else {
								// set error message
								loginMessagePanel.append('<div>Login via OpenID not successful, please check the specified openID.</div>');
							}
						},
						function(e){ // onException
							reporter.empty();
							jQuery.logSystemError('OpenID login service call failed', e);
						});
					}
					
					/**
					 * Execute RPC call to server for stage one of OpenID protocoll.
					 * 
					 * @param username
					 * @param onSuccess
					 * @param onException
					 */
					function loginOpenID(username, onSuccess, onException) {
						// request sessionId and then start a login via openID on server
						mySession.getSessionId(function(sessionId){
							// use json-rpc for authentication of the session
							jsonService.openid.authRequest({
								params: [sessionId, username],
								onSuccess: onSuccess,
								onException: onException
							});	
						});
					}
					
					/**
					 * Helper function to render an login error message popup.
					 * 
					 * @param message1
					 * @param message2
					 */
					function renderLoginErrors(message1, message2) {
						var details =[]
						if(message1 && message1.length > 0) {
							details.push(message1);
						}
						if(message2 && message2.length > 0) {
							details.push(message2);
						}
						jQuery.logUserMessage("Unable to login", details);
					}
					
					/**
					 * Wrapper around an input field with minimalistic validation and error state.
					 * 
					 * @param elem parent DOM element
					 * @param type type of the input field, e.g., text or password
					 * @param name the name of the input field. To be used in messages. 
					 * @param minchars number of chars to present to be seen as valid input
					 * @returns methods for checking and retrieving the input field
					 */
					function ParameterizedTextFieldInput(elem, type, name, minchars) {
						var inputElement = jQuery('<input type="'+type+'"/>');
						inputElement.css('width','350');
						elem.append(inputElement);
						
						inputElement.change(function(){
							clearErrorState();
						});
						
						function clearErrorState(){
							inputElement.removeClass('termgenie-input-field-error');	
						}
						
						function setErrorState() {
							inputElement.addClass('termgenie-input-field-error');
						}
						
						return {
							/**
							 * check the current value of the input field.
							 * 
							 * @returns {
							 * 		success: boolean,
							 * 		message: String,
							 * 		value: String
							 * }
							 */
							check : function() {
								var success = false;
								var message = undefined;
								var value = undefined;
								clearErrorState();
								var text = inputElement.val();
								if (text && text.length > 0) {
									if (text.length >= minchars) {
										success = true;
										value = text;
									}
									else {
										setErrorState();
										message = name+' is too short. The '+name+' consits of at least '+minchars+' characters';
									}
								}
								else {
									setErrorState();
									message = name+' is empty. Please specifiy the '+name+' to login.';
								}
								return {
									success: success,
									message: message,
									value: value
								};
							},
							/**
							 * allow to clear the value
							 */
							clear: function() {
								inputElement.val('');
							}
						};
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