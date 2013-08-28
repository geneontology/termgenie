/**
 * Setup the TermGenie management page.
 * 
 * @returns empty object
 */
function TermGenieFreeForm(){
	
	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainConfigurationPanel = jQuery('#MainConfigurationPanel');
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
	              'progress.getProgress',
	              'freeform.isEnabled',
	              'freeform.canView',
	              'freeform.getAvailableNamespaces',
	              'freeform.autocomplete',
	              'freeform.validate',
	              'freeform.submit',
	              'freeform.getAutoCompleteResource']
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
		// request sessionId and then check if the free form feature is enabled
		jsonService.freeform.isEnabled({
			onSuccess: function(result) {
				if (result === true) {
					checkUserPermissions(function(hasPermission){ // on success
						if (hasPermission === true) {
							startFreeForm();
						}
						else {
							setInsufficientUserRightsMessage(myLoginPanel.getCredentials());
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
				jQuery.logSystemError('Could not check free form feature on server',e);
				return true;
			}
		});	
	}
	
	function onLogout() {
		mainMessagePanel.empty();
		mainConfigurationPanel.empty();
		mainMessagePanel.append(defaultErrorMessage);
	}
	
	function setReviewDisabledMessage() {
		mainMessagePanel.append('The free form feature is not enabled for this TermGenie server.');
	}
	
	function setInsufficientUserRightsMessage(username) {
		mainMessagePanel.append('The current user ('+username+') is not allowed to use the free form feature.');
	}
	
	function checkUserPermissions(onSuccess, onError) {
		// request sessionId and then check user permissions
		mySession.getSessionId(function(sessionId){
			jsonService.freeform.canView({
				params: [sessionId],
				onSuccess: onSuccess,
				onException: onError
			});	
		});
	}
	
	/**
	 * Start free form input.
	 */
	function startFreeForm() {
		// TODO place holder, implement proper free form input elements 
		mainConfigurationPanel.load('TermGenieFreeFormContent.html', function() {
			var myAccordion = MyAccordion('#accordion');
			
			mySession.getSessionId(function(sessionId){
				jsonService.freeform.getAvailableNamespaces({
					params: [sessionId],
					onSuccess: function(oboNamespaces) {
						if (oboNamespaces && oboNamespaces !== null && oboNamespaces.length >= 0) {
							getRemoteResourcesPopulateFreeForm(sessionId, oboNamespaces, myAccordion);
						}
						else {
							jQuery.logSystemError('Retrieved OBO namespaces are empty.');
						}
					},
					onException: function(e) {
						jQuery.logSystemError('Could not retrieve OBO namespaces from server', e);
					}
				});	
			});
		});
		
		function getRemoteResourcesPopulateFreeForm(sessionId, oboNamespaces, myAccordion) {
			fetchRemoteResource('xref', function(xrefs) {
				// process lines into choices
				var choices = [];
				jQuery.each(xrefs, function(index, pair){
					if(pair.value !== undefined && pair.value !== null) {
						choices.push(pair.value);
					}
				});
				populateFreeFormInput(oboNamespaces, myAccordion, choices, []);
			}, function(e){
				// hidden error message
				jQuery.logSystemError('RemoteResource service call failed',e, true);
				
				// render form without auto-complete for xrefs
				populateFreeFormInput(oboNamespaces, myAccordion, [], []);
			});
		};
		
		function fetchRemoteResource(name, onSuccessCallback, onExceptionCallback) {
			// request sessionId and then start a request for the resource.
			mySession.getSessionId(function(sessionId){
				// use json-rpc to retrieve available ontologies
				jsonService.freeform.getAutoCompleteResource({
					params: [sessionId, name],
					onSuccess: onSuccessCallback,
					onException: onExceptionCallback
				});	
			});
		};
		
		/**
		 * Create the input fields and selectors for the free form template.
		 * 
		 * @param oboNamespaces
		 * @param myAccordion
		 * @param xrefChoices
		 * @param orcids
		 */
		function populateFreeFormInput(oboNamespaces, myAccordion, xrefChoices, orcids) {
			// label
			var labelInput = createLabelInput();
			
			// namespace selector
			var namespaceInput = createNamespaceInput();
			
			// relations
			var relationsInput = createRelationsInput();
			relationsInput.disable();
			
			// def
			var defInput = createDefInput();
			defInput.disable();
			
			// def xrefs
			var defXrefsInput = createDefXrefsInput(xrefChoices);
			defXrefsInput.disable();

			// synonyms
			var synonymInput = createSynonymInput();
			synonymInput.disable();
			
			// xrefs
			var xrefInputs = createXrefInputs();
			xrefInputs.disable();
			
			// validate input button
			var validateButton = createValidateButton();
			validateButton.disable();
			
			// make the first call, the pre-selected value does not fire the change event
			namespaceInput.updateNamespace();
			
			/**
			 * Create an input object for the label in the free form template.
			 * 
			 * @returns label input object
			 */
			function createLabelInput() {
				// retrieve the pre-existing DOM element
				var labelInputField = jQuery('#free-form-input-label');
				
				function setError() {
					labelInputField.addClass('termgenie-input-field-error');
				}
				
				function resetError() {
					labelInputField.removeClass('termgenie-input-field-error');
				};
				
				labelInputField.change(resetError);
				
				/**
				 * return the functions for this object
				 */
				return {
					getLabel: function() {
						return labelInputField.val();
					},
					validate: function() {
						var label = labelInputField.val();
						if (label && label !== null && label.length > 5) {
							return null;
						}
						setError();
						return 'A valid label is required for a new term.';
					}
				};
			};
			
			/**
			 * Create an input object for the OBO namespace(s) in the free form template.
			 * 
			 * @returns namespace input object
			 */
			function createNamespaceInput() {
				// retrieve the pre-existing DOM element
				var namespaceCell = jQuery('#free-form-input-namespace-cell');
				var namespaceInputSelector = jQuery('<select></select>')
				jQuery.each(oboNamespaces, function(intIndex, objValue){
					namespaceInputSelector.append('<option value="'+objValue+'">'+objValue+'</option>');
				});
				namespaceCell.append(namespaceInputSelector);
				
				namespaceInputSelector.change(updateOboNamespace);
				
				function updateOboNamespace(){
					resetError();
					var currentOboNamespace = getVal();
					
					// enable relations with auto-complete for terms
					relationsInput.enable(currentOboNamespace);
					
					// enable def, def-xrefs, synonyms
					defInput.enable();
					defXrefsInput.enable();
					synonymInput.enable();
					xrefInputs.enable();
					
					// active validate button
					validateButton.enable();
				};
				
				function getVal() {
					return namespaceInputSelector.val();
				};
				
				function setError() {
					namespaceInputSelector.addClass('termgenie-input-field-error');
				};
				
				function resetError() {
					namespaceInputSelector.removeClass('termgenie-input-field-error');
				};
				
				/**
				 * return the functions for this object
				 */
				return {
					getNamespace: function() {
						return getVal();
					},
					validate: function() {
						var current = getVal();
						if (current && current !== null && current.length > 0) {
							return null;
						}
						setError();
						return 'A valid obo namespace is required for a new term'
					},
					updateNamespace: function() {
						updateOboNamespace();
					}
				};
			};
			
			/**
			 * Create an input object for the definition in the free form template.
			 * 
			 * @returns definition input object.
			 */
			function createDefInput() {
				// retrieve the pre-existing DOM element
				var defInputField = jQuery('#free-form-input-def');
				
				function getVal() {
					return defInputField.val();
				};
				
				function setError() {
					defInputField.addClass('termgenie-input-field-error');
				};
				
				function resetError() {
					defInputField.removeClass('termgenie-input-field-error');
				};
				
				/**
				 * return the functions for this object
				 */
				return {
					enable: function() {
						defInputField.removeAttr("disabled");
					},
					disable: function() {
						defInputField.attr("disabled", "disabled"); // disable
					},
					getDef: function() {
						return getVal();
					},
					validate: function() {
						resetError();
						var current = getVal();
						if (current && current !== null && current.length > 0) {
							return null;
						}
						setError();
						return "A valid term definition is required."
					}
				};
			};
			
			/**
			 * Create an input list object for definition xrefs in the free form template.
			 * 
			 * @param xrefChoices
			 * @returns xrefs input object
			 */
			function createDefXrefsInput(xrefChoices) {
				// retrieve the pre-existing DOM element
				var defXrefsInputCell = jQuery('#free-form-input-dbxref-cell');
				var listParent = createLayoutTable();
				defXrefsInputCell.append(listParent);
				var inputFields = [];
				
				var addRemove = createAddRemoveWidget(defXrefsInputCell, addField, removeField);
				// create two input fields, one for the editor xref and one for the literature xref
				addField();
				addField();
				
				function addField() {
					var listElem = jQuery('<tr></tr>');
					listElem.appendTo(listParent);
					var tdElement = jQuery('<td></td>');
					tdElement.appendTo(listElem);
					var inputField = jQuery('<input type="text"/>');
					if (xrefChoices && xrefChoices !== null) {
						inputField.autocomplete({
							source: xrefChoices
						});
					}
					tdElement.append(inputField);
					inputFields.push(inputField);
				};
				
				function removeField() {
					if (inputFields.length > 1) {
						listParent.find('tr').last().remove();
						inputFields.pop();
					}
				};
				
				function getVal() {
					var results = [];
					jQuery.each(inputFields, function(pos, inputField){
						var val = inputField.val();
						if (val && val !== null && val.length > 0) {
							results.push(val);
						}
					});
					return results;
				};
				
				function setError() {
					jQuery.each(inputFields, function(pos, inputField){
						inputField.addClass('termgenie-input-field-error');
					});
				};
				
				function resetError() {
					jQuery.each(inputFields, function(pos, inputField){
						inputField.removeClass('termgenie-input-field-error');
					});
				};
				
				/**
				 * return the functions for this object
				 */
				return {
					enable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.removeAttr("disabled");
						});
						addRemove.enable();
					},
					disable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.attr("disabled", "disabled"); // disable
						});
						addRemove.disable();
					},
					getXrefs: function() {
						return getVal();
					},
					validate: function() {
						resetError();
						var current = getVal();
						if (current && current !== null && current.length > 0) {
							var errors = [];
							jQuery.each(current, function(pos, text){
								var pattern = /^\S+:\S+$/; // {non-whitespace}+ colon {non-whitespace}+ [whole string]
								var matching = pattern.test(text); 
								if (matching === false) {
									errors.push('The xref: "'+text+'" does not conform to the expected pattern. XRefs consists of a prefix and suffix with a colon (:) as separator and no whitespaces');
								}
							});
							if (errors.length > 0) {
								setError();
								return errors[0];
							}
							return null;
						}
						setError();
						return "At lease one valid xref is required."
					}
				};
			};
			
			/**
			 * Create input elements for relations in the free form template.
			 * 
			 * @returns relations object
			 */
			function createRelationsInput() {
				var isaList = createIsAList();
				var partOfList = createPartOfList();
				var hasPartList = createHasPartList();
				
				return {
					enable: function(oboNamespace) {
						isaList.enable(oboNamespace);
						partOfList.enable();
					},
					disable: function() {
						isaList.disable();
						partOfList.disable();
					},
					getIsA: function() {
						return isaList.values();
					},
					getPartOf: function() {
						return partOfList.values();
					},
					getHasPart: function() {
						return hasPartList.values();
					},
					validate: function() {
						var isaValidation = isaList.validate()
						if (isaValidation !== undefined && isaValidation !== null) {
							return isaValidation;
						}
						var partOfValidation = partOfList.validate();
						if (partOfValidation !== undefined && partOfValidation !== null) {
							return partOfValidation;
						}
						return hasPartList.validate();
					}
				};
				
				/**
				 * Create an input object for a list of isa relations.
				 * 
				 * @returns isa list object
				 */
				function createIsAList() {
					// retrieve the pre-existing DOM element
					var globalIsAContainer = jQuery('#free-form-input-isa-cell');
					var listParent = createLayoutTable();
					globalIsAContainer.append(listParent);
					var inputFields = [];
					
					var currentOboNamespace = null;
					
					var addRemove = createAddRemoveWidget(globalIsAContainer, addField, removeField);
					addField();
					
					function addField() {
						var listElem = jQuery('<tr></tr>');
						listElem.appendTo(listParent);
						var tdElement = jQuery('<td></td>');
						tdElement.appendTo(listElem);
						inputFields.push(AutoCompleteTerms(tdElement, function(){
							return currentOboNamespace;
						}, true));
					};
					
					function removeField() {
						if (inputFields.length > 1) {
							listParent.find('tr').last().remove();
							inputFields.pop();
						}
					};
					
					function getValues() {
						var results = [];
						jQuery.each(inputFields, function(pos, value){
							var currentValue = value.value();
							if (currentValue && currentValue !== null) {
								results.push(currentValue);
							}
						});
						return results;
					}
					
					/**
					 * return the functions for this object
					 */
					return {
						values: getValues,
						validate: function() {
							var errors = [];
							jQuery.each(inputFields, function(pos, value){
								// only require the first field to be valid
								var optional = false;
								if (pos > 0) {
									optional = true;
								}
								var error = value.validate(optional, 'Missing is_a relationship. At least one is_a relation is required.');
								if (error && error !== null) {
									errors.push(error);
								}
							});
							if (errors.length > 0) {
								return errors[0];
							}
							var currentValues = getValues();
							if (!currentValues || currentValues === null || currentValues.length === 0) {
								return 'Missing is_a relationship. At least one is_a relation is required.';
							}
							return null;
						},
						enable: function(oboNamespace) {
							currentOboNamespace = oboNamespace;
							jQuery.each(inputFields, function(pos, value){
								value.enable();
							});
							addRemove.enable();
						},
						disable: function() {
							jQuery.each(inputFields, function(pos, value){
								value.disable();
							});
							addRemove.disable();
						}
					};
				}
				
				/**
				 * Create an input object for a list of partOf relations.
				 * 
				 * @returns partOf relation list object
				 */
				function createPartOfList() {
					// retrieve the pre-existing DOM element
					var globalPartOfContainer = jQuery('#free-form-input-partof-cell');
					return createAutoCompleteRelationList(globalPartOfContainer);
				}
				
				/**
				 * Create an input object for a list of hasPart relations.
				 * 
				 * @returns hasPart relation list object
				 */
				function createHasPartList() {
					// retrieve the pre-existing DOM element
					var globalHasPartContainer = jQuery('#free-form-input-haspart-cell');
					return createAutoCompleteRelationList(globalHasPartContainer);
				}
				
				/**
				 * Create an input object for a list of relations.
				 * 
				 * @returns relation list object
				 */
				function createAutoCompleteRelationList(inputContainer) {
					var listParent = createLayoutTable();
					inputContainer.append(listParent);
					var inputFields = [];
					
					var addRemove = createAddRemoveWidget(inputContainer, addField, removeField);
					addField();
					
					function addField() {
						var listElem = jQuery('<tr></tr>');
						listElem.appendTo(listParent);
						var tdElement = jQuery('<td></td>');
						tdElement.appendTo(listElem);
						inputFields.push(AutoCompleteTerms(tdElement, null));
					};
					
					function removeField() {
						if (inputFields.length > 1) {
							listParent.find('tr').last().remove();
							inputFields.pop();
						}
					};
					
					function getValues() {
						var results = [];
						jQuery.each(inputFields, function(pos, value){
							var currentValue = value.value();
							if (currentValue && currentValue !== null) {
								results.push(currentValue);
							}
						});
						return results;
					};
					
					/**
					 * return the functions for this object
					 */
					return {
						values: getValues,
						validate: function() {
							var errors = [];
							jQuery.each(inputFields, function(pos, value){
								var error = value.validate(true);
								if (error && error !== null) {
									errors.push(error);
								}
							});
							if (errors.length > 0) {
								return errors[0];
							}
							return null;
						},
						enable: function() {
							jQuery.each(inputFields, function(pos, value){
								value.enable();
							});
							addRemove.enable();
						},
						disable: function() {
							jQuery.each(inputFields, function(pos, value){
								value.disable();
							});
							addRemove.disable();
						}
					};
				}
				
				/**
				 * Input field widget with auto-completion for a single ontology terms. 
				 * 
				 * @param elem {DOM element} element
				 * @param oboNamespace function to retrieve the current obo namespace
				 * 
				 * @returns functions for the object: value(), validate()
				 */
				function AutoCompleteTerms(elem, getOboNamespace, clearOnEnable) {
					
					var inputElement = jQuery('<input></input>');
					elem.append(inputElement);
					
					function clearErrorState() {
						inputElement.removeClass('termgenie-input-field-error');	
					}
					
					function setErrorState() {
						inputElement.addClass('termgenie-input-field-error');
					}
					
					// setup the autocompletion widget, includes an
					// additional description div for terms
					
					// used to prevent race conditions
					var requestIndex = 0;
					
					inputElement.extendedautocomplete({
						minLength: 3,
						// create data source
						// use rpc to retrieve suggestions
						source: function( request, response ) {
							var oboNamespace = null;
							if (getOboNamespace && getOboNamespace !== null) {
								oboNamespace = getOboNamespace();
								if (!oboNamespace) {
									oboNamespace = null;
								}
							}
							// prepare rpc request data
							var term = request.term;
							requestIndex += 1;
							var myRequestIndex = requestIndex;

							mySession.getSessionId(function(sessionId){
								jsonService.freeform.autocomplete({
									params:[sessionId, term, oboNamespace, 5],
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
										jQuery.logSystemError('Autocomplete service call failed', e, true);
										if (myRequestIndex === requestIndex) {
											response([]);
										}
									}
								});
							});
						},
						createInfoDiv: function() {
							return '<div class="term-description-content"></div>';
						},
						createInfoDivContent: function(item){
							var layout = createLayoutTableOpenTag();
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
							return layout;
						},
						getLabel: function(item){
							return item.label;
						}, 
						onSelect : function() {
							clearErrorState();
						},
						renderItem: function( ul, item ) {
							return jQuery( '<li class="termgenie-autocomplete-menu-item"></li>' )
								.data( 'item.autocomplete', item )
								.append( '<a><span class="termgenie-autocomplete-menu-item-label">' + 
										item.label + '</span></a>' )
								.appendTo( ul );
						}
					});
					
					function getValue() {
						var term = inputElement.extendedautocomplete( 'getSelected' );
						if (term && term !== null) {
							var text = inputElement.val();
							if (term.label == text) {
								var identifier = term.identifier.termId;
								return identifier;
							}
						}
						return null;
					}
					
					/**
					 * return the functions for this object
					 */
					return {
						validate: function(optional, msg) {
							clearErrorState();
							var current = getValue();
							if ((current && current !== null && current.length > 0) || (optional === true)) {
								return null;
							}
							setErrorState();
							if (msg && msg !== null) {
								return msg;
							}
							return "The input field does not contain a valid term.";
						},
						value: getValue,
						enable: function() {
							inputElement.removeAttr("disabled"); // enable
							if (clearOnEnable === true) {
								inputElement.extendedautocomplete( 'clear' );
							}
						},
						disable: function() {
							inputElement.attr("disabled", "disabled"); // disable
						}
					};
				}
			};
			
			function createSynonymInput() {
				// retrieve the pre-existing DOM element
				var synonymsInputCell = jQuery('#free-form-input-synonym-cell');
				var listParent = createLayoutTable();
				synonymsInputCell.append(listParent);
				var inputFields = [];
				
				var addRemove = createAddRemoveWidget(synonymsInputCell, addField, removeField);
				addField();
				
				function addField() {
					var listElem = jQuery('<tr></tr>');
					listElem.appendTo(listParent);
					var tdElement = jQuery('<td></td>');
					tdElement.appendTo(listElem);
					inputFields.push(createSynonymRow(tdElement));
				};
				
				function removeField() {
					if (inputFields.length > 1) {
						listParent.find('tr').last().remove();
						inputFields.pop();
					}
				};
				
				function getVal() {
					var results = [];
					jQuery.each(inputFields, function(pos, inputField){
						var val = inputField.val();
						if (val && val !== null && val.length > 0) {
							results.push(val);
						}
					});
					return results;
				};
				
				/**
				 * Create a table row for a synonym input. Created as part of 
				 * the given tableContainer.
				 * 
				 * @param tableContainer DOM-element
				 * @returns synonym row object
				 */
				function createSynonymRow(tableContainer) {
					var synonymRow = jQuery('<tr></tr>');
					tableContainer.append(synonymRow);
					
					// input fields
					// label
					var synonymLabel = createSynonymLabel();
					
					// scope selector
					var scopeSelector = createScopeSelector();
					
					// list of xrefs, provide sanity check
					var xrefList = createXrefSynonymList();
					
					/**
					 * Create a synonym label object.
					 * 
					 * @returns synonym label object
					 */
					function createSynonymLabel() {
						var labelField = jQuery('<input type="text" style="width:350px"/>');
						var labelTD = jQuery('<td></td>');
						labelTD.append(labelField);
						synonymRow.append(labelTD);
						
						function getValue() {
							return labelField.val();
						}
						
						/**
						 * return the functions for this object
						 */
						return {
							enable: function() {
								labelField.removeAttr("disabled");
							},
							disable: function() {
								labelField.attr("disabled", "disabled"); // disable
							},
							getLabel: getValue,
							validate: function() {
								return null;
							}
						};
					};
					
					/**
					 * Create a synonym scope selector object
					 * 
					 * @returns scope selector object
					 */
					function createScopeSelector() {
						var selector = jQuery('<select>'+
								'<option value="RELATED">RELATED</option>'+
								'<option value="NARROW">NARROW</option>'+
								'<option value="EXACT">EXACT</option>'+
								'<option value="BROAD">BROAD</option>'+
								'</select>');
						var scopeTD = jQuery('<td></td>');
						scopeTD.append(selector);
						synonymRow.append(scopeTD);
						
						/**
						 * return the functions for this object
						 */
						return {
							enable: function() {
								selector.removeAttr("disabled");
							},
							disable: function() {
								selector.attr("disabled", "disabled"); // disable
							},
							getScope: function() {
								return selector.val();
							},
							validate: function() {
								return null;
							}
						};
					};
					
					/**
					 * Create a list of xrefs for a synonym.
					 * 
					 * @returns xref list object
					 */
					function createXrefSynonymList() {
						var xrefListContainer = jQuery('<td></td>');
						synonymRow.append(xrefListContainer);
						var listParent = createLayoutTable();
						xrefListContainer.append(listParent);
						var inputFields = [];
						
						var addRemove = createAddRemoveWidget(xrefListContainer, addField, removeField);
						addField();
						
						function addField() {
							var listElem = jQuery('<tr></tr>');
							listElem.appendTo(listParent);
							var tdElement = jQuery('<td></td>');
							tdElement.appendTo(listElem);
							var inputField = jQuery('<input type="text"/>');
							tdElement.append(inputField);
							inputFields.push(inputField);
						};
						
						function removeField() {
							if (inputFields.length > 1) {
								listParent.find('tr').last().remove();
								inputFields.pop();
							}
						};
						
						function getValues() {
							var results = [];
							jQuery.each(inputFields, function(pos, inputField){
								var val = inputField.val();
								if (val && val !== null && val.length > 0) {
									results.push(val);
								}
							});
							return results;
						};
						
						return {
							enable: function() {
								jQuery.each(inputFields, function(pos, inputField){
									inputField.removeAttr("disabled");
								});
							},
							disable: function() {
								jQuery.each(inputFields, function(pos, inputField){
									inputField.attr("disabled", "disabled"); // disable
								});
							},
							getXrefs: getValues,
							validate: function() {
								var values = getValues();
								if (values && values !== null && values.length > 0) {
									jQuery.each(values, function(pos, value) {
										// TODO check that the values is in proper xref syntax:
										// s+:s+
									});
								}
								return null;
							}
						};
					}
					
					/**
					 * return the functions for this object
					 */
					return {
						enable: function() {
							synonymLabel.enable();
							scopeSelector.enable();
							xrefList.enable();
						},
						disable: function() {
							synonymLabel.disable();
							scopeSelector.disable();
							xrefList.disable();
						},
						getSynonym: function() {
							var currentSynonymLabel = synonymLabel.getLabel();
							if (currentSynonymLabel && currentSynonymLabel !== null && currentSynonymLabel.length > 0) {
								return {
									label: currentSynonymLabel,
									scope: scopeSelector.getScope(),
									category: null,
									xrefs: xrefList.getXrefs()
								};
							}
							return null;
						},
						validate: function() {
							var error;
							error = xrefList.validate();
							if (error && error !== null) {
								return error;
							}
							error = scopeSelector.validate();
							if (error && error !== null) {
								return error;
							}
							return synonymLabel.validate();
						}
					};
				}
				
				/**
				 * return the functions for this object
				 */
				return {
					enable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.enable();
						});
						addRemove.enable();
					},
					disable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.disable();
						});
						addRemove.disable();
					},
					getSynonyms: function() {
						var synonyms = [];
						jQuery.each(inputFields, function(pos, inputField){
							var currentSynonym = inputField.getSynonym();
							if (currentSynonym && currentSynonym !== null) {
								synonyms.push(currentSynonym);
							}
						});
						return synonyms;
					},
					validate: function() {
						jQuery.each(inputFields, function(pos, inputField){
							var currentValidation = inputField.validate();
							if (currentValidation && currentValidation !== null && currentValidation.length > 0) {
								return currentValidation;
							}
						});
						return null;
					}
				};
			};
			
			function createXrefInputs() {
				
				// retrieve the pre-existing DOM element
				var xrefsInputCell = jQuery('#free-form-input-xrefs-cell');
				
				var listParent = createLayoutTable();
				listParent.append('<tr><td>Xref</td><td>Comment</td></tr>');
				xrefsInputCell.append(listParent);
				var inputFields = [];
				
				var addRemove = createAddRemoveWidget(xrefsInputCell, addField, removeField);
				addField();
				
				function addField() {
					var listElem = jQuery('<tr></tr>');
					listElem.appendTo(listParent);
					
					// id
					var idTdElement = jQuery('<td></td>');
					idTdElement.appendTo(listElem);
					var idInputField = jQuery('<input type="text"/>');
					idTdElement.append(idInputField);
					
					// annotation
					var annotationTdElement = jQuery('<td></td>');
					annotationTdElement.appendTo(listElem);
					var annotationInputField = jQuery('<input type="text"/>');
					annotationTdElement.append(annotationInputField);
					
					inputFields.push({
						'idref': idInputField,
						'annotation': annotationInputField
					});
				};
				
				function removeField() {
					if (inputFields.length > 1) {
						listParent.find('tr').last().remove();
						inputFields.pop();
					}
				};
				
				
				return {
					enable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.idref.removeAttr("disabled");
							inputField.annotation.removeAttr("disabled");
						});
						addRemove.enable();
					},
					disable: function() {
						jQuery.each(inputFields, function(pos, inputField){
							inputField.idref.attr("disabled", "disabled");
							inputField.annotation.attr("disabled", "disabled");
						});
						addRemove.disable();
					},
					getXrefs: function() {
						var xrefs = [];
						jQuery.each(inputFields, function(pos, inputField){
							var ref = inputField.idref.val();
							if(ref !== undefined && ref !== null && ref.length > 0) {
								var xref = {
										'idRef' : ref
								}
								xrefs.push(xref);
								var ann = inputField.annotation.val();
								if (ann !== undefined && ann !== null && ann.length > 0) {
									xref['annotation'] = ann;
								}
							}
						});
						return xrefs;
					},
					validate: function() {
						// TODO check for comments without an xref
						// TODO check for xref format
						return null;
					}
				};
			}
			
			/**
			 * Validate all the input fields.
			 * 
			 * @returns null or first error message.
			 */
			function validateAll() {
				var error = labelInput.validate();
				if (error && error !== null) {
					return error;
				}
				error = namespaceInput.validate();
				if (error && error !== null) {
					return error;
				}
				error = defInput.validate();
				if (error && error !== null) {
					return error;
				}
				error = defXrefsInput.validate();
				if (error && error !== null) {
					return error;
				}
				error = relationsInput.validate();
				if (error && error !== null) {
					return error;
				}
				error = xrefInputs.validate();
				if (error && error !== null) {
					return error;
				}
				return synonymInput.validate();
			};
			
			/**
			 * Get the free form term request, created from the input fields.
			 * This is only a valid request, if the validateAll() does not 
			 * return any errors.
			 * 
			 * @returns free form term request.
			 */
			function getInputAll() {
				return {
					label: labelInput.getLabel(),
					namespace: namespaceInput.getNamespace(),
					definition: defInput.getDef(),
					dbxrefs: defXrefsInput.getXrefs(),
					isA: relationsInput.getIsA(),
					partOf: relationsInput.getPartOf(),
					hasPart: relationsInput.getHasPart(),
					synonyms: synonymInput.getSynonyms(),
					xrefs: xrefInputs.getXrefs()
				};
			};
		
			function createValidateButton() {
				// retrieve the pre-existing DOM element
				var validateButtonElem = jQuery('#button-freeform-verification-start');
				var busyElement= jQuery('#button-freeform-verification-start-progress');
				
				function validateButtonClickFunction() {
					busyElement.empty();
					
					// run sanity checks
					var error = validateAll();
					if (error && error !== null) {
						jQuery.logUserMessage(error);
						return;
					}
					
					// get request
					var freeFormRequest = getInputAll();
					
					// busy message
					var busyMessage = jQuery(createBusyMessage('Verifing your request on the server.'));
					busyElement.append(busyMessage);
					var progressInfo = ProgressInfoWidget(busyMessage, 6, false);
					
					// send request to server
					mySession.getSessionId(function(sessionId){
						jsonService.freeform.validate({
							params:[sessionId, freeFormRequest],
							onSuccess: function(result) {
								// render review panel
								createReviewPanel(result, myAccordion);
								
								// activate and switch to next panel
								myAccordion.enablePane(1);
								myAccordion.activatePane(1);
							},
							onException: function(e) {
								jQuery.logSystemError("Validation of free form request call failed", e);
								return true;
							},
							onProgress: function(uuid) {
								jsonService.progress.getProgress({
									params:[uuid],
									onSuccess: function(messages) {
										progressInfo.addMessages(messages);
									}
								});
							},
							onComplete: function() {
								// clear busy message in all cases
								busyElement.empty();
							}
						});
					});
				};
				
				return {
					enable: function() {
						validateButtonElem.removeAttr("disabled");
						validateButtonElem.click(validateButtonClickFunction);
					},
					disable: function() {
						validateButtonElem.attr("disabled", "disabled"); // disable
						validateButtonElem.unbind("click");
					}
				};
			};
		};

		/**
		 * 
		 * @param validationResponse 
		 * Type: JsonFreeFormValidationResponse {
		 *    generalError: String,
		 *    errors: FreeFormHint[] {
		 *       field: String,
		 *       hint: String
		 *    },
		 *    generatedTerm: JsonOntologyTerm {
		 *       tempId: String,
		 *       label: String,
		 *       definition: String,
		 *       defXRef: String[],
		 *       synonyms: JsonSynonym[] {
		 *          label: String,
		 *          scope: String,
		 *          category: String,
		 *          xrefs: String[]
		 *       },
		 *       relations: String[],
		 *       metaData: String[],
		 *       owlAxioms: String,
		 *       isObsolete: boolean,
		 *       pattern: String
		 *    }
		 * }
		 */
		// review panel
		function createReviewPanel(validationResponse, myAccordion) {
			var reviewContainer = jQuery('#freeform-step2-div-verification-and-review');
			
			// clean up
			// clear container
			reviewContainer.empty();
			
			// remove click handler
			var submitButton = jQuery('#button-submit-for-review');
			submitButton.unbind('click');
			
			// clear checkbox
			var submitCheckbox = jQuery('#checkbox-submit-for-review');
			submitCheckbox.unbind('change');
			submitCheckbox.removeAttr('checked');
			
			// hide submit button
			var buttonCheckBoxDiv = jQuery('#button-submit-for-review-div');
			buttonCheckBoxDiv.hide();
			
			// hide send e-mail button
			var sendEmailCheckBox = jQuery('#div-checkbox-send-submit-email');
			sendEmailCheckBox.hide();
			
			if (!validationResponse || validationResponse === null) {
				jQuery.logSystemError('Validation response is undefined');
				return;
			}
			
			// check general error
			if (validationResponse.generalError && validationResponse.generalError !== null) {
				renderGeneralError(reviewContainer, validationResponse.generalError)
				return;
			}
			
			// check for free form hints
			if (validationResponse.errors && validationResponse.errors !== null) {
				renderErrors(reviewContainer, validationResponse.errors);
				return;
			}
			
			// render warnings
			if (validationResponse.warnings && validationResponse.warnings !== null) {
				renderWarnings(reviewContainer, validationResponse.warnings);
			}
			
			// render term
			var generatedTerm = validationResponse.generatedTerm;
			if (!generatedTerm || generatedTerm === null) {
				jQuery.logSystemError("Invalid response to term verification: empty generated term.");
				return;
			}
			renderReviewPanel(reviewContainer, generatedTerm);
			
			// show e-mail select
			sendEmailCheckBox.show();
			
			// show submit button
			buttonCheckBoxDiv.show();
			
			// checkbox
			submitCheckbox.change(function(){
				if (submitCheckbox.is(':checked')) {
					// activate submit button
					submitButton.click(submitClickHandler);
				}
				else {
					submitButton.unbind('click');
				}
			});
			
			
			// --- Helper ---
			
			function renderGeneralError(parent, generalError) {
				var generalErrorContainer = jQuery('<div class="term-generation-general-error"></div>');
				generalErrorContainer.appendTo(parent);
				generalErrorContainer.append('<div class="term-generation-general-error-heading">Error Message</div>');
				generalErrorContainer.append('<div class="term-generation-general-error-description">Your request produced the following error:</div>');
				generalErrorContainer.append('<div class="term-generation-general-error-details">'+generalError+'</div>');
				generalErrorContainer.append('<div class="term-generation-general-error-description">Please check your input and retry. If the problem persits, please contact the TermGenie team.</div>');
			};
			
			function renderErrors(parent, errors) {
				var detailedErrorContainer = jQuery('<div class="term-generation-detailed-errors"></div>');
				detailedErrorContainer.appendTo(parent);
				detailedErrorContainer.append('<div class="term-generation-detailed-errors-heading">Error Messages</div>');
				detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Your request produced the following list of errors.</div>');
				var layout = jQuery('<table cellpadding="5"></table>');
				detailedErrorContainer.append(layout);
				detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Please consider the messages and try to fix them, by changing the input from the previous step.</div>');
				
				layout.append('<thead><tr><td>Field</td><td>Message</td></tr></thead>');
				
				jQuery.each(errors, function(index, validationHint){
					var trElement = jQuery('<tr></tr>');
					trElement.appendTo(layout);
					trElement.append('<td>' + validationHint.field +'</td>');
					trElement.append('<td>' + validationHint.hint +'</td>');
				});
			};
			
			function renderWarnings(parent, warnings) {
				var detailedWarningContainer = jQuery('<div class="term-generation-detailed-errors"></div>');
				detailedWarningContainer.appendTo(parent);
				detailedWarningContainer.append('<div class="term-generation-detailed-errors-heading">Warning Messages</div>');
				detailedWarningContainer.append('<div class="term-generation-detailed-errors-description">Your request produced the following list of warnings.</div>');
				if (warnings.length > 11) {
					detailedWarningContainer.append('<div class="term-generation-detailed-errors-description">There are '+warnings.length+' warnings. Only the first 10 are shown.</div>');
					warnings = warnings.slice(0,10);
				}
				var layout = jQuery('<table cellpadding="5"></table>');
				detailedWarningContainer.append(layout);
				detailedWarningContainer.append('<div class="term-generation-detailed-errors-description">Please consider the messages and try to fix them, by changing the input from the previous step.</div>');
				
				layout.append('<thead><tr><td>Field</td><td>Message</td></tr></thead>');
				
				jQuery.each(warnings, function(index, validationHint){
					var trElement = jQuery('<tr></tr>');
					trElement.appendTo(layout);
					trElement.append('<td>' + validationHint.field +'</td>');
					trElement.append('<td>' + validationHint.hint +'</td>');
				});
			};
			
			/**
			 * @param parent DOM element
			 * @param term 
			 * Type: JsonOntologyTerm {
			 *     tempId: String,
			 *     label: String,
			 *     definition: String,
			 *     defXRef: String[],
			 *     synonyms: JsonSynonym[] {
			 *        label: String,
			 *        scope: String,
			 *        category: String,
			 *        xrefs: String[]
			 *     },
			 *     relations: String[],
			 *     metaData: String[],
			 *     owlAxioms: String,
			 *     isObsolete: boolean,
			 *     pattern: String
			 *  }
			 */
			function renderReviewPanel(parent, term) {
				var layoutTable = createLayoutTable();
				parent.append(layoutTable);
				
				// label
				var tableContent = '<tr><td>Label</td><td>'+term.label+'</td></tr>\n';
				
				// definition
				tableContent += '<tr><td>Definition</td><td>'+term.definition+'</td></tr>\n';
				
				// xrefs
				tableContent += '<tr><td>Xrefs</td><td>';
				jQuery.each(term.defXRef, function(pos, xref){
					if (pos > 0) {
						tableContent += ', ';
					}
					tableContent += xref;
				});
				tableContent += '</td></tr>';
				
				// synonyms
				if (term.synonyms && term.synonyms !== null && term.synonyms.length > 0) {
					tableContent += '<tr><td>Synonyms</td><td></td></tr>/n';
					jQuery.each(term.synonyms, function(pos, synonym){
						tableContent += '<tr><td></td><td>'
						tableContent += '"'+synonym.label+'" '+synonym.scope+' [';
						if (synonym.xrefs && synonym.xrefs !== null) {
							jQuery.each(synonym.xrefs, function(xrefPos, xref){
								if (xrefPos > 0) {
									tableContent += ', ';
								}
								tableContent += xref;
							});
						}
						tableContent += ']</td></tr>/n';
					});
				}
				
				// relations
				tableContent += '<tr><td>Relations</td><td></td></tr>';
				jQuery.each(term.relations, function(pos, rel){
					tableContent += '<tr><td></td><td>';
					tableContent += rel;
					tableContent += '</td></tr>/n';
				});
				
				
				// meta data
				if (term.metaData && term.metaData !== null && term.metaData.length > 0) {
					tableContent += '<tr><td>Meta-Data</td><td></td></tr>';
					jQuery.each(term.metaData, function(pos, data){
						tableContent += '<tr><td></td><td>';
						tableContent += data;
						tableContent += '</td></tr>/n';
					});
				}
				
				layoutTable.append(tableContent);
				
			};
			
			function submitClickHandler() {
				// get check box status for e-mail send
				var sendConfirmationEmail = sendEmailCheckBox.is(':checked');
				
				// get and clear busyElement from step 3
				var busyElement = jQuery('#button-submit-for-review-progress');
				busyElement.empty();
				
				// busy message
				var busyMessage = jQuery(createBusyMessage('Submitting your request to the server.'));
				busyElement.append(busyMessage);
				var progressInfo = ProgressInfoWidget(busyMessage, 6, false);
				
				// clear old content from next panel
				var step3container = jQuery('#termgenie-freeform-step3-content-container');
				step3container.empty();
				
				// activate and switch to next panel
				myAccordion.enablePane(2);
				myAccordion.activatePane(2);
				
				// send request to server
				mySession.getSessionId(function(sessionId){
					jsonService.freeform.submit({
						params:[sessionId, generatedTerm, sendConfirmationEmail],
						onSuccess: function(result) {
							// render result panel
							createResultPanel(result, step3container);
						},
						onException: function(e) {
							jQuery.logSystemError("Service call for submission of free form term failed", e);
							return true;
						},
						onProgress: function(uuid) {
							jsonService.progress.getProgress({
								params:[uuid],
								onSuccess: function(messages) {
									progressInfo.addMessages(messages);
								}
							});
						},
						onComplete: function() {
							// clear busy message in all cases
							busyElement.empty();
						}
					});
				});
			};
		};
		
		// result panel
		function createResultPanel(results, container) {
			
			container.append('<div class="term-generation-commit-heading">Submitted for Review<div>');
			if (results.success === true) {
				if(results.message && results.message.length > 0) {
					container.append('<div>' + markupNewLines(results.message) + '</div>');
				}
				if (results.terms && results.terms.length > 0) {
					container.append('<div>The following terms have been created:</div>');
					var termList = jQuery('<ul></ul>');
					jQuery.each(results.terms, function(index, term){
						termList.append('<li style="font-family:monospace;">ID: '+term.tempId+' Label: '+term.label+'</li>');
					});
					container.append(termList);
				}
			}
			else {
				container.append('<div>The commit of the generated terms did not complete normally with the following reason:</div>');
				container.append('<div class="term-generation-commit-error-details">'+markupNewLines(results.message)+'</div>');
			}
			
			
			function markupNewLines(text) {
				var message = '';
				var lines = text.split('\n');
				jQuery.each(lines, function(pos, line){
					message += line + '</br>\n';
				});
				return message; 
			}
		};
		
		/**
		 * Provide a three-tab Accordion with the additional functionality to 
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
			
			jQuery(id).accordion({ heightStyle: 'content', event: "" });
			
			// implement a custom click function
			// allow only to open panes, which are enabled in the selections object
			jQuery(id+' h3').click(function() {
				var idx = jQuery(id+' h3').index(this);
				var activate = selections["Pane_" + idx];
				if (activate) {
					jQuery(id).accordion("option", "active", idx);
				}
			});
			
			return {
				/**
				 * Active the specified panel.
				 * 
				 * @param pos position to activate (zero-based)
				 */
				activatePane : function(pos) {
					jQuery(id).accordion("option", "active", pos);
				},
				
				/**
				 * Set the status of a pane.
				 * 
				 * @param pos position to activate (zero-based)
				 * @param state boolean
				 */
				setPaneState : function(pos, state) {
					selections["Pane_" + pos] = state;
				},
			
				/**
				 * Enable a pane for click events.
				 * 
				 * @param pos position to enable (zero-based)
				 */
				enablePane : function(pos) {
					selections["Pane_" + pos] = true;
				},
				
				/**
				 * Disable a pane for click events.
				 * 
				 * @param pos position to disable (zero-based)
				 */
				disablePane : function(pos) {
					selections["Pane_" + pos] = false;
				}
			};
		};
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
	 * Create stub for adding and removing elements.
	 * 
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
		
		return {
			enable: function() {
				// clean up before adding new handlers
				addButton.unbind('click');
				delButton.unbind('click');
				
				// add new handlers
				addButton.click(addfunction);
				delButton.click(removeFunction);
			},
			disable: function() {
				addButton.unbind('click');
				delButton.unbind('click');
			}
		};
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
	 * Widget for rendering process status messages provided by the TermGenie server.
	 * 
	 * @param parentElem parent DOM element
	 * @param limit max number of messages shown, if less or equals zero, all messages are shown
	 * @param renderDetails boolean flag, if true render optional message details
	 * @returns {
	 * 	  addMessages: function(messages)
	 *  }
	 */
	function ProgressInfoWidget(parentElem, limit, renderDetails) {
		
		var lineParent = jQuery('<div class="termgenie-progress-infos"></div>');
		parentElem.append(lineParent);
		
		return {
			addMessages: function(messages) {
				if (messages !== null) {
					jQuery.each(messages, function(index, progressMessage){
						var line = '<div>'
							+ '<span class="termgenie-progress-info-time">' + progressMessage.time + '</span>'
							+ '<span class="termgenie-progress-info-message">' + progressMessage.message + '</span>';
						if (renderDetails === true && progressMessage.details && progressMessage.details !== null) {
							line += '<div class="termgenie-progress-info-details">'
								+ '<span>Details:</span>'
								+ '<pre>'+progressMessage.details+'</pre>'
								+ '</div>';
						}
						line += '</div>'
						lineParent.append(line);
					});
					if (limit && limit > 0) {
						while (lineParent.children().length > limit) {
							lineParent.children().first().remove();
						}
					}
				}
			}
		};
	}
	
	return {
		// empty object to hide internal functionality
	};
}
// actual call in jQuery to execute the TermGenie free form scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie free form.
	TermGenieFreeForm();
});
