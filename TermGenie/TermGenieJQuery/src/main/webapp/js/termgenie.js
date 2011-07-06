function termgenie(){
	/**
	 * Provide an Accordion with the additional functionality to 
	 * enable/disable individual panes for click events.
	 * 
	 * @param id html-id for the accordian div tag
	 * 
	 * @returns methods for the accordion
	 */
	function MyAccordion(id) {
		// private variables;
		var selections = {};
		selections.Pane_0 = true;
		selections.Pane_1 = false;
		selections.Pane_2 = false;
		selections.Pane_3 = false;
		
		jQuery(id).accordion({ clearStyle: true, autoHeight: false, event: "" });
		
		// implement a custom click function
		// allow only to open panes, which are enabled in the selections object
		jQuery(id+' h3').click(function() {
			var idx = jQuery(id+' h3').index(this);
			var activate = selections["Pane_" + idx];
			if (activate) {
				jQuery(id).accordion("activate", idx);
			}
		});
		
		return {
			/**
			 * Active the specified panel.
			 * 
			 * @param pos postion to activate (zero-based)
			 */
			activatePane : function(pos) {
				jQuery(id).accordion("activate", pos);
			},
			
			/**
			 * Set the status of a pane.
			 * 
			 * @param pos postion to activate (zero-based)
			 * @param state boolean
			 */
			setPaneState : function(pos, state) {
				selections["Pane_" + pos] = state;
			},
		
			/**
			 * Enable a pane for click events.
			 * 
			 * @param pos postion to enable (zero-based)
			 */
			enablePane : function(pos) {
				selections["Pane_" + pos] = true;
			},
			
			/**
			 * Disable a pane for click events.
			 * 
			 * @param pos postion to disable (zero-based)
			 */
			disablePane : function(pos) {
				selections["Pane_" + pos] = false;
			}
		};
	};
	
	/**
	 * Handle all elements of the login and user managmenent process.
	 * 
	 * @returns methods for the login panel
	 */
	function LoginPanel() {
		
		var panel = createLoginPanel();
		var userInfo = null;
		
		function login(username, password) {
			userinfo = {
				username: username,
				password: password
			}
		}
		
		function logout() {
			userInfo = null;
		}
		
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
			 * Retrieve the username and password for commiting.
			 * Only defined, if the user is logged in.
			 * 
			 * @returns { username, password }
			 */
			getCredentials: function() {
				if (userInfo !== null) {
					return {
						username : userInfo.username,
						password : userInfo.password
					}
				}
				return {};
			}
		};
		
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
			
			var loginPanel = jQuery('<div></div>');
			loginPanel.append('<div style="padding-bottom:5px">To commit terms a login is required.</div>');
			var table = createLayoutTable();
			table.addClass('termgenie-login-panel-layout-table');
			table.appendTo(loginPanel);
			
			// username
			var row1 = jQuery('<tr></tr>');
			row1.appendTo(table);
			row1.append('<td>Username:</td>');
			var tablecell1 = jQuery('<td></td>');
			tablecell1.appendTo(row1);
			var usernameInput = ParameterizedTextFieldInput(tablecell1, 'text', "Username", 4);
			
			// password
			var row2 = jQuery('<tr></tr>');
			row2.appendTo(table);
			row2.append('<td>Password:</td>');
			var tablecell2 = jQuery('<td></td>');
			tablecell2.appendTo(row2);
			var passwordInput = ParameterizedTextFieldInput(tablecell2, 'password', "Password", 6);
			
			loginPanel.appendTo('body');
			
			var loginMessagePanel = jQuery('<div></div>');
			loginMessagePanel.appendTo(loginPanel);
			
			loginPanel.dialog({
				title: 'Login for TermGenie',
				autoOpen: false,
				height: 250,
				width: 350,
				modal: true,
				buttons: {
					"Log In": function() {
						loginMessagePanel.empty();
						var usernameCheck = usernameInput.check();
						var passwordCheck = passwordInput.check();
						if (!(usernameCheck.success === true) || !(passwordCheck.success === true)) {
							renderLoginErrors(usernameCheck.message, passwordCheck.message);
							return;
						}
						
						// TODO execute login
						// on success replace with username and logout button
						var username = usernameCheck.value;
						var password = passwordCheck.value;
						login(username, password);
						loginClickElem.detach();
						elem.append(displayUsernameElem);
						elem.append(logoutClickElem);
						displayUsernameElem.text('Logged in as: '+username);
						
						closeLoginDialog();
					},
					"Cancel": function() {
						closeLoginDialog();
					}
				}
			});
			
			loginClickElem.click(function(){
				loginPanel.dialog('open');
			});
			
			logoutClickElem.click(function(){
				logout();
				logoutClickElem.detach();
				displayUsernameElem.detach();
				elem.append(loginClickElem);
			});
			
			return elem;
			
			function closeLoginDialog() {
				loginPanel.dialog( "close" );
				loginMessagePanel.empty();
			}
			
			function renderLoginErrors(message1, message2) {
				var details =[]
				if(message1 && message1.length > 0) {
					details.push(message1);
				}
				if(message2 && message2.length > 0) {
					details.push(message2);
				}
				loggingSystem.logUserMessage("Unable to login", details);
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
					}
				};
			}
		}
	}
	
	//create proxy for json rpc
	var jsonService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: true,
	    methods: ['generate.availableTermTemplates', 
	              'generate.generateTerms', 
	              'ontology.availableOntologies',
	              'ontology.autocomplete',
	              'commit.isValidUser',
	              'commit.exportTerms',
	              'commit.commitTerms']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonService, true);
	
	// Logging and user messages
	var loggingSystem = LoggingSystem ();
	
	// global elements for this application
	var myAccordion = MyAccordion('#accordion');
	var myLoginPanel = LoginPanel(); 
	var myUserPanel = createUserPanel();
	
	// create busy icon and message to show during wait
	jQuery('#div-select-ontology').append(createBusyMessage('Quering for available ontologies at the server.'));
	
	// use json-rpc to retrieve available ontologies
	jsonService.ontology.availableOntologies({
		onSuccess: function(result) {
			/*
			 * Actual start code for the page.
			 * 
			 * Retrieve and create the content for the step 1. 
			 */
			createOntologySelector(result);
		},
		onException: function(e) {
			jQuery('#div-select-ontology').empty();
			loggingSystem.logSystemError('AvailableOntologies service call failed',e);
			return true;
		}
	});
	
	/**
	 * Create a selector for the given list of ontologies.
	 * 
	 * Side conditions: 
	 *   - assumes the list to be non-empty, 
	 *   - if ontologies.length === 1, skip selection menu and go to next step 
	 * 
	 * @param ontologies list of ontology names
	 */
	function createOntologySelector(ontologies) {
		var selectedValue;
		var ontselect;
		var elem;
		// create selector for given ontologies
		if (ontologies.length === 1) {
			// short cut, if only one exists, skip this step
			selectedValue = ontologies[0];
			setStep1Header(selectedValue);
			createTemplateSelector(selectedValue);
			// go to the next panel and deactivate the first panel
			setStep2Active(false);
		} else {
			ontselect = c_span('select-ontology-header','Available Ontologies') +
			'<select id="select-ontology-select">';
			jQuery.each(ontologies, function(intIndex, objValue){
				ontselect += '<option value="'+objValue+'">'+objValue+'</option>';
			});
			ontselect += '</select>'+ c_button('select-ontology-button', 'Submit');
			
			// add to div
			elem = jQuery('#div-select-ontology');
			elem.empty();
			elem.append(ontselect);
			
			// register click handler
			jQuery('#select-ontology-button').click(function() {
				var selectedValue = jQuery('#select-ontology-select').val();
				setStep1Header(selectedValue);
				createTemplateSelector(selectedValue);
				setStep2Active(true);
			});
		}
		
		/**
		 * Set the selected ontology name in the header.
		 * 
		 * @param ontology selected ontology name
		 */
		function setStep1Header(ontology) {
			var elem = jQuery('#span-step1-additional-header');
			elem.empty();
			elem.append('<span class="step1-additional-header">'+ontology+'</span>');
		}
		
		/**
		 * Active the second step. Depending on the parameter, the pane 
		 * for step 1 can be revisited.
		 * 
		 * @param step1Available boolean
		 */
		function setStep2Active(step1Available) {
			myAccordion.setPaneState(0, step1Available);
			myAccordion.enablePane(1);
			myAccordion.activatePane(1);
		}
	}
	
	/**
	 * Variable for keeping the template widgets 
	 */
	var termTemplateWidgetList = null;
	
	/**
	 * Create the menu for selecting term generation templates. 
	 * 
	 * @param ontology selected ontology name
	 */
	function createTemplateSelector(ontology) {
		// create general layout
		var termselect = c_div('div-template-selector', 
				c_span('select-template-header','Select Template'))+
				c_div('div-all-template-parameters','');
		
		// get intented dom element
		var elem = jQuery('#div-select-templates-and-parameters');
		// clear, from previous templates
		elem.empty();
		
		// clear header from previous templates
		jQuery('#span-step2-additional-header').empty();
		
		// set busy message
		elem.append(createBusyMessage('Quering for available termplates at the server.'));
		
		// start async rpc to retrieve available templates
		jsonService.generate.availableTermTemplates({
			params:[ontology],
			onSuccess: function(result) {
				// clear busy message
				elem.empty();
				// append layout
				elem.append(termselect);
				
				termTemplateWidgetList = TermTemplateWidgetList(result);
				createTemplateSelectorMenu(ontology, result);
				registerTermGenerationButton();
			},
			onException: function(e) {
				elem.empty();
				loggingSystem.logSystemError('AvailableTermTemplates service call failed',e);
				return true;
			}
		});
		
		function registerTermGenerationButton() {
			// register click handler for term generation button
			var submitButton = jQuery('#button-termgeneration-start');
			var busyElement= jQuery('#button-termgeneration-start-progress');
			submitButton.click(function(){
				busyElement.empty();
				var status = termTemplateWidgetList.getAllTemplateParameters();
				var success = status.extractionResult.isSuccessful()
				if (success === false) {
					var details = [];
					jQuery.each(status.extractionResult.getErrors(), function(index, error){
						var message = '';
						if (error.template) {
							message += 'The template <span class="nobr">\'';
							message += getTemplateName(error.template);
							if (error.field) {
								message += '\'</span> in field <span class="nobr">\''+error.field.name;
							}
							message += '\'</span> has the following error:<br/>';
						}
						message += error.message;
						details[index] = message;
					});
					var logMessage = 'Verification failed. There ';
					if (details.length === 1) {
						logMessage += 'is one error. ';
					}
					else {
						logMessage += 'are ' + details.length + ' errors. ';
					}
					logMessage += 'Please check the marked fields.';
					loggingSystem.logUserMessage(logMessage, details);
					return;
				}
				if (status.parameters.length === 0) {
					loggingSystem.logUserMessage('Please select a template from the menu, and click on add template. '+
						'Provide details for the required fields and click on the "'+
						submitButton.text()+
						'"-Button again, to proceed to the next step.');
					return;
				}
				busyElement.append(createBusyMessage('Verifing your request and generating terms on the server.'));
				setStep2HeaderInfo(status.parameters);
				jsonService.generate.generateTerms({
					params:[ontology, status.parameters],
					onSuccess: function(result) {
						busyElement.empty();
						renderStep3(result, ontology);
					},
					onException: function(e) {
						busyElement.empty();
						loggingSystem.logSystemError("GenerateTerms service call failed", e);
						return true;
					}
				});
			});
		}
		
		function setStep2HeaderInfo(parameters) {
			var header = jQuery('#span-step2-additional-header');
			header.empty();
			var map = {};
			for ( var i = 0; i < parameters.length; i++) {
				var name = parameters[i].termTemplate.name;
				if(!map[name]) {
					map[name] = 1;
				}
				else{
					map[name] = 1 + map[name];
				}
			}
			var headerInfo = '';
			jQuery.each(map, function(key, value) {
				if (headerInfo.length > 0) {
					headerInfo += ', ';
				}
				headerInfo += key + ' (' + value + ')';
			});
			header.append(headerInfo);
		}
		
		function renderStep3(generationResponse, ontology) {
			createResultReviewPanel(generationResponse, ontology);
			myAccordion.enablePane(2);
			myAccordion.activatePane(2);	
		}
	}
	
	/**
	 * Create the selection menu for the list of templates
	 * 
	 * @param ontology ontology name
	 * @param templates list of term templates
	 */
	function createTemplateSelectorMenu(ontology, templates) {
		// create layout
		jQuery('#div-template-selector')
			.append('<select id="select-add-template-select"></select>'+
				c_button('button-add-template-select', 'Add template'));
		
		// select dom element
		var domElement = jQuery('#select-add-template-select');

		// foreach template create a menu entry, use index for retrieval
		jQuery.each(templates, function(intIndex, objValue) {
			var templateName = getTemplateName(objValue);
			var option = jQuery('<option />');
			option.text(templateName);
			option.val(intIndex);
			domElement.append(option);
		});
		
		// click handler for adding a selected template
		jQuery('#button-add-template-select').click(function (){
			var intIndex = jQuery('#select-add-template-select').val();
			termTemplateWidgetList.addTemplate(templates[intIndex]);
		});
	}
	
	/**
	 * Holder of all template widgets. Provides methods for adding 
	 * and removing of widgets.
	 * 
	 * @param templates list of all templates for this widget
	 */
	function TermTemplateWidgetList(templates){
		// private members
		var templateMap = {};
		
		// private methods
		function createTemplateSubList(template, id, wrapperId) {
			var templateContainer = jQuery('<div id="'+wrapperId+'" class="template-list-wrapper"></div>');
			templateContainer.appendTo(jQuery('#div-all-template-parameters'));
			var templateDisplay = getTemplateName(template);
			var templateTitle = jQuery('<div class="termgenie-template-header">Template: <span class="label-template-name">'+templateDisplay+'</span></div>');
			createAddRemoveWidget(templateTitle, 
					function(){
						privateAddTemplate(template);
					}, 
					function(){
						privateRemoveTemplate(template);
					});
			
			templateContainer.append(templateTitle);
			if (template.description && template.description.length > 0) {
				templateContainer.append('<div class="termgenie-term-template-description">Description: <span class="description-content">'+template.description+'</span></div>');
			}
			if (template.hint && template.hint.length > 0) {
				templateContainer.append('<div class="termgenie-term-template-hint">Hint: <span class="hint-content">'+template.hint+'</span></div>');
			}
			templateContainer.append('<div id="'+id+'"></div>');
		}
		
		function privateAddTemplate(template) {
			var templateListContainer = templateMap[template.name];
			if (!templateListContainer) {
				templateListContainer = {
					count : 0,
					list : new Array(),
					id : 'div-all-template-parameters-'+template.name,
					wrapperId : 'div-all-template-parameters-wrapper-'+template.name
				}
				createTemplateSubList(template, templateListContainer.id, templateListContainer.wrapperId);
				templateMap[template.name] = templateListContainer;
			}
			var templateWidget = TermTemplateWidget(template,templateListContainer.count);
			templateListContainer.list[templateListContainer.count] = templateWidget;
			
			var listElem = jQuery('#'+templateListContainer.id);
			
			// if the list was empty create the layout 
			if (templateListContainer.count === 0) {
				templateWidget.createLayout(listElem);
			}
			// append a new line to the list
			var domId = templateListContainer.id+'-'+templateListContainer.count;
			templateWidget.appendLine(listElem, domId);
			
			templateListContainer.count += 1;
		}
		
		function privateRemoveTemplate(template) {
			var templateListContainer = templateMap[template.name];
			if (templateListContainer) {
				if (templateListContainer.count > 1) {
					var pos = templateListContainer.count - 1;
					var domId = templateListContainer.id+'-'+pos;
					jQuery('#'+domId).remove();
					templateListContainer.list[pos] = undefined;
					templateListContainer.count = pos;
				}
				else {
					jQuery('#'+templateListContainer.wrapperId).remove();
					templateMap[template.name] = undefined;
				}
			}
		}
		
		return {
			//public methods
			
			/**
			 * add a template to the widget list. Group the templates by 
			 * template name. Order the list by creation of the groups, 
			 * not by name.
			 */
			addTemplate : function (template) {
				privateAddTemplate(template);
			},
			
			/**
			 * remove a template. When the last template of a group is 
			 * removed, the empty group is also removed.
			 */
			removeTemplate : function(template) {
				privateRemoveTemplate(template);
			},
			
			/**
			 * extract and validate all input fields for the requested 
			 * templates.
			 * 
			 * return object {
			 *    extractionResult: ExtractionResult
			 *    parameters: JsonTermGenerationInput{
			 *       termTemplate: JsonTermTemplate,
			 *       termGenerationParameter: JsonTermGenerationParameter
			 *    }[]
			 * }
			 */
			getAllTemplateParameters : function() {
				var extractionResult = ExtractionResult();
				var parameters = [];
				jQuery.each(templateMap, function(name, listcontainer){
					if (listcontainer && listcontainer.list) {
						var list = listcontainer.list;
						jQuery.each(list, function(index, templateWidget){
							var extracted = templateWidget.extractTermGenerationInput(extractionResult);
							if(extracted && extracted.success === true) {
								parameters.push(extracted.input);
							}
						});
					}
				});
				return {
					extractionResult : extractionResult,
					parameters: parameters
				}
			}
		};
	};
	
	/**
	 * Keep all the information for one template and functions to render it.
	 * 
	 * @param template termgeneration template
	 * @param id internal id number
	 */
	function TermTemplateWidget(template, id) {
		var templateFields = template.fields;
		var inputFields = [];
		
		return {
			/**
			 * get internal id, used to distinguish 
			 * between templates with the same name.
			 * 
			 * return id
			 */
			getId : function() {
				return id;
			},
			
			/**
			 * create layout for this template, including header and footer
			 * 
			 * @param elem parent dom element
			 */
			createLayout : function(elem) {
				var i; 		// define here as there is only function scope
				var field;	// define here as there is only function scope
				
				var layout = createLayoutTableOpenTag()+'<thead>';
				
				var firstRow = '<tr><td>Required</td>';
				var secondRow = '<tr>';
				var thirdRow = '<tr>';
				
				
				var first = true;
				for (i = 0; i < templateFields.length; i+=1) {
					field = templateFields[i];
					if (i > 0) {
						// write top level requirements	
						if (first && field.required === false) {
							first = false;
							firstRow += '<td>Optional</td>';
						} else {
							firstRow += '<td></td>';
						}
					}
					// write field names
					secondRow += '<td><span class="termgenie-term-template-field-name">'+field.name+'</span></td>';
					thirdRow += '<td><span class="termgenie-term-template-field-ontologies">';
					if (field.ontologies && field.ontologies.length > 0) {
						jQuery.each(field.ontologies, function(index, ontology){
							if (index > 0) {
								thirdRow += ', ';
							}
							thirdRow += getShortOntologyName(ontology);
						});
					}
					thirdRow += '</span></td>';
				}
				firstRow += '</tr>';
				secondRow += '</tr>';
				thirdRow += '</tr>';
				
				
				
				// write empty body and footer
				layout += firstRow + secondRow + thirdRow + '</thead><tbody></tbody></table>';
				
				elem.append(layout);
			},
			
			/**
			 * Append the term template in layout format, including all functionality.
			 * 
			 * @param elem parent dom element 
			 * @param domId the unique id for this line
			 */
			appendLine : function(elem, domId) {
				// define variable here as there is only function scope
				var i;
				var field;
				var tdElement;
				
				var trElement = jQuery('<tr id="'+domId+'"></tr>');
				elem.find('tbody').first().append(trElement);
				
				for (i = 0; i < templateFields.length; i+=1) {
					field = templateFields[i];
					trElement.append('<td></td>');
					tdElement = trElement.children().last();
					
					if (field.ontologies && field.ontologies.length > 0) {
						var cardinality = field.cardinality;
						if (cardinality.min === 1 && cardinality.max === 1) {
							var prefixes = field.functionalPrefixes;
							if (prefixes && prefixes.length > 0) {
								inputFields[i] = AutoCompleteOntologyInputPrefix(tdElement, i, field.ontologies, prefixes);
							}
							else {
								inputFields[i] = AutoCompleteOntologyInput(tdElement, i, field.ontologies);	
							}
						}
						else {
							inputFields[i] = AutoCompleteOntologyInputList(tdElement, i, field.ontologies, cardinality.min, cardinality.max);
						}
					}
					else {
						var cardinality = field.cardinality;
						if (cardinality.min === 1 && cardinality.max === 1) {
							inputFields[i] = TextFieldInput(tdElement, i);
						}
						else {
							var validator = undefined;
							if (field.name == 'DefX_Ref') {
								validator = function(text, template, field, extractionResult) {
									if(!text || text.length < 3) {
										extractionResult.addError('The field "'+field.name+'" is too short. Def_XRef consists of a prefix and suffix with a colon (:) as separator', template, field);
										return false;
									}
									var pattern = /^\S+:\S+$/; // {non-whitespace}+ colon {non-whitespace}+ [whole string]
									var matching = pattern.test(text); 
									if (matching === false) {
										extractionResult.addError('The field "'+field.name+'" does not conform to the expected pattern. Def_XRef consists of a prefix and suffix with a colon (:) as separator and no whitespaces', template, field);
									}
									return matching;
								};
							}
							inputFields[i] = TextFieldInputList(tdElement, i, cardinality.min, cardinality.max, validator);
						}
					}
				}
			},
			
			/**
			 * extract and validate all input fields for this template.
			 * 
			 * return object {
			 *    success: boolean
			 *    input: JsonTermGenerationInput{
			 *       termTemplate: JsonTermTemplate,
			 *       termGenerationParameter: JsonTermGenerationParameter
			 *    }
			 * }
			 */
			extractTermGenerationInput : function(extractionResult) {
				var success = true;
				var parameter = {
						terms:    [],
						strings:  [],
						prefixes: []
					};
				initSubArrays(parameter.terms, templateFields.length);
				initSubArrays(parameter.strings, templateFields.length);
				initSubArrays(parameter.prefixes, templateFields.length);
				
				for ( var i = 0; i < templateFields.length; i++) {
					var templatefield = templateFields[i];
					var inputField =  inputFields[i];
					var csuccess = inputField.extractParameter(parameter, template, templatefield, 0, extractionResult);
					success = success && csuccess;
				}
				return {
					success: success,
					input: {
						termTemplate: template,
						termGenerationParameter: parameter
					}
				};
				
				function initSubArrays(array, count) {
					for(var j = 0; j < count; j += 1) {
						array[j] = [];
					}
				}
			}
		};
	}
	
	function ExtractionResult() {
		var success = true;
		var errors = [];
		
		return {
			addError : function(error, template, field) {
				success = false;
				errors.push({
					message: error,
					template: template,
					field: field
				});
			},
			isSuccessful: function() {
				return success;
			},
			getErrors: function() {
				return errors;
			}
		};
	}
	
	function TextFieldInput(elem, templatePos, validator) {
		var inputElement = jQuery('<input type="text"/>'); 
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
			extractParameter : function(parameter, template, field, pos, extractionResult) {
				var success;
				clearErrorState();
				if (!pos) {
					pos = 0;
				}
				var text = inputElement.val();
				if (text !== null && text.length > 0) {
					if (validator) {
						success = validator(text, template, field, extractionResult);
						if(success === false) {
							setErrorState();
							return false;
						}
					}
					var list = parameter.strings[templatePos];
					if (!list) {
						list = [];
						parameter.strings[templatePos] = list;
					}
					list[pos] = text;
					return true;
				}
				success = (field.required === false);
				if (success === false) {
					extractionResult.addError("Required value missing", template, field);
					setErrorState();
				}
				return success;
			}
		};
	}
	
	function TextFieldInputList(container, templatePos, min, max, validator) {
		
		var list = [];
		var listParent = createLayoutTable();
		listParent.appendTo(container);
		for ( var i = 0; i < min; i+= 1) {
			appendInput();
		}
		createAddRemoveWidget(container, appendInput, removeInput);
		
		function appendInput() {
			if (list.length <  max) {
				var listElem = jQuery('<tr></tr>');
				listElem.appendTo(listParent);
				var tdElement = jQuery('<td></td>');
				tdElement.appendTo(listElem);
				var inputElem = TextFieldInput(tdElement, templatePos, validator);  
				list.push(inputElem);
			}
		}
		
		function removeInput() {
			if (list.length > min) {
				listParent.find('tr').last().remove();
				list.pop();
			}
		}
		
		return {
			extractParameter : function(parameter, template, field, pos, extractionResult) {
				var success = true;
				jQuery.each(list, function(index, inputElem){
					if (inputElem) {
						var csuccess = inputElem.extractParameter(parameter, template, field, index, extractionResult);
						success = success && csuccess;
					}
				});
				return success;
			}
		};
	}
	
	function AutoCompleteOntologyInput(elem, templatePos, ontologies) {
		
		var term = undefined;
		var inputElement = jQuery('<input/>');
		elem.append(inputElement);
		var descriptionDiv = null;
		
		function clearErrorState() {
			inputElement.removeClass('termgenie-input-field-error');	
		}
		
		function setErrorState() {
			inputElement.addClass('termgenie-input-field-error');
		}
		
		function updateDescriptionDiv(ofElement) {
			var w = ofElement.outerWidth();
			if (w < 400) {
				w = 400;
			}
			var h = ofElement.outerHeight();
			if (h < 200) {
				h = 200;
			}
			if (descriptionDiv === null) {
				descriptionDiv = jQuery('<div><div class="term-description-content"></div></div>')
					.addClass( 'ui-widget-content ui-autocomplete ui-corner-all' )
					.css({
						'width': w,
						'height': h
					})
					.appendTo('body');
				descriptionDiv.resizable({
					minHeight: h,
					minWidth: w
				});
//				descriptionDiv.draggable();
			}
			else {
				descriptionDiv.resizable( "option", "minHeight", h );
				descriptionDiv.resizable( "option", "minWidth", w );
			}
			descriptionDiv.position({
				my: 'left top',
				at: 'right top',
				of: inputElement.autocomplete('widget'),
				collision: 'none none'
			});
		}
		
		function removeDescriptionDiv() {
			if (descriptionDiv !== null) {
				descriptionDiv.removeClass('ui-autocomplete-input');
				descriptionDiv.remove();
				descriptionDiv = null;
			}
		}
		
		function setContentDescriptionDiv(item) {
			var content = descriptionDiv.children().first();
			content.empty();
			var layout = createLayoutTableOpenTag();
			layout += '<tr><td>Ontology</td><td>'+getOntologyName(item.identifier.ontology)+'</td></tr>';
			layout += '<tr><td>Label</td><td>'+item.label+'</td></tr>';
			layout += '<tr><td>Identifier</td><td>'+item.identifier.termId+'</td></tr>';
			if (item.description && item.description.length > 0) {
				layout += '<tr><td>Description</td><td>'+item.description+'</td></tr>';
			}
			if (item.synonyms && item.synonyms.length > 0) {
				layout += '<tr><td>Synonyms</td><td>';
				for ( var i = 0; i < item.synonyms.length; i++) {
					var synonym = item.synonyms[i];
					if (synonym && synonym.length > 0) {
						if (i > 0) {
							layout += '<br/>';
						}
						layout += synonym;
					}
				}
				layout += '</td></tr>';
			}
			layout += '</table>'; 
			content.append(layout);
		}
		
		// used to prevent race conditions
		var requestIndex = 0;
		
		inputElement.autocomplete({
			minLength: 3,
			source: function( request, response ) {
				removeDescriptionDiv();
				var term = request.term;
				requestIndex += 1;
				var myRequestIndex = requestIndex;
				
				jsonService.ontology.autocomplete({
					params:[term, ontologies, 5],
					onSuccess: function(data) {
						if (myRequestIndex === requestIndex) {
							if (data !== null || data.length > 0) {
								response(data);	
							}
							else {
								response([]);
							}
						}
					},
					onException: function(e) {
						loggingSystem.logSystemError('Autocomplete service call failed', e, true);
						if (myRequestIndex === requestIndex) {
							response([]);
						}
					}
				});
			},
			select : function(event, ui) {
				clearErrorState();
				inputElement.val(ui.item.label);
				term = ui.item;
				removeDescriptionDiv();
				return false;
			},
			focus : function(event, ui) {
				inputElement.val(ui.item.label);
				updateDescriptionDiv(inputElement.autocomplete('widget'));
				setContentDescriptionDiv(ui.item);
				return false;
			},
			close : function(event, ui) {
				removeDescriptionDiv();
			} 
		})
		.data( 'autocomplete' )._renderItem = function( ul, item ) {
			return jQuery( '<li class="termgenie-autocomplete-menu-item"></li>' )
				.data( 'item.autocomplete', item )
				.append( '<a><span class="termgenie-autocomplete-menu-item-label">' + 
						item.label + '</span></a>' )
				.appendTo( ul );
		};

		
		
		return {
			extractParameter : function(parameter, template, field, pos, extractionResult) {
				clearErrorState();
				if (!pos) {
					pos = 0;
				}
				if (term && term !== null) {
					var text = inputElement.val();
					if (term.label == text) {
						var identifier = term.identifier;
						var list = parameter.terms[templatePos];
						if (!list) {
							list = [];
							parameter.terms[templatePos] = list;
						}
						list[pos] = identifier;
						return true;
					}
				}
				extractionResult.addError('No valid term. Please specify a term from '+
						getShortOntologyNameList(field.ontologies), template, field);
				setErrorState();
				return false;
			}
		};
	}
	
	function AutoCompleteOntologyInputList(container, templatePos, ontologies, min, max) {
		
		var list = [];
		var listParent = createLayoutTable();
		listParent.appendTo(container);
		for ( var i = 0; i < min; i++) {
			appendInput();
		}
		createAddRemoveWidget(container, appendInput, removeInput);
		
		function appendInput() {
			if (list.length <  max) {
				var listElem = jQuery('<tr></tr>');
				listElem.appendTo(listParent);
				var tdElement = jQuery('<td></td>');
				tdElement.appendTo(listElem);
				list.push(AutoCompleteOntologyInput(tdElement, templatePos, ontologies));
			}
		}
		
		function removeInput() {
			if (list.length > min) {
				listParent.find('tr').last().remove();
				list.pop();
			}
		}
		
		return {
			extractParameter : function(parameter, template, field, pos, extractionResult) {
				var success = true;
				jQuery.each(list, function(index, inputElem){
					if (inputElem) {
						var csuccess = inputElem.extractParameter(parameter, template, field, index, extractionResult);
						success = success && csuccess;
					}
				});
				return success;
			}
		};
	}
	
	function AutoCompleteOntologyInputPrefix (elem, templatePos, ontologies, prefixes) {
		var checkbox, i, j;
		
		var container = createLayoutTable();
		container.appendTo(elem);
		var inputContainer = jQuery('<tr><td></td></tr>');
		inputContainer.appendTo(container);
		
		var inputField = AutoCompleteOntologyInput(inputContainer, templatePos, ontologies);
		
		var checkboxes = [];
		for ( i = 0; i < prefixes.length; i++) {
			checkbox = jQuery('<input type="checkbox" checked="true"/>');
			checkboxes[i] = checkbox;
			inputContainer = jQuery('<tr><td class="prefixCheckbox"></td></tr>');
			inputContainer.append(checkbox);
			inputContainer.append('<span class="term-prefix-label"> '+prefixes[i]+' </span>');
			inputContainer.appendTo(container);
		}
		
		function clearErrorState() {
			container.removeClass('termgenie-input-field-error');	
		}
		
		function setErrorState() {
			container.addClass('termgenie-input-field-error');
		}
		
		return {
			extractParameter : function(parameter, template, field, pos, extractionResult) {
				clearErrorState();
				if (!pos) {
					pos = 0;
				}
				var success = inputField.extractParameter(parameter, template, field, pos, extractionResult);
				
				var cPrefixes = [];
				
				for ( j = 0; j < checkboxes.length; j++) {
					checkbox = checkboxes[j];
					if(checkbox.is(':checked')) {
						cPrefixes.push(prefixes[j]);
					}
				}
				if (cPrefixes.length === 0) {
					setErrorState();
					extractionResult.addError('No prefixes selected.', template, field);
					return false;
				}
				parameter.strings[templatePos] = cPrefixes;
				return success;
			}
		};
	}
	
	/**
	 * Create the dynamic part of the user panel
	 */
	function createUserPanel() {
		var checkBoxElem = jQuery('#checkbox-try-commit');
		return {
			submit : function (terms, ontology) {
				// select mode
				var isCommit = checkBoxElem.is(':checked');
				
				var step3AdditionalHeader = jQuery('#span-step3-additional-header');
				step3AdditionalHeader.empty();
				
				var headerMessage = 'Selected ';
				headerMessage += terms.length;
				headerMessage += '  Term';
				if (terms.length > 1) {
					headerMessage += 's';
				}
				headerMessage += ' for ';
				
				if (isCommit) {
					headerMessage += 'Commit';
				} else {
					headerMessage += 'Export';
				}
				step3AdditionalHeader.append(headerMessage);
				var step4Container = jQuery('#termgenie-step4-content-container');
				step4Container.empty();
				myAccordion.enablePane(3);
				myAccordion.activatePane(3);
				
				if (isCommit) {
					// TODO verify username and password via login panel
					
					step4Container.append(createBusyMessage('Executing commit request on the server.'));
					// try to commit
					jsonService.commit.commitTerms({
						params: [terms, ontology, username, password],
						onSuccess: function(result) {
							step4Container.empty();
							renderCommitResult(result, step4Container);
						},
						onException: function(e) {
							step4Container.empty();
							loggingSystem.logSystemError("CommitTerms service call failed", e);
							return true;
						}
					});
				} else {
					step4Container.append(createBusyMessage('Preparing terms for export on the server.'));
					// just generate the info for the export a obo/owl
					jsonService.commit.exportTerms({
						params: [terms, ontology],
						onSuccess: function(result) {
							step4Container.empty();
							renderExportResult(result, step4Container);
						},
						onException: function(e) {
							step4Container.empty();
							loggingSystem.logSystemError("ExportTerms service call failed", e);
							return true;
						}
					});
				}
			}
		};
	}
	
	/**
	 * Display the results for the term generation.
	 * 
	 * @param ontology
	 * @param generationResponse 
	 * Type: 
	 * JsonGenerationResponse {
	 *     generalError: String,
	 *     errors: JsonValidationHint{
	 *         template: JsonTermTemplate,
	 *         field: int,
	 *         level: int,
	 *         hint: String;
	 *     }[],
	 *     generatedTerms: JsonOntologyTerm {
	 *     	   id: String,
	 *     	   label: String,
	 *     	   definition: String,
	 *     	   logDef: String,
	 *     	   comment: String,
	 *     	   defxRef: String[],
	 *     	   relations: JsonTermRelation {
	 *     			source: String,
	 *     			target: String,
	 *     			properties: String[]
	 *         }[]
	 * 	   }[]
	 */
	function createResultReviewPanel(generationResponse, ontology){
		var container = jQuery('#div-verification-and-review');

		// clear from previous results
		container.empty();
		// hide the submiy panel, till it is clear 
		// that there are results for the next step
		jQuery('#div-step3-submit-panel').hide();
		// remove existing click handler
		clearSubmitHandler();

		if (!generationResponse) {
			return;
		}

		if (isValid(generationResponse.generalError)) {
			renderGeneralError(container, generationResponse.generalError);			
			return;
		}

		if(isValid(generationResponse.errors)) {
			renderErrors(container, generationResponse.errors);
		}

		var reviewTerms = null;

		if(isValid(generationResponse.generatedTerms)) {
			reviewTerms = renderGeneratedTerms(parent, generationResponse.generatedTerms);

			setSubmitHandler(reviewTerms, generationResponse.generatedTerms, ontology);

			// show hidden panel
			jQuery('#div-step3-submit-panel').show();
		}
		
		// Helper functions, to improve readability of the code
		
		function isValid(field) {
			return field && field.length > 0;
		}
		
		function renderGeneralError(parent, generalError) {
			var generalErrorContainer = jQuery('<div class="term-generation-general-error"></div>');
			generalErrorContainer.appendTo(parent);
			generalErrorContainer.append('<div class="term-generation-general-error-heading">Error Message</div>');
			generalErrorContainer.append('<div class="term-generation-general-error-description">Your request produced the following error:</div>');
			generalErrorContainer.append('<div class="term-generation-general-error-details">'+generalError+'</div>');
			generalErrorContainer.append('<div class="term-generation-general-error-description">Please check your input and retry. If the problem persits, please contact the TermGenie team.</div>');
		}
		
		function renderErrors(parent, errors) {
			var detailedErrorContainer = jQuery('<div class="term-generation-detailed-errors"></div>');
			detailedErrorContainer.appendTo(parent);
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-heading">Error Messages</div>');
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Your request produced the following list of errors.</div>');
			var layout = jQuery('<table cellpadding="5"></table>');
			detailedErrorContainer.append(layout);
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Please consider the messages and try to fix them, by changing the input from the previous step.</div>');
			
			layout.append('<thead><tr><td>Template</td><td>Field</td><td>Level</td><td>Message</td></tr></thead>');
			
			jQuery.each(errors, function(index, validationHint){
				var trElement = jQuery('<tr></tr>');
				trElement.appendTo(layout);
				trElement.append('<td>' + validationHint.template.name + '</td>');
				if(validationHint.field >= 0 && validationHint.field < validationHint.template.fields.length) {
					trElement.append('<td>' + 
						validationHint.template.fields[validationHint.field].name +
						'</td>');
				}
				else {
					trElement.append('<td></td>');
				}
				trElement.append('<td>' + renderWarningLevel(validationHint.level) + '</td>');
				trElement.append('<td>' + validationHint.hint +'</td>');
			});
			
			function renderWarningLevel(level) {
				if (level < 10) {
					return '<span class="warn-level-warn">Warning</span>';
				}
				if (level > 10) {
					return '<span class="warn-level-fatal">Fatal</span>';
				}
				return '<span class="warn-level-error">Error</span>';
			}
		}
		
		/**
		 * Render the generated terms under the given parent.
		 * 
		 * @param parent DOM element
		 * @param generatedTerms array of generated terms
		 * @returns {
		 * 		checkBoxes: CheckBox[],
		 * 		termPanels: TermReviewPanel[]
		 * }
		 */
		function renderGeneratedTerms(parent, generatedTerms) {
			var checkBoxes = [];
			var termPanels = [];
			var generatedTermContainer = jQuery('<div class="term-generation-details"></div>');
			generatedTermContainer.appendTo(container);
			
			generatedTermContainer.append('<div class="term-generation-details-heading">Proposed new terms by TermGenie</div>');
			generatedTermContainer.append('<div class="term-generation-details-description">Your request produced the following list of term candidates:</div>');
			var layout = jQuery('<table cellpadding="5" class="termgenie-proposed-terms-table"></table>');
			generatedTermContainer.append(layout);
			
			var fields = ['label','definition','defxRef','synonyms'];
			var fieldHeaders = ['Label','Definition','Def_XRef','Synonyms'];
			var header = '<thead><tr><td></td>';
			jQuery.each(fieldHeaders, function(index, field){
				header += '<td>'+field+'</td>';
			});
			header += '<td>MetaData</td><td>Relations</td></tr></thead>';
			layout.append(header);
			
			jQuery.each(generatedTerms, function(index, term){
				var trElement = jQuery('<tr></tr>');
				trElement.appendTo(layout);
				var tdElement = jQuery('<td></td>');
				tdElement.appendTo(trElement);
				var checkBox = jQuery('<input type="checkbox"/>');
				checkBox.appendTo(tdElement);
				checkBoxes.push(checkBox);

				var termPanel = TermReviewPanel(trElement, term, fields);
				termPanels.push(termPanel);
			});
			generatedTermContainer.append('<div class="term-generation-details-description">Please select the term(s) for the final step.</div>');
			
			return {
				checkBoxes: checkBoxes,
				termPanels: termPanels
			};
		}
		
		/**
		 * Handle the review of one term.
		 * 
		 * @param trElement
		 * @param term
		 * @param fields
		 * @returns methods of the panel
		 */
		function TermReviewPanel(trElement, term, fields) {
			// clone term, preserve original term for comparison
			var newTerm = jQuery.extend(true, {}, term);
			
			// render the invidual fields
			var fieldPanels = [];
			// determine the type (String or array) jQuery.isArray()
			jQuery.each(fields, function(index, field) {
				var tdElement = jQuery('<td></td>');
				tdElement.appendTo(trElement);
				var value = term[field];
				var panel = null;
				
				if(jQuery.inArray(field, ['synonyms','defxRef']) >= 0) {
					if (value && value.length > 0) {
						panel = StringListFieldReviewPanel(tdElement, term, field);
					}
					else {
						panel = EmptyStringListFieldReviewPanel(tdElement, term, field);
					}
				}
				else if (jQuery.inArray(field, ['label','definition']) >= 0) {
					panel = StringFieldReviewPanel(tdElement, term, field);
				}
				fieldPanels[index] = panel;
			});
			// just create the panels for meta data and relations
			MetaDataFieldReviewPanel(trElement, term);
			RelationFieldReviewPanel(trElement, term);
			
			return {
				/**
				 * Retrieve the term from the panel.
				 * 
				 * @returns term
				 */
				getTerm : function(){
					jQuery.each(fields, function(index, field) {
						var panel = fieldPanels[index];
						if (panel !== null) {
							newTerm[field] = panel.getValue();
						}
					});
					// do not read any changes from meta data or relations
					return newTerm;
				}
			};
			
			function StringFieldReviewPanel(parent, term, field) {
				var reviewField = createInputField(term[field]);
				reviewField.appendTo(parent);
				
				return {
					getValue : function () {
						return normalizeString(reviewField.val());
					}
				};
			}
			
			function StringListFieldReviewPanel(parent, term, field) {
				var listParent = createLayoutTable();
				var rows = [];
				listParent.appendTo(parent);
				
				var table = createLayoutTable();
				table.appendTo(parent);
				var strings = term[field];
				jQuery.each(strings, function(index, value){
					addLine(value);
				});
				
				createAddRemoveWidget(parent, addLine, removeLine);
				
				function addLine(value) {
					var tableCell = jQuery('<tr></tr>');
					var tdCell = jQuery('<td></td>');
					tdCell.appendTo(tableCell);
					var checkbox = jQuery('<input type="checkbox" checked="true"/>');
					checkbox.appendTo(tdCell);
					
					tdCell = jQuery('<td></td>');
					tdCell.appendTo(tableCell);
					var inputField = createInputField(value);
					inputField.appendTo(tdCell);
					tableCell.appendTo(listParent);
					rows.push({
						tableCell : tableCell,
						checkbox : checkbox,
						inputField : inputField
					});
				}
				
				function removeLine() {
					if (rows.length > strings.length) {
						var element = rows.pop();
						element.tableCell.remove();
					}
				}
				
				return {
					getValue : function () {
						var strings = [];
						jQuery.each(rows, function(index, element){
							if(element.checkbox.is(':checked')) {
								var text = normalizeString(element.inputField.val());
								if (text !== null) {
									strings.push(text);
								}
							}
						});
						if (strings.length > 0) {
							return strings;
						}
						return null;
					}
				};
			}
			
			function EmptyStringListFieldReviewPanel(parent, term, field) {
				var listParent = createLayoutTable();
				var rows = [];
				listParent.appendTo(parent);
				addLine();
				createAddRemoveWidget(parent, addLine, removeLine);
				
				function addLine() {
					var tableCell = jQuery('<tr><td></td></tr>');
					var inputField = createInputField();
					inputField.appendTo(tableCell);
					tableCell.appendTo(listParent);
					rows.push({
						tableCell : tableCell,
						inputField : inputField
					});
				}
				
				function removeLine() {
					if (rows.length > 1) {
						var element = rows.pop();
						element.tableCell.remove();
					}
				}

				return {
					getValue : function () {
						var strings = [];
						jQuery.each(rows, function(index, element){
							var text = normalizeString(element.inputField.val());
							if (text !== null) {
								strings.push(text);
							}
						});
						if (strings.length > 0) {
							return strings;
						}
						return null;
					}
				};
			}
			
			/**
			 * Render the meta data. Currently this is read-only.
			 * 
			 * @param parent
			 * @param term
			 * @returns empty object: no external methods
			 */
			function MetaDataFieldReviewPanel(parent, term) {
				var metaData = term.metaData;
				var table = createLayoutTable();
				table.appendTo(parent);
				jQuery.each(metaData, function(field, value) {
					table.append('<tr><td>'+field+'</td><td class="nobr">'+value+'</td></tr>');
				});
				return {};
			}
			
			/**
			 * Render the relations. Currently this is read-only.
			 * 
			 * @param parent
			 * @param term
			 * @returns empty object: no external methods
			 */
			function RelationFieldReviewPanel(parent, term) {
				
				return {};
			}
			
			function createInputField(string) {
				if (!string || typeof string !== 'string') {
					return jQuery('<input type="text" style="width:250px"/>');
				}
				var elem;
				if (string.length < 35) {
					elem = jQuery('<input type="text" style="width:250px"/>');
				}
				else {
					elem = jQuery('<textarea style="width:250px;height:70px"></textarea>');
				}
				elem.val(string);
				return elem;
			}
			
			function normalizeString(string) {
				if (string && string.length > 0) {
					string = jQuery.trim(string).replace(/\s+/g,' ');
					if (string.length > 1) {
						// ignore strings with just one char!
						return string;
					}
				}
				return null;
			}
		}
		
		function clearSubmitHandler() {
			jQuery('#button-submit-for-commit-or-export').unbind('click');
		}
		
		function setSubmitHandler(reviewTerms, generatedTerms, ontology) {
			/*
			 * add functionality to the submit button:
			 * only try to commit, if at least one check box is enabled,
			 * otherwise prepare for export.
			 */
			jQuery('#button-submit-for-commit-or-export').click(function(){
				var terms = [];
				if (reviewTerms !== null) {
					jQuery.each(reviewTerms.checkBoxes, function(index, checkBox){
						if (checkBox.is(':checked')) {
							terms.push(reviewTerms.termPanels[index].getTerm());
						}
					});
				}
				if (terms.length === 0) {
					loggingSystem.logUserMessage('Please select at least one term to proceed.');
					return;
				}
				
				myUserPanel.submit(terms, ontology);
			});
		}
	}
	
	/**
	 * @param commitResults JsonCommitResult {
	 * 		success: boolean,
	 * 		message: String,
	 * 		terms: JsonOntologyTerm[]
	 * 
	 * }
	 * @param container target DOM element
	 */
	function renderCommitResult(commitResults, container) {
		container.append('<div class="term-generation-commit-heading">Commit<div>');
		if (commitResults.success === true) {
			// TODO
		}
		else {
			container.append('<div>The commit of the generated terms did not complete normally with the following reason:</div>');
			container.append('<div class="term-generation-commit-error-details">'+commitResults.message+'</div>');
		}
	}
	
	/**
	 * @param exportResult JsonExportResult {
	 * 		success: boolean,
	 * 		message: String,
	 * 		formats: String[],
	 * 		contents: String[]
	 * }
	 * @param container target DOM element
	 */
	function renderExportResult(exportResult, container) {
		var i;
		var name;
		var content;
		container.append('<div class="term-generation-export-heading">Export<div>');
		var exportsContainer = jQuery('<div class="term-generation-exports"></div>');
		exportsContainer.appendTo(container);
		if (exportResult.success === true) {
			for( i = 0; i < exportResult.formats.length; i += 1) {
				name = exportResult.formats[i];
				if (name && name.length > 0 && exportResult.contents.length > i) {
					content = exportResult.contents[i];
					if (content && content.length > 0) {
						renderExport(name, content, exportsContainer);
					}
				}
			}
		}
		else {
			container.append('<div>The export of the generated terms did not complete normally with the following reason:</div>');
			container.append('<div class="term-generation-export-error-details">'+exportResult.message+'</div>');
		}
		
		
		function renderExport(name, content, exportsContainer) {
			exportsContainer.append('<div>'+name+'</div>');
			exportsContainer.append('<pre>'+content+'</pre>');
		}
	}
	
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
				tabTitles.append('<li><a href="'+tabId+'">'+name+'</a></li>');
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
		 * Specifiy a DOM parent and a message limit.
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
	
	// Helper functions
	/**
	 * Retrieve the display name for this template.
	 * 
	 * @param template
	 * @returns String displayName
	 */
	function getTemplateName(template) {
		if (template.display && template.display.length > 0) {
			return template.display;
		}
		return template.name;
	}
	
	/**
	 * Format the internal ontology name into a readable version.
	 * 
	 * @param ontologyName String
	 * @returns String better readable ontology name
	 */
	function getOntologyName(ontologyName) {
		// replace the '|' char with a space
		return ontologyName.replace(/\|/,' ');
	}
	
	/**
	 * Format the internal ontology name list into a short readable version.
	 * 
	 * @param ontologyNames String[]
	 * @returns String better readable ontology name
	 */
	function getShortOntologyNameList(ontologyNames) {
		var result = '';
		jQuery.each(ontologyNames, function(index, ontologyName){
			if (index > 0) {
				result += ', ';
			}
			result += getShortOntologyName(ontologyName);
		});
		return result;
	}
	
	/**
	 * Try to minimze the ontology name.
	 * 
	 * @param ontologyName
	 * @returns String short version of the ontology name
	 */
	function getShortOntologyName(ontologyName) {
		// remove the 'GeneOntology|' prefix
		var name = ontologyName.replace(/GeneOntology\|/,''); 
		return getOntologyName(name);
	}
	
	// HTML wrapper functions
	/** 
	 * @param parent DOM element
	 * @param addfunction function clickhandler for add
	 * @param removeFunction function clickhandler for remove
	 */
	function createAddRemoveWidget(parent, addfunction, removeFunction) {
		var addButton = jQuery('<a class="myClickable">More</a>'); 
		var delButton = jQuery('<a class="myClickable">Less</a>');
		var buttons = jQuery('<span class="more-less-buttons"></span>');
		buttons.append(" (");
		buttons.append(addButton);
		buttons.append(", ");
		buttons.append(delButton);
		buttons.append(")");
		buttons.appendTo(parent);
		
		// click listener for add button
		addButton.click(addfunction);
		
		// click listener for remove button
		delButton.click(removeFunction);
	}
	
	/**
	 * Create a styled layout table with no borders, zero spacing and zero padding.
	 * 
	 * @returns DOM element
	 */
	function createLayoutTable() {
		return jQuery(createLayoutTableOpenTag()+'</table>');
	}
	
	/**
	 * Create starting tag for layout table.
	 * 
	 * @returns String
	 */
	function createLayoutTableOpenTag() {
		return '<table class="termgenie-layout-table" cellSpacing="0" cellPadding="0">';
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
	 * Helper for creating a div tag with an id and specified content.
	 * 
	 * @param id div id
	 * @param content html
	 */
	function c_div(id, content) {
		return '<div id="'+id+'">'+content+'</div>';
	}
	
	/**
	 * Helper for creating a span tag with an style class and specified content.
	 * 
	 * @param css css style class
	 * @param content html
	 */
	function c_span(css, content) {
		return '<span class="'+css+'">'+content+'</span>';
	}
	
	/**
	 * Helper for creating a button with an id and specified text.
	 * 
	 * @param id button id
	 * @param text text
	 */
	function c_button(id, text) {
		return '<button type="button" id="'+id+'">'+text+'</button>';
	}

	
	return {
		// empty object to hide internal functionality
	};
};

// actuall call in jquery to execute the termgenie scripts after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	termgenie();
});