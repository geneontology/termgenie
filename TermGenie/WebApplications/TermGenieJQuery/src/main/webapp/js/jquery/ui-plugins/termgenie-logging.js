/*
 * Add two simple logging commands to TermGenie using a dialog element.
 * 
 * Depends:
 * 	jquery.ui.dialog.js
 * 	jquery.ui.tabs.js
 */
(function($){
	var loggingSystem = null;
	$.extend({
		"logSystemError" : function(message, error, hidden) {
			if (loggingSystem == null) {
				loggingSystem = LoggingSystem();
			}
			loggingSystem.logSystemError(message, error, hidden);
		},
		"logUserMessage" : function(message, details) {
			if (loggingSystem == null) {
				loggingSystem = LoggingSystem();
			}
			loggingSystem.logUserMessage(message, details);
		}
	});

	/**
	 * Provide a simple message and error logging system. 
	 * (Only client side logging, no transfer to server)
	 * 
	 * @returns methods for logging
	 */
	function LoggingSystem () {
		
		var popupLoggingPanel = PopupLoggingPanel();
		var dialogBox = DialogBox();
		
		/**
		 * Logging panel in a popup with two kinds of logs: errors and messages.
		 * 
		 * @returns methods for logging (internal)
		 */
		function PopupLoggingPanel() {
			var popupDiv = jQuery('<div></div>');
			popupDiv.appendTo('body');
			var tabTitles = jQuery('<ul></ul>');
			tabTitles.appendTo(popupDiv);
			var errorPanel = createPanel("Error Messages", 300, 'termgenie-logging-tabId-1');
			var messagePanel = createPanel("User Messages", 300, 'termgenie-logging-tabId-2');
			
			popupDiv.dialog({
				autoOpen: false,
				modal: true,
				draggable: true,
				resizable: true,
				minHeight: 450,
				minWidth: 500,
				title: 'Error Logging Console',
				buttons: [{
					text: "Clear",
					click: function() {
						var selected = popupDiv.tabs('option', 'selected');
						if (selected === 0) {
							errorPanel.clear();
						}
						else if (selected === 1) {
							messagePanel.clear();
						}
					}
				},{
					text: "Close Panel",
					click: function() {
						popupDiv.dialog('close');
					}
				}]
			});
			
			// create tabs in popup, using a custom prefix for tabId
			popupDiv.tabs({
				idPrefix: 'termgenie-logging-tabId-'
			});
			
			// register handler for link to show this panel
			jQuery('#termgenie-error-console-link').click(function(){
				popupDiv.dialog('open');
			});
			
			function createPanel(name, maxCount, tabId) {
				tabTitles.append('<li><a href="#'+tabId+'">'+name+'</a></li>');
				var container = jQuery('<div id="'+tabId+'"></div>');
				container.appendTo(popupDiv);
				var contentContainer = jQuery('<div style="overflow: scroll;position:absolute;height:75%;width:90%"></div>');
				container.append(contentContainer);
				return LoggingPanel(contentContainer, maxCount);
			}
			
			return {
				/**
				 * @param message String
				 */
				appendMessage: function(message){
					messagePanel.append(message);
					// do not force popup, as this is also reported via the dialog box
				},
				
				/**
				 * Append an error to the log. Do not show the panel, if hidden is true.
				 * 
				 * @param message String
				 * @param error Exception
				 * @param hidden boolean
				 */
				appendError: function(message, error, hidden) {
					errorPanel.append(message +' \n '+error);
					// force popup, except if hidden is true
					if (!(hidden === true)) {
						// select the error tab
						popupDiv.tabs('select', 0);
						// show error popup
						popupDiv.dialog('open');
					}
				}
			};
		}
		
		/**
		 * A panel for displaying log messages. 
		 * Specify a DOM parent and a message limit.
		 * 
		 * @param parent DOM element
		 * @param maxCount int
		 * @returns methods for logging.
		 */
		function LoggingPanel(parent, maxCount) {
			var count = 0;
			var loggingDiv = jQuery('<div></div>');
			loggingDiv.appendTo(parent);
			
			function getCurrentTime(){
				var date = new Date();
				var timeString = date.getFullYear(); // four digit year
				timeString += '-';
				timeString = leadingZero(timeString, (1 + date.getMonth())); // month (0-11)
				timeString += '-';
				timeString = leadingZero(timeString, date.getDate()); // day in month 1-31
				timeString += ' ';
				timeString = leadingZero(timeString, date.getHours()); // 0-23
				timeString += ':';
				timeString = leadingZero(timeString, date.getMinutes()); // 0-59
				timeString += ':';
				timeString = leadingZero(timeString, date.getSeconds()); // 0-59
				return timeString;
				
				function leadingZero(string, value) {
					if (value < 10) {
						string += '0';
					}
					string += value;
					return string;
				}
			}
			
			return {
				/**
				 * Append a message to the panel. Automatically prepend a timestamp.
				 * If the internal message limit is reached, the oldest message 
				 * will be deleted.
				 * 
				 * @param message String
				 */
				append : function (message) {
					count += 1;
					loggingDiv.append('<div><span class="termgenie-logging-date-time">'+getCurrentTime()+'</span> '+message+'</div>');
					if (count > maxCount) {
						loggingDiv.children().first().remove();
					}
				},
				/**
				 * Clear the panel of all log messages so far.
				 */
				clear : function() {
					count = 0;
					loggingDiv.empty();
				}
			};
		}
		
		/**
		 * Create a dialog box which is aware of the internal logging mechanisms.
		 * 
		 * @returns methods for the dialog box.
		 */
		function DialogBox () {
			var dialogDiv = jQuery('<div></div>');
			dialogDiv.appendTo('body');
			var dialogContent = jQuery('<div></div>');
			dialogContent.appendTo(dialogDiv);
			var moreDetailsDiv = jQuery('<div style="margin:10px;"></div>');
			moreDetailsDiv.appendTo(dialogDiv);
			
			dialogDiv.dialog({
				autoOpen: false,
				modal: true,
				minWidth: 450,
				draggable: true,
				resizable: true,
				title: 'Information',
				buttons: [{
					text: "Ok",
					click: function() {
						dialogDiv.dialog('close');
					}
				}]
			});
			
			return {
				/**
				 * show a dialog with the message and optional details.
				 * 
				 * @param message String
				 * @param details String[]
				 */
				show : function(message, details) {
					// write message also to hidden log
					popupLoggingPanel.appendMessage(message);
					
					// write message to dialog
					moreDetailsDiv.empty();
					dialogContent.empty();
					dialogContent.append(message);
					if (details && details.length > 0) {
						var allDetailsDiv = jQuery('<div style="display:none;overflow:auto;"></div>');
						jQuery.each(details, function(index, detail){
							allDetailsDiv.append('<div style="padding:5px 5px;">'+detail+'</div>');
						});
						
						var moreDetailsButton = jQuery('<span class="myClickable" style="font-size:0.8em;">Show Details</span>');
						moreDetailsButton.click(function(){
							if (allDetailsDiv.is(":visible")) {
								allDetailsDiv.hide();
								moreDetailsButton.text('Show Details');
							}
							else {
								allDetailsDiv.show();
								moreDetailsButton.text('Hide Details');
							}
						});
						
						moreDetailsButton.appendTo(moreDetailsDiv);
						allDetailsDiv.appendTo(moreDetailsDiv);
					}
					dialogDiv.dialog('open');
				} 
			}
		}
		
		return {
			/**
			 * Log an error to the error panel.
			 * 
			 * @param message String
			 * @param error Exception
			 * @param hidden boolean, if hidden is true, do not open logging panel.
			 */
			logSystemError : function(message, error, hidden) {
				popupLoggingPanel.appendError(message, error, hidden);
			},
			/**
			 * Log and show a message to the user.
			 * 
			 * @param message String
			 * @param details String[] 
			 */
			logUserMessage : function(message, details) {
				dialogBox.show(message, details);
			}
		};
	}
})(jQuery);