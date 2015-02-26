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
	
	//create proxy for json rpc
	var jsonService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: true,
	    methods: ['generate.availableTermTemplates', 
	              'generate.generateTerms',
	              'generate.getAutoCompleteResource',
	              'ontology.getOntologyStatus',
	              'ontology.autocomplete',
	              'commit.exportTerms',
	              'commit.commitTerms',
	              'user.createSession',
	              'user.logout',
	              'user.isAuthenticated',
	              'user.keepSessionAlive',
	              'user.getValue',
	              'user.setValue',
	              'user.getValues',
	              'user.setValues',
	              'browserid.verifyAssertion',
	              'renderer.renderHierarchy',
	              'renderer.visualizeGeneratedTerms',
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
	
	// global elements for this application
	var myAccordion = MyAccordion('#accordion');
	var myLoginPanel = jQuery.LoginPanel(jsonService, mySession); 
	var myUserPanel = createUserPanel();
	
	// create busy icon and message to show during wait
	jQuery('#div-select-ontology').append(createBusyMessage('Quering for available ontologies at the server.'));
	
	// retrieve ontology status and start render it in step 1
	jQuery('#div-step1-ontology-status').empty();
	jQuery('#div-step1-ontology-status').append(createBusyMessage('Checking ontology on the server.'));
	jsonService.ontology.getOntologyStatus({
		onSuccess: function(result) {
			jQuery('#div-step1-ontology-status').empty();
			/*
			 * Actual start code for the page.
			 * 
			 * Retrieve and create the content for the step 1.
			 * 
			 * The result contains the ontology name and the 
			 * current status information.
			 */
			createOntologyStatusInformation(result);
		},
		onException: function(e) {
			jQuery('#div-step1-ontology-status').empty();
			jQuery('#div-select-ontology').empty();
			jQuery.logSystemError('getOntologyStatus service call failed',e);
			jQuery('#div-step1-ontology-status').append('OntologyStatus service call failed. Please reload the page.');
			return true;
		}
	});
	
	var remoteResourceCache = {};
	
	function fetchRemoteResource(name, onSuccessCallback, onErrorCallback) {
		// first check cache
		if (remoteResourceCache[name]) {
			// found in cache
			onSuccessCallback(remoteResourceCache[name]);
			return;
		}
		// not in cache
		
		// create default error handler, if not defined
		if (!onErrorCallback) {
			onErrorCallback = function(e) {
				// hidden error message
				jQuery.logSystemError('RemoteResource service call failed',e, true);
			};
		}
		// request sessionId and then start a request for the resource.
		mySession.getSessionId(function(sessionId){
			// use json-rpc to retrieve available ontologies
			jsonService.generate.getAutoCompleteResource({
				params: [sessionId, name],
				onSuccess: function(result) {
					// put in cache
					remoteResourceCache[name] = result;
					onSuccessCallback(result);
				},
				onException: onErrorCallback
			});	
		});
	}
	
	/**
	 * Check the status object, if the status code is not okay, prevent requests and render a message.
	 * 
	 * Otherwise extract the ontology name and process to step 2.
	 * 
	 * @param ontologyStatus status object
	 */
	function createOntologyStatusInformation(ontologyStatus) {
		/*
		  ontologyStatus: {
				ontology: String,
				okay: boolean,
				messages: String[]
		  }
		 */
		var ontologyName = ontologyStatus.ontology;
		
		// if the status is okay, go to step 2
		if (ontologyStatus.okay === true) {
			setStep1Header(ontologyName);
			createTemplateSelector(ontologyName);
			setStep2Active();
		}
		else {
			// render message
			var ulParent = jQuery('<ul></ul>');
			jQuery.each(ontologyStatus.messages, function(index, message){
				ulParent.append('<li>'+message+'</li>');
			});
			jQuery('#div-step1-ontology-status').append(ulParent);
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
		 * Active the second step and deactivate step 1.
		 */
		function setStep2Active(step1Available) {
			myAccordion.setPaneState(0, false);
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
		var termselect = c_div('div-template-selector', '')+
				c_div('div-all-template-parameters','');
		
		// get intented dom element
		var elem = jQuery('#div-select-templates-and-parameters');
		// clear, from previous templates
		elem.empty();
		
		// clear header from previous templates
		jQuery('#span-step2-additional-header').empty();
		
		// set busy message
		elem.append(createBusyMessage('Quering for available termplates at the server.'));
		
		// request sessionId
		mySession.getSessionId(function(sessionId){
			// start async rpc to retrieve available templates
			jsonService.generate.availableTermTemplates({
				params:[sessionId],
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
					jQuery.logSystemError('AvailableTermTemplates service call failed',e);
					return true;
				}
			});
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
					jQuery.logUserMessage(logMessage, details);
					return;
				}
				if (status.parameters.length === 0) {
					jQuery.logUserMessage('Please select a template from the menu, and click on add template. '+
						'Provide details for the required fields and click on the "'+
						submitButton.text()+
						'"-Button again, to proceed to the next step.');
					return;
				}
				var busyMessage = jQuery(createBusyMessage('Verifing your request and generating terms on the server.'));
				busyElement.append(busyMessage);
				var progressInfo = ProgressInfoWidget(busyMessage, 6, false);
				
				setStep2HeaderInfo(status.parameters);
				mySession.getSessionId(function(sessionId){
					jsonService.generate.generateTerms({
						params:[sessionId, status.parameters],
						onSuccess: function(result) {
							renderStep3(result, ontology);
						},
						onException: function(e) {
							jQuery.logSystemError("GenerateTerms service call failed", e);
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
							busyElement.empty();
						}
					});
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
		var parent = jQuery('#div-template-selector');
		var renderTreeOnly = templates.length > 15;
		
		if (renderTreeOnly === false) {
		
			// create layout
			parent.append('<span class="select-template-header">Select Template</span>'
					+'<select id="select-add-template-select"></select>'+
					c_button('button-add-template-select', 'Add template')+
					c_button('button-view-templates-in-tree', 'View in Tree'));
		
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
			
			// make it a nice combo box, including an additional description of the template
			domElement.extendedcombobox({
				minWidth: 450,
				minHeight: 250,
				createInfoDiv: function() {
					return '<div class="term-description-content"></div>';
				},
				createInfoDivContent: function(item) {
					var template = templates[item.value];
					return renderTemplateDesc(template);
				}
			});
			
			// click handler for adding a selected template
			jQuery('#button-add-template-select').click(function (){
				var intIndex = jQuery('#select-add-template-select').val();
				termTemplateWidgetList.addTemplate(templates[intIndex]);
			});
		}

		function renderTemplateDesc(template, styleClass) {
			var layout;
			if (!styleClass) {
				layout = createLayoutTableOpenTag();
			}else {
				layout = '<table class="termgenie-layout-table '+styleClass+'" cellSpacing="0" cellPadding="0">'
			}
			layout += '<tr><td>Name</td><td>'+getTemplateName(template)+'</td></tr>';
			if (template.description && template.description.length > 0) {
				layout += '<tr><td>Description</td><td>'+template.description+'</td></tr>';
			}
			layout += '<tr><td>Ontology Fields:</td></tr>';
			jQuery.each(template.fields, function(index, field){
				if(field.ontology !== undefined && field.ontology !== null) {
					var names = getOntologyName(field.ontology);
					layout += '<tr><td>'+field.name+'</td><td>'+names+'</td></tr>';
				}
			});
			layout += '</table>';
			return layout;
		}
		
		function TemplateTreeWidget(templates) {
			// create template tree from the categories in the templates
			// create a tree in memory
			var templateTreeDataStructure = {
					children: {}
			};
			jQuery.each(templates, function(intIndex, objValue) {
				var templateName = getTemplateName(objValue);
				if (objValue.categories && objValue.categories !== null) {
					jQuery.each(objValue.categories, function(catIndex, category) {
						handleCategory(category, templateName, objValue, templateTreeDataStructure);
					});
				}
			});
			
			function handleCategory(category, templateName, templateObj, templateTree) {
				if (category === null) {
					// leaf node
					templateTree.children[templateName] = {
						type: 'template',
						label: templateName,
						template: templateObj 
					};
				}
				else {
					var currentCategory;
					var trailingCategory;
					
					// check whether the category contains a slash as path separator
					var slashPos = category.indexOf('/');
					if (slashPos < 0) {
						// no slash
						currentCategory = category;
						trailingCategory = null;
					}
					else {
						// has a slash
						currentCategory = category.substring(0, slashPos);
						trailingCategory = category.substr(slashPos+1);
					}
					
					// create node if necessary
					var node = templateTree.children[currentCategory];
					if (node === undefined) {
						node = {
							type: 'meta',
							label : currentCategory,
							children: {}
						};
						templateTree.children[currentCategory] = node;
					}
					// recursion
					handleCategory(trailingCategory, templateName, templateObj, node);
				}
			}
			
			// render nodes of the tree as un-ordered list
			// this list, will be later converted into a tree
			function renderNode(node, elemParent) {
				var currentElem = jQuery('<li></li>');
				currentElem.append('<a href="#">'+node.label+'</a>')
				if (node.type === 'meta') {
					currentElem.attr('rel', 'meta');
					currentElem.attr('class', 'termgenie-select-template-tree-node');
					if (node.children) {
						var childrenParent = jQuery('<ul></ul>');
						jQuery.each(node.children, function(intIndex, childNode) {
							renderNode(childNode, childrenParent);
						});
						currentElem.append(childrenParent);
					}
				}
				else if (node.type === 'template'){
					currentElem.data('termTemplate', node.template);
					
					// add tooltip to rendered node
					// add an empty title tag, otherwise the widget 
					// does not render the tooltip!
					currentElem.attr('title', '');
					// add class to hide icons on leaf nodes
					currentElem.attr('class', 'jstree-no-icons termgenie-select-template-tree-node');
					var tooltipContent = renderTemplateDesc(node.template, 'termgenie-select-template-tree-tooltip');
					currentElem.tooltip( {
						content: tooltipContent
					});
				}
				elemParent.append(currentElem);
			}
			
			var templateTreeContent = jQuery('<ul></ul>');
			jQuery.each(templateTreeDataStructure.children, function(intIndex, node) {
				renderNode(node, templateTreeContent);
			});
			var templateTreeDiv = jQuery('<div class="termgenie-select-template-tree"></div>');
			templateTreeDiv.append(templateTreeContent);
			
			
			// create a jstree with: 
			// * classic theme,
			// * two node types: default and meta
			// * search: hide non-matching nodes
			// * use 'ui' to handle selection, allow only one selection
			// * render the exisiting list from html as tree
			templateTreeDiv.jstree({
				'themes' : {
					'theme' : "classic",
					'dots' : true,
					'icons' : true
				},
				'types' : {
					// the default type
					'default' : {
						'max_children'  : -1,
						'max_depth'     : -1,
						'valid_children': 'all',
						'select_node' : true,
						'hover_node' : true
					},
					'meta' : {
						'max_children'  : -1,
						'max_depth'     : -1,
						'valid_children': 'all',
						'select_node' : false,
						'hover_node' : false
					}
				},
				'search': {
					'show_only_matches': true
				},
				'ui': {
					'select_limit': 1
				},
				'plugins' : [ 'themes', 'types', 'ui', 'html_data', 'search', 'sort' ]
			});
			
			return {
				getSelected: function() {
					var result = null;
					var currentSelection = templateTreeDiv.jstree('get_selected');
					if (currentSelection && currentSelection !== null && currentSelection.length > 0) {
						// retrieve the stored template
						var template = jQuery.data(currentSelection[0], 'termTemplate');
						if (template && template !== null) {
							result = template;
						}
					}
					return result;
				},
				appendTo: function(parentElem) {
					// add search box with two buttons: 'search' and 'clear'
					var treeSearchInputField = jQuery('<input type="text"></input>');
					var treeSearchButton = jQuery('<button type="button" >Search</button>');
					treeSearchButton.click(function(){
						var query = treeSearchInputField.val();
						if (query && query !== null && query.length > 2) {
							templateTreeDiv.jstree('search', query);
						}
					});
					
					var treeClearSearchButton = jQuery('<button type="button" >Clear</button>');
					treeClearSearchButton.click(function(){
						templateTreeDiv.jstree('clear_search');
						treeSearchInputField.val('');
					});
					
					var treeSearchDiv = jQuery('<div></div>');
					treeSearchDiv.append(treeSearchInputField);
					treeSearchDiv.append(treeSearchButton);
					treeSearchDiv.append(treeClearSearchButton);
					
					parentElem.append(treeSearchDiv);
					
					// add tree
					parentElem.append(templateTreeDiv);
				}
			}
		}
		
		var templateTreeWidgetInstance = new TemplateTreeWidget(templates);
		
		if (renderTreeOnly === false) {
			// create dialog
			var templateTreeDialog = jQuery('<div title="Available TermGenieTemplates"></div>');
			templateTreeWidgetInstance.appendTo(templateTreeDialog);
			templateTreeDialog.dialog({
				autoOpen: false,
				minWidth: 500,
				minHeight: 450,
				modal: true,
				buttons: {
					"Use template": function() {
						var currentSelection = templateTreeWidgetInstance.getSelected();
						if (currentSelection !== null) {
							
							// add the current selection from the tree
							termTemplateWidgetList.addTemplate(currentSelection);
							
							// only exit dialog, if the there was a selection
							$(this).dialog( "close" );
						}
					},
					Cancel: function() {
						$(this).dialog( "close" );
					}
				}
			});
			
			// click handler for 'view in tree' button
			jQuery('#button-view-templates-in-tree').click(function (){
				templateTreeDialog.dialog( "open" );
			});
		}
		else {
			// create a tree only selection
			var templateTreeDiv = jQuery('<div></div>');
			templateTreeWidgetInstance.appendTo(templateTreeDiv);
			var treeAddButton = jQuery('<button type="button" style="margin-top:10px;margin-left:2px">Use template</button>');
			treeAddButton.click(function (){
				var currentSelection = templateTreeWidgetInstance.getSelected();
				if (currentSelection !== null) {
					// add the current selection from the tree
					termTemplateWidgetList.addTemplate(currentSelection);
				}
			});
			treeAddButton.appendTo(templateTreeDiv);
			parent.append(templateTreeDiv);
		}
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
				templateContainer.append('<div class="termgenie-term-template-hint">'+createLayoutTableOpenTag()+'<tr><td class="termgenie-term-template-hint">Hint:</td><td class="hint-content">'+template.hint+'</td></tr></table></div>');
			}
			templateContainer.append('<div id="'+id+'"></div>');
		}
		
		function privateAddTemplate(template) {
			var templateListContainer = templateMap[template.name];
			if (!templateListContainer) {
				templateListContainer = {
					list : [],
					id : 'div-all-template-parameters-'+template.name,
					wrapperId : 'div-all-template-parameters-wrapper-'+template.name
				}
				createTemplateSubList(template, templateListContainer.id, templateListContainer.wrapperId);
				templateMap[template.name] = templateListContainer;
			}
			var templateWidget = TermTemplateWidget(template, templateListContainer.list.length);
			
			var listElem = jQuery('#'+templateListContainer.id);
			
			// if the list was empty create the layout 
			if (templateListContainer.list.length === 0) {
				templateWidget.createLayout(listElem);
			}
			// append a new line to the list
			var domId = templateListContainer.id+'-'+templateListContainer.list.length;
			templateWidget.appendLine(listElem, domId);
			
			templateListContainer.list.push(templateWidget);
		}
		
		function privateRemoveTemplate(template) {
			var templateListContainer = templateMap[template.name];
			if (templateListContainer) {
				if (templateListContainer.list.length > 1) {
					var pos = templateListContainer.list.length - 1;
					var domId = templateListContainer.id+'-'+pos;
					jQuery('#'+domId).remove();
					templateListContainer.list.pop();
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
					var fieldLabel = field.name;
					if (field.label && field.label !== null) {
						fieldLabel = field.label;
					}
					secondRow += '<td><span class="termgenie-term-template-field-name">'+fieldLabel+'</span></td>';
					thirdRow += '<td><span class="termgenie-term-template-field-ontologies">';
					if (field.ontology !== undefined && field.ontology !== null) {
						thirdRow += getShortOntologyName(field.ontology);
					}
					if (field.hint !== undefined && field.hint.length > 0) {
						thirdRow += field.hint;
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
				var trElement = jQuery('<tr id="'+domId+'"></tr>');
				elem.find('tbody').first().append(trElement);
				
				jQuery.each(templateFields, function(i, field){
					trElement.append('<td></td>');
					var tdElement = trElement.children().last();
					
					if (field.remoteResource && field.remoteResource !== null) {
						if (field.remoteResource === 'xref') {
							// xrefs
							fetchRemoteResource('xref', function(xrefs){
								var choices = [];
								jQuery.each(xrefs, function(index, pair){
									if(pair.value !== undefined && pair.value !== null) {
										choices.push(pair.value);
									}
								});
								// create field with choices
								createField(field, i, tdElement, choices);
							});
							
						}
						else if (field.remoteResource === 'orchid') {
							// orchid
							fetchRemoteResource('orchid', function(orchids){
								var choices = [];
								jQuery.each(orchids, function(index, pair){
									if(pair.value !== undefined && pair.value !== null) {
										choices.push(pair.value);
									}
								});
								// create field with choices
								createField(field, i, tdElement, choices);
							});
						}
						else {
							createField(field, i, tdElement);
						}
					}
					else {
						createField(field, i, tdElement);
					}
				});
				
				function createField(field, i, tdElement, choices) {
					if (field.ontology !== undefined && field.ontology !== null) {
						var cardinality = field.cardinality;
						if (cardinality.min === 1 && cardinality.max === 1) {
							var prefixes = field.functionalPrefixes;
							if (prefixes && prefixes.length > 0) {
								inputFields[i] = AutoCompleteOntologyInputPrefix(tdElement, i, field.ontology, prefixes, field.functionalPrefixesIds, field.preSelected);
							}
							else {
								var optionalTerm = field.required === false;
								inputFields[i] = AutoCompleteOntologyInput(tdElement, i, field.ontology, optionalTerm);	
							}
						}
						else {
							inputFields[i] = AutoCompleteOntologyInputList(tdElement, i, field.ontology, cardinality.min, cardinality.max);
						}
					}
					else {
						var cardinality = field.cardinality;
						if (cardinality.min === 1 && cardinality.max === 1) {
							var size = undefined;
							if (field.name === 'Comment') {
								size = 75;
							}
							inputFields[i] = TextFieldInput(tdElement, i, undefined, undefined, size);
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
							inputFields[i] = TextFieldInputList(tdElement, i, cardinality.min, cardinality.max, validator, choices);
						}
					}
				}
			},
			
			/**
			 * extract and validate all input fields for this template.
			 * 
			 * @param extractionResult {ExtractionResult} status object
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
						terms:    {},
						strings:  {}
					};
				
				jQuery.each(templateFields, function(index, templatefield) {
					var inputField =  inputFields[index];
					var csuccess = inputField.extractParameter(parameter, template, templatefield, 0, extractionResult);
					success = success && csuccess;
				});
				
				return {
					success: success,
					input: {
						termTemplate: template,
						termGenerationParameter: parameter
					}
				};
				
			}
		};
	
		/**
		 * Simple input field widget for text, with an optional validator.
		 * 
		 * @param elem {DOM element} parent element
		 * @param templatePos {int} position in the term template
		 * @param validator {function} validator function (optional)
		 * 
		 * @returns functions for the widget (i.e. extractParameter())
		 */
		function TextFieldInput(elem, templatePos, validator, choices, size) {
			var inputElement;
			if (size !== undefined && size > 0) {
				inputElement = jQuery('<input type="text" size="'+size+'"/>');
			}
			else {
				inputElement = jQuery('<input type="text"/>');
			}
			if (choices && choices !== null) {
				inputElement.autocomplete({
					source: choices
				});
			}
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
				 * Extract the user input from the input field.
				 * 
				 * @param parameter {JsonTermGenerationParameter}
				 * @param template {JsonTermTemplate} termgeneration template
				 * @param field {JsonTemplateField}
				 * @param pos {int}
				 * @param extractionResult {ExtractionResult} status object
				 * 
				 * @returns boolean
				 */
				extractParameter : function(parameter, template, field, pos, extractionResult) {
					var success;
					clearErrorState();
					if (!pos) {
						pos = 0;
					}
					var text = inputElement.val();
					if (text !== null && text.length > 0) {
						if (validator && validator !== null) {
							success = validator(text, template, field, extractionResult);
							if(success === false) {
								setErrorState();
								return false;
							}
						}
						var list = parameter.strings[field.name];
						if (!list) {
							list = [];
							parameter.strings[field.name] = list;
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
		
		/**
		 * Input field widget for a list of text fields.
		 * 
		 * @param container {DOM element} parent element
		 * @param templatePos {int} position in the term template
		 * @param min {int} minimum number of fields
		 * @param max {int} maximum number of fields
		 * @param validator {function} validator function (optional)
		 * 
		 * @returns functions for the widget (i.e. extractParameter())
		 */
		function TextFieldInputList(container, templatePos, min, max, validator, choices) {
			
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
					var inputElem = TextFieldInput(tdElement, templatePos, validator, choices);  
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
				/**
				 * Extract the user input from the input field.
				 * 
				 * @param parameter {JsonTermGenerationParameter}
				 * @param template {JsonTermTemplate} termgeneration template
				 * @param field {JsonTemplateField}
				 * @param pos {int}
				 * @param extractionResult {ExtractionResult} status object
				 * 
				 * @returns boolean
				 */
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
		
		/**
		 * Input field widget with auto-completion for a single ontology terms. 
		 * 
		 * @param elem {DOM element} parent element
		 * @param templatePos {int} position in the term template
		 * @param currentOntology {String} ontologies to be searched
		 * @param optionalTerm boolean if true, allow null entries
		 * 
		 * @returns functions for the widget (i.e. extractParameter())
		 */
		function AutoCompleteOntologyInput(elem, templatePos, currentOntology, optionalTerm) {
			
			var inputElement = jQuery('<input/>');
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
					// prepare rpc request data
					var term = request.term;
					requestIndex += 1;
					var myRequestIndex = requestIndex;
					
					mySession.getSessionId(function(sessionId){
						jsonService.ontology.autocomplete({
							params:[sessionId, term, currentOntology, 5],
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
			})
			
			return {
				/**
				 * Extract the user input from the input field.
				 * 
				 * @param parameter {JsonTermGenerationParameter}
				 * @param template {JsonTermTemplate} termgeneration template
				 * @param field {JsonTemplateField}
				 * @param pos {int}
				 * @param extractionResult {ExtractionResult} status object
				 * 
				 * @returns boolean
				 */
				extractParameter : function(parameter, template, field, pos, extractionResult) {
					clearErrorState();
					if (!pos) {
						pos = 0;
					}
					var term = inputElement.extendedautocomplete( "getSelected" );
					if (term && term !== null) {
						var text = inputElement.val();
						if (term.label == text) {
							var identifier = term.identifier;
							var list = parameter.terms[field.name];
							if (!list) {
								list = [];
								parameter.terms[field.name] = list;
							}
							list[pos] = identifier;
							return true;
						}
					}
					if (optionalTerm === true) {
						return true;
					}
					else {
						extractionResult.addError('No valid term. Please specify a term from '+
							getShortOntologyName(field.ontology), template, field);
						setErrorState();
						return false;
					}
				}
			};
		}
		
		/**
		 * Input field widget with auto-completion for a list of ontology terms. 
		 * 
		 * @param container {DOM element} parent element
		 * @param templatePos {int} position in the term template
		 * @param currentOntology {String} ontologies to be searched
		 * @param min {int} minimum number of fields
		 * @param max {int} maximum number of fields
		 * 
		 * @returns functions for the widget (i.e. extractParameter())
		 */
		function AutoCompleteOntologyInputList(container, templatePos, currentOntology, min, max) {
			
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
					list.push(AutoCompleteOntologyInput(tdElement, templatePos, currentOntology));
				}
			}
			
			function removeInput() {
				if (list.length > min) {
					listParent.find('tr').last().remove();
					list.pop();
				}
			}
			
			return {
				/**
				 * Extract the user input from the input field.
				 * 
				 * @param parameter {JsonTermGenerationParameter}
				 * @param template {JsonTermTemplate} termgeneration template
				 * @param field {JsonTemplateField}
				 * @param pos {int}
				 * @param extractionResult {ExtractionResult} status object
				 * 
				 * @returns boolean
				 */
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
		
		/**
		 * Input field widget with auto-completion for a single ontology term 
		 * and a list of prefixes. 
		 * 
		 * @param elem {DOM element} parent element
		 * @param templatePos {int} position in the term template
		 * @param currentOntology {String} ontologies to be searched
		 * @param prefixes {String[]} list of prefixes
		 * @param prefixesIds {String[]} list of prefixesIds
		 * @param preselect {boolean} pre-select prefix checkboxes
		 * 
		 * @returns functions for the widget (i.e. extractParameter())
		 */
		function AutoCompleteOntologyInputPrefix (elem, templatePos, currentOntology, prefixes, prefixesIds, preselect) {
			if (preselect === undefined || preselect === null) {
				preselect = true;
			}
			if (!prefixesIds || prefixesIds == null || prefixesIds.length < prefixes.length) {
				prefixesIds = prefixes
			}
			
			var checkbox, i, j;
			
			var container = createLayoutTable();
			container.appendTo(elem);
			var inputContainer = jQuery('<tr><td class="prefixInputFieldCell"></td></tr>');
			inputContainer.appendTo(container);
			
			var prefixContainer = jQuery('<tr><td class="prefixInputFieldCell"></td></tr>');
			prefixContainer.appendTo(container);
			
			var inputField = AutoCompleteOntologyInput(inputContainer.children(":first"), templatePos, currentOntology);
			
			var internalTable = createLayoutTable();
			var checkboxes = [];
			for ( i = 0; i < prefixes.length; i++) {
				var row = jQuery('<tr><td></td></tr>');
				if (preselect === true) {
					checkbox = jQuery('<input type="checkbox" checked="true"/>');
				}
				else {
					checkbox = jQuery('<input type="checkbox"/>');
				}
				
				checkboxes[i] = checkbox;
				
				row.children(":first").append(checkbox);
				row.append('<td><span class="term-prefix-label"> '+prefixes[i]+' </span></td>');
				internalTable.append(row);
			}
			internalTable.appendTo(prefixContainer.children(":first"));
			
			function clearErrorState() {
				container.removeClass('termgenie-input-field-error');	
			}
			
			function setErrorState() {
				container.addClass('termgenie-input-field-error');
			}
			
			return {
				/**
				 * Extract the user input from the input field.
				 * 
				 * @param parameter {JsonTermGenerationParameter}
				 * @param template {JsonTermTemplate} termgeneration template
				 * @param field {JsonTemplateField}
				 * @param pos {int}
				 * @param extractionResult {ExtractionResult} status object
				 * 
				 * @returns boolean
				 */
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
							cPrefixes.push(prefixesIds[j]);
						}
					}
					if (cPrefixes.length === 0) {
						setErrorState();
						extractionResult.addError('No prefixes selected.', template, field);
						return false;
					}
					parameter.strings[field.name] = cPrefixes;
					return success;
				}
			};
		}
	}
	
	/**
	 * Status object for the extraction of term generation parameters 
	 * from the input widgets.
	 * 
	 * @returns Methods for the ExtractionResult 
	 * {
	 * 	addError: function(),
	 *  isSuccessful: function(),
	 *  getErrors: function()
	 * }
	 */
	function ExtractionResult() {
		var success = true;
		var errors = [];
		
		return {
			/**
			 * add an error to the list of encountered problems.
			 * 
			 * @param error {String}
			 * @param template
			 * @param field
			 */
			addError : function(error, template, field) {
				success = false;
				errors.push({
					message: error,
					template: template,
					field: field
				});
			},
			/**
			 * Check weather the extraction was successful
			 * 
			 * @returns {boolean}
			 */
			isSuccessful: function() {
				return success;
			},
			
			/**
			 * @returns list of errors
			 */
			getErrors: function() {
				return errors;
			}
		};
	}
	
	/**
	 * Create the dynamic part of the user panel
	 */
	function createUserPanel() {
		var checkBoxElem = jQuery('#checkbox-try-commit');
		var checkBoxSendSubmitEmail = jQuery('#checkbox-send-submit-email');
		return {
			submit : function (terms, ontology) {
				// select mode
				var isCommit = checkBoxElem.is(':checked');
				
				// set header in the accordion
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
				
				var sendConfirmationEMail = checkBoxSendSubmitEmail.is(':checked');
				
				// open next tab in the accordion
				var step4Container = jQuery('#termgenie-step4-content-container');
				step4Container.empty();
				var progressInfo = ProgressInfoWidget(step4Container, 6, false);
				myAccordion.enablePane(3);
				myAccordion.activatePane(3);
				
				if (isCommit) {
					step4Container.append(createBusyMessage('Executing commit request on the server.'));
					// try to commit
					mySession.getSessionId(function(sessionId){
						jsonService.commit.commitTerms({
							params: [sessionId, terms, sendConfirmationEMail],
							onSuccess: function(result) {
								step4Container.empty();
								renderCommitResult(result, step4Container);
							},
							onException: function(e) {
								step4Container.empty();
								jQuery.logSystemError("CommitTerms service call failed", e);
								return true;
							},
							onProgress: function(uuid) {
								jsonService.progress.getProgress({
									params:[uuid],
									onSuccess: function(messages) {
										progressInfo.addMessages(messages);
									}
								});
							}
						});
					});
				} else {
					step4Container.append(createBusyMessage('Preparing terms for export on the server.'));
					// just generate the info for the export a obo/owl
					mySession.getSessionId(function(sessionId){
						jsonService.commit.exportTerms({
							params: [sessionId, terms],
							onSuccess: function(result) {
								step4Container.empty();
								renderExportResult(result, step4Container);
							},
							onException: function(e) {
								step4Container.empty();
								jQuery.logSystemError("ExportTerms service call failed", e);
								return true;
							}
						});
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
	 *         hint: String;
	 *     }[],
	 *     warnings: JsonValidationHint{
	 *         template: JsonTermTemplate,
	 *         field: int,
	 *         hint: String;
	 *     }[],
	 *     generatedTerms: JsonOntologyTerm {
	 *     	   id: String,
	 *     	   label: String,
	 *         synonyms: JsonSynonym[],
	 *     	   definition: String,
	 *     	   logDef: String,
	 *     	   comment: String,
	 *     	   defXRef: String[],
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
			return;
		}
		
		if(isValid(generationResponse.warnings)) {
			renderWarnings(container, generationResponse.warnings);
		}

		var reviewTerms = null;

		if(isValid(generationResponse.generatedTerms)) {
			reviewTerms = renderGeneratedTerms(parent, generationResponse.generatedTerms, generationResponse.termTemplates);

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
				trElement.append('<td><span class="warn-level-error">Error</span></td>');
				trElement.append('<td>' + validationHint.hint +'</td>');
			});
		}
		
		function renderWarnings(parent, warnings) {
			var detailedErrorContainer = jQuery('<div class="term-generation-detailed-errors"></div>');
			detailedErrorContainer.appendTo(parent);
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-heading">Warning Messages</div>');
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Your request produced the following list of warnings.</div>');
			if (warnings.length > 11) {
				detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">There are '+warnings.length+' warnings. Only the first 10 are shown.</div>');
				warnings = warnings.slice(0,10);
			}
			var layout = jQuery('<table cellpadding="5"></table>');
			detailedErrorContainer.append(layout);
			detailedErrorContainer.append('<div class="term-generation-detailed-errors-description">Please consider the messages and try to fix them, by changing the input from the previous step.</div>');
			
			layout.append('<thead><tr><td>Template</td><td>Field</td><td>Level</td><td>Message</td></tr></thead>');
			
			jQuery.each(warnings, function(index, validationHint){
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
				trElement.append('<td><span class="warn-level-warn">Warning</span></td>');
				trElement.append('<td>' + validationHint.hint +'</td>');
			});
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
		function renderGeneratedTerms(parent, generatedTerms, termTemplates) {
			var checkBoxes = [];
			var termPanels = [];
			var generatedTermContainer = jQuery('<div class="term-generation-details"></div>');
			generatedTermContainer.appendTo(container);
			
			generatedTermContainer.append('<div class="term-generation-details-heading">Proposed new terms by TermGenie</div>');
			generatedTermContainer.append('<div class="term-generation-details-description">Your request produced the following list of term candidates:</div>');
			//generatedTermContainer.append('<div>Optional: Create an image with the generated term hierarchy: <button id="Generated-Terms-Hierarchy-Preview-Button">Generate Image</button> (Opens a new window.)</div>');
			var layout = jQuery('<table cellpadding="5" class="termgenie-proposed-terms-table"></table>');
			generatedTermContainer.append(layout);
			
			jQuery.each(generatedTerms, function(index, term){
				var trElement = jQuery('<tr></tr>');
				trElement.appendTo(layout);
				var tdElement = jQuery('<td></td>');
				tdElement.appendTo(trElement);
				var checkBox = jQuery('<input type="checkbox"/>');
				checkBox.appendTo(tdElement);
				checkBoxes.push(checkBox);

				var termPanel = TermReviewPanel(trElement, term, termTemplates[index]);
				termPanels.push(termPanel);
			});
			generatedTermContainer.append('<div class="term-generation-details-description">Please select the term(s) for the final step.</div>');
			
			
			// add button to render terms in hierarchy image
			/*
			var renderHierarchyButton = jQuery('#Generated-Terms-Hierarchy-Preview-Button');
			renderHierarchyButton.click(function(){
				jsonService.renderer.visualizeGeneratedTerms({
					params: [generatedTerms],
					onSuccess: function(result) {
						if(result.success === true) {
							window.open(result.message);
						}
						else {
							jQuery.logUserMessage('Render Hierarchy service call failed', result.message);
							jQuery.openLogPanel();
						}
						
					},
					onException: function(e) {
						jQuery.logSystemError('Render Hierarchy service call failed', e);
					}
				});
			});
			*/
			
			return {
				checkBoxes: checkBoxes,
				termPanels: termPanels
			};
		}
		
		/**
		 * Handle the review of one term.
		 * 
		 * @param parent
		 * @param term
		 * @returns methods of the panel
		 */
		function TermReviewPanel(parent, term, template) {
			// clone term, preserve original term for comparison
			var newTerm = jQuery.extend(true, {}, term);
			
			// use a new table to layout each term
			var elem = jQuery('<td class="termgenie-review-panel-term-details-frame"></td>');
			parent.append(elem);
			
			var interalLayoutTable = createLayoutTable();
			interalLayoutTable.addClass('termgenie-review-panel-term-details-table');
			elem.append(interalLayoutTable);
			
			interalLayoutTable.append('<tr class="header"><td>Temporary Term Identifier</td></tr>');
			interalLayoutTable.append('<tr class="values"><td class="termgenie-pre nobr termgenie-temp-identfier">'+term.tempId+'</td></tr>');
			
			interalLayoutTable.append('<tr class="header"><td>Label</td><td>Definition</td><td>Def_XRef</td></tr>');
			var trElement = jQuery('<tr class="values"></tr>');
			interalLayoutTable.append(trElement);
			
			// render the invidual fields
			var fieldPanels = {};
			var tdElement;
			
			//Label
			tdElement  = jQuery('<td></td>');
			trElement.append(tdElement);
			fieldPanels.label = StringFieldReviewPanel(tdElement, term.label);
			
			//Definition
			tdElement  = jQuery('<td></td>');
			trElement.append(tdElement);
			fieldPanels.definition = StringFieldReviewPanel(tdElement, term.definition);
			
			// Def_XRefs
			tdElement  = jQuery('<td></td>');
			trElement.append(tdElement);
			fieldPanels.defXRef = StringListFieldReviewPanel(tdElement, term.defXRef);
			
			// new header and row
			if (term.relations !== undefined && term.relations !== null) {
				interalLayoutTable.append('<tr class="header"><td>Synonyms</td><td>MetaData</td><td>Relations</td></tr>')
			}
			else {
				interalLayoutTable.append('<tr class="header"><td>Synonyms</td><td>MetaData</td></tr>')
			}
			trElement = jQuery('<tr class="values"></tr>');
			interalLayoutTable.append(trElement);
			
			// synonyms
			tdElement  = jQuery('<td></td>');
			trElement.append(tdElement);
			fieldPanels.synonyms = SynonymListReviewPanel(tdElement, term.synonyms, template);
			
			// Metadata
			tdElement  = jQuery('<td></td>');
			trElement.append(tdElement);
			MetaDataFieldReviewPanel(tdElement, term.metaData);
			
			// Relations
			if (term.relations !== undefined && term.relations !== null) {
				tdElement  = jQuery('<td></td>');
				trElement.append(tdElement)
				RelationFieldReviewPanel(tdElement, term.relations);
			}
			
			// changed Relations
			if(term.changed && term.changed.length > 0) {
				interalLayoutTable.append('<tr class="header"><td colspan="3" >Changed Relations</td></tr>')
				trElement = jQuery('<tr class="values"></tr>');
				interalLayoutTable.append(trElement);
				tdElement  = jQuery('<td colspan="3"></td>');
				trElement.append(tdElement);
				ChangedRelationsFieldReviewPanel(tdElement, term.changed);
			}
			
			return {
				/**
				 * Retrieve the term from the panel.
				 * 
				 * @returns term
				 */
				getTerm : function(){
					jQuery.each(fieldPanels, function(field, panel) {
						newTerm[field] = panel.getValue();
					});
					// do not read any changes from meta data or relations
					return newTerm;
				}
			};
			
			/**
			 * Create a review field for a string.
			 * 
			 * @param parent {DOM element}
			 * @param string {String} value
			 * 
			 * @returns Methods for the review panel (i.e. getValue())
			 */
			function StringFieldReviewPanel(parent, string) {
				var reviewField = createInputField(string);
				reviewField.appendTo(parent);
				
				return {
					getValue : function () {
						return normalizeString(reviewField.val());
					}
				};
			}
			
			/**
			 * Create a review field for a string array.
			 * 
			 * @param parent {DOM element}
			 * @param strings {String[]} values
			 * 
			 * @returns Methods for the review panel (i.e. getValue())
			 */
			function StringListFieldReviewPanel(parent, strings) {
				var listParent = createLayoutTable();
				var rows = [];
				listParent.appendTo(parent);
				
				var table = createLayoutTable();
				table.appendTo(parent);
				if (strings && strings.length > 0) {
					jQuery.each(strings, function(index, value){
						addLine(value);
					});
				}
				
				function addLine(value) {
					if (!value || value.length === 0) {
						// do nothing
						return;
					}
					var tableCell = jQuery('<tr></tr>');
					var tdCell = jQuery('<td></td>');
					tdCell.appendTo(tableCell);
					var checkbox = jQuery('<input type="checkbox" checked="true"/>');
					checkbox.appendTo(tdCell);
					
					var inputField = jQuery('<td>'+value+'</td>');
					inputField.appendTo(tableCell);
					tableCell.appendTo(listParent);
					rows.push({
						checkbox : checkbox,
						value : value
					});
				}
				
				return {
					getValue : function () {
						var strings = [];
						jQuery.each(rows, function(index, element){
							if(element.checkbox.is(':checked')) {
								strings.push(element.value);
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
			 * Render the synonyms and methods for identifiying 
			 * synonyms selected by the user.
			 * 
			 * @param parent
			 * @param synonyms
			 * @param template
			 * @returns method for selected synonyms
			 */
			function SynonymListReviewPanel(parent, synonyms, template) {
				
				parent.css('height','100%');
				var divElem = jQuery('<div></div>');
				parent.append(divElem);
				
				
				var listParent = createLayoutTable();
				listParent.css('width','350px');
				var rows = [];
				listParent.appendTo(divElem);
				
				var showCategory = false;
				
				if (synonyms && synonyms.length > 0) {
					jQuery.each(synonyms, function(index, synonym){
						showCategory = showCategory || (synonym.category && synonym.category.length > 0);
					});
					
					var header = '<tr><td></td><td>Label</td><td>Scope</td>';
					if (showCategory === true) {
						header += '<td>Category</td>';
					}
					header += '</tr>';
					listParent.append(header);
					
					jQuery.each(synonyms, function(index, synonym){
						addLine(synonym, showCategory);
					});
				}
				
				// button for additional synonyms
				var addSynonymButton = jQuery('<button>Add synonym</button>');
				addSynonymButton.appendTo(divElem);
				
				// get remote resources for xref auto-complete
				var xrefChoices = null;
				
				fetchRemoteResource('xref', function(xrefs) {
					jQuery.each(xrefs, function(index, pair){
						if(pair.value !== undefined && pair.value !== null) {
							if (xrefChoices === null) {
								xrefChoices = [];
							}
							xrefChoices.push(pair.value);
						}
					});
				});
				
				// implement add synonym dialog
				addSynonymButton.click(function(){
					var editDialog = jQuery('<div style="width:100%;heigth:100%;display: block;"></div>');
					var newSynonymFields = [];
					
					editDialog.append('<div class="termgenie-add-synonym-hint">This dialog allows to create additonal synonyms. '+
							'Invalid entry lines are ignored. This includes empty or redundant labels and malformed xrefs</div>');
					var editDialogTable = createLayoutTable();
					editDialogTable.append('<tr>'
							+'<th>Synonym Label</th>'
							+'<th>Scope</th>'
							+'<th>XRef</th>'
							+'</tr>');
					editDialog.append(editDialogTable);
					
					// create first synonymsField
					newSynonymFields.push(synonymField(editDialogTable));
					
					editDialog.dialog({
						title: "Add Synonyms",
						resizable: true,
						height:400,
						width: 700,
						minHeight: 200,
						minWidth: 600,
						modal: true,
						closeOnEscape: false,
						buttons: {
							"More": function() {
								newSynonymFields.push(synonymField(editDialog));
							},
							"Done": function() {
								jQuery.each(newSynonymFields, function(index, newSynonymField) {
									var newSynonym = newSynonymField.getSynonym();
									if (newSynonym && newSynonym !== null) {
										addLine(newSynonym, showCategory);
									}
								});
								$( this ).dialog( "close" );
							},
							"Cancel": function() {
								$( this ).dialog( "close" );
							}
						}
					});
					
					/**
					 * Object holding the input data for an additional synonym.
					 * 
					 * @param editDialog the parent object
					 */
					function synonymField(editDialogRow) {
						
						// TODO implement
						
						var synonymFieldRow = jQuery('<tr class="termgenie-add-synonym-line"></tr>');
						editDialogRow.append(synonymFieldRow);
						
						// input fields
						// label
						var labelField = jQuery('<input type="text" style="width:350px"/>');
						var labelTD = jQuery('<td></td>');
						labelTD.append(labelField);
						synonymFieldRow.append(labelTD);
						
						// scope selector
						var scopeSelector = jQuery('<select>'+
								'<option value="RELATED">RELATED</option>'+
								'<option value="NARROW">NARROW</option>'+
								'<option value="EXACT">EXACT</option>'+
								'<option value="BROAD">BROAD</option>'+
								'</select>');
						var scopeTD = jQuery('<td></td>');
						scopeTD.append(scopeSelector);
						synonymFieldRow.append(scopeTD);
						
						// list of xrefs, provide autocomplete and sanity check
						
						
						var xrefInputElement = jQuery('<input type="text"/>');
						if (xrefChoices && xrefChoices !== null) {
							xrefInputElement.autocomplete({
								source: xrefChoices
							});
						}
						var xrefTD = jQuery('<td></td>');
						xrefTD.append(xrefInputElement);
						synonymFieldRow.append(xrefTD);
						
						return {
							
							/**
							 * return JsonSynonym or null
							 */
							getSynonym : function() {
								// check that a valid label is selected
								var newLabel = labelField.val();
								if (!newLabel || newLabel === null || newLabel.length <= 1) {
									return null;
								}
								var redundant = false;
								
								// check if label already exists
								jQuery.each(rows, function(index, row){
									if(row.synonym.label === newLabel) {
										redundant = true;
									}
								});
								if (redundant === true) {
									return null;
								}
								
								// scope
								var newScope = scopeSelector.val();
								
								// check that the xref conforms to the pattern
								var newXref = xrefInputElement.val();
								var newXrefs = [];
								if (newXref && newXref !== null && newXref.length >= 3) {
									var pattern = /^\S+:\S+$/; // {non-whitespace}+ colon {non-whitespace}+ [whole string]
									var matching = pattern.test(newXref); 
									if (matching === true) {
										newXrefs.push(newXref);
									}
								}
								
								// otherwise create a new instance of JsonSynonym
								/*
								 * JsonSynonym {
								 *     label : String,
								 *     scope : String,
								 *     category: String, // usually null;
								 *     xrefs : Set<String> // implemented as String[]
								 * }
								 */
								return {
									label: newLabel,
									scope: newScope,
									category: null,
									xrefs: newXrefs
								};
							}
						}
					};
				});
				
				
				return {
					getValue : function () {
						var synonyms = [];
						jQuery.each(rows, function(index, row){
							if(row.checkbox.is(':checked')) {
								row.synonym.scope = row.getScope();
								synonyms.push(row.synonym);
							}
						});
						if (synonyms.length > 0) {
							return synonyms;
						}
						return null;
					}
				};
				
				function addLine(synonym, showCategory) {
					var tableRow = jQuery('<tr></tr>');
					if (rows.length % 2 == 0) {
						tableRow.css('background-color', '#D0D0D0');
					}
					
					// checkbox
					var checkboxCell = jQuery('<td></td>');
					checkboxCell.appendTo(tableRow);
					var checkbox = jQuery('<input type="checkbox" checked="true"/>');
					checkbox.appendTo(checkboxCell);
					
					// label
					tableRow.append('<td>'+synonym.label+'</td>');
					
					// scope
					var scopeCell = jQuery('<td></td>');
					scopeCell.appendTo(tableRow);
					var selectScope = null;
					if (synonym.scope && synonym.scope.length > 0) {
						selectScope = jQuery('<select>'+
								'<option value="RELATED">RELATED</option>'+
								'<option value="NARROW">NARROW</option>'+
								'<option value="EXACT">EXACT</option>'+
								'<option value="BROAD">BROAD</option>'+
								'</select>');
						selectScope.val(synonym.scope);
					}
					else {
						selectScope = jQuery('<select>'+
								'<option value="-" selected=true>-</option>'+
								'<option value="RELATED">RELATED</option>'+
								'<option value="NARROW">NARROW</option>'+
								'<option value="EXACT">EXACT</option>'+
								'<option value="BROAD">BROAD</option>'+
								'</select>');
					}
					scopeCell.append(selectScope);
					
					// category
					if (showCategory === true) {
						var categoryCell = jQuery('<td></td>');
						categoryCell.appendTo(tableRow);
						if (synonym.category && synonym.category.length > 0) {
						 	categoryCell.text(synonym.category);
						}
					}
					
					// DO not render xrefs as is it too repetitive in the GUI
					// xrefs
					// var xrefCell = jQuery('<td></td>');
					// xrefCell.appendTo(tableRow);
					// if (synonym.xrefs && synonym.xrefs.length > 0) {
					// 	var xrefText = '[';
					// 	jQuery.each(synonym.xrefs, function(index, xref){
					// 		if (index > 0) {
					// 			xrefText += ',';
					// 		}
					// 		xrefText += xref;
					// 	});
					// 	xrefText += ']';
					// 	xrefCell.text(xrefText);
					// }
					
					tableRow.appendTo(listParent);
					
					rows.push({
						tableRow : tableRow,
						checkbox : checkbox,
						synonym : synonym,
						getScope : function() {
							var scope = synonym.scope;
							if (selectScope !== null) {
								scope = selectScope.val();
								if (scope == '-') {
									scope = synonym.scope;
								}
							}
							return scope;
						}
					});
				}
			}
			
			/**
			 * Render the meta data. Currently this is read-only.
			 * 
			 * @param parent
			 * @param term
			 * @returns empty object: no external methods
			 */
			function MetaDataFieldReviewPanel(parent, metaData) {
				parent.css('height','100%');
				var divElem = jQuery('<div></div>');
				parent.append(divElem);
				var table = createLayoutTable();
				table.css('width','350px');
				table.appendTo(divElem);
				jQuery.each(metaData, function(index, value) {
					table.append('<tr><td class="nobr">'+value+'</td></tr>');
				});
				return {};
			}
			
			/**
			 * Render the changed relations. This is a read-only field.
			 * 
			 * @param parent
			 * @param jsonChanges
			 */
			function ChangedRelationsFieldReviewPanel(parent, jsonChanges) {
				parent.css('height','100%');
				var divElem = jQuery('<div></div>');
				parent.append(divElem);
				var table = createLayoutTable();
				table.appendTo(divElem);
				
				jQuery.each(jsonChanges, function(index, jsonChange){
					var relList = jsonChange.changes;
					var count = 0;
					jQuery.each(relList, function(relIndex, relString){
						var marked = markupRelation(relString);
						if (marked) {
							if(count === 0) {
								var s = '<tr><td>'+jsonChange.id;
								if (jsonChange.label && jsonChange.label !== null) {
									s += '</td><td>' + jsonChange.label;
								}
								s += '</td></tr>';
								table.append(s);
							}
							table.append('<tr><td></td><td class="termgenie-pre nobr">'+marked+'</td></tr>');
							count += 1;
						}
					});
				});
			}

			/**
			 * Markup temporary identifiers in a relation string.
			 * 
			 * @returns {String} or undefined
			 */
			function markupRelation(relString) {
				// markup relation by splitting it into tokens
				var containsTempId = isTempIdentifier(relString);
				if (containsTempId === true) {
					var replaced = "";
					var words = relString.split(' ');
					jQuery.each(words, function(pos, word){
						if(isTempIdentifier(word)) {
							replaced += '<span class="termgenie-temp-identfier">' + word + '</span> ';
						}
						else {
							replaced += word + ' ';
						}
					});
					return replaced;
				}
				return undefined;
			}
			
			/**
			 * Render the relations. Currently this is read-only.
			 * 
			 * @param parent
			 * @param relations
			 * @returns empty object: no external methods
			 */
			function RelationFieldReviewPanel(parent, relations) {
				parent.css('height','100%');
				var divElem = jQuery('<div></div>');
				parent.append(divElem);
				var table = createLayoutTable();
				table.appendTo(divElem);
				if(relations && relations.length > 0) {
					jQuery.each(relations, function(index, relation){
						var relString = markupRelation(relation);
						if (!relString) {
							relString = relation;
						}
						table.append('<tr><td class="termgenie-pre nobr">'+relString+'</td></tr>');
					});
				}
				else {
					divElem.append('No relations generated.');
				}
				return {
					relations: relations
				};
			}
			
			/**
			 * Create an input field with a default String value
			 * 
			 * @param string
			 * @returns DOM element
			 */
			function createInputField(string) {
				if (!string || typeof string !== 'string') {
					// empty or unknown: create empty field
					return jQuery('<input type="text" style="width:350px"/>');
				}
				var elem;
				if (string.length < 42) {
					// if the text is short use single line
					elem = jQuery('<input type="text" style="width:350px"/>');
				}
				else {
					//  if the text is long use multi-line input
					elem = jQuery('<textarea style="width:350px;height:70px"></textarea>');
				}
				elem.val(string);
				return elem;
			}
			
			/**
			 * Normalize a string in terms of length and whitespaces.
			 * 
			 * @returns {String} string or null
			 */
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
					jQuery.logUserMessage('Please select at least one term to proceed.');
					return;
				}
				
				myUserPanel.submit(terms, ontology);
			});
			
			/*
			 * hide e-mail select
			 */
			jQuery('#div-checkbox-send-submit-email').hide();
			
			/*
			 * change label of submit button depending state of commit checkbox
			 */
			var checkBoxElem = jQuery('#checkbox-try-commit');
			checkBoxElem.change(function(){
				var label = 'Preview';
				if (checkBoxElem.is(':checked')) {
					label = 'Submit';
					jQuery('#div-checkbox-send-submit-email').show();
				}
				else {
					jQuery('#div-checkbox-send-submit-email').hide();
				}
				jQuery('#button-submit-for-commit-or-export').text(label);
				
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
			if(commitResults.message && commitResults.message.length > 0) {
				container.append('<div>' + markupNewLines(commitResults.message) + '</div>');
			}
			if (commitResults.terms && commitResults.terms.length > 0) {
				container.append('<div>The following terms have been created:</div>');
				var termList = jQuery('<ul></ul>');
				jQuery.each(commitResults.terms, function(index, term){
					termList.append('<li style="font-family:monospace;">ID: '+term.tempId+' Label: '+term.label+'</li>');
				});
				container.append(termList);
			}
//			renderResetButton(container);
		}
		else {
			container.append('<div>The commit of the generated terms did not complete normally with the following reason:</div>');
			container.append('<div class="term-generation-commit-error-details">'+markupNewLines(commitResults.message)+'</div>');
		}
		
		function markupNewLines(text) {
			var message = '';
			var lines = text.split('\n');
			jQuery.each(lines, function(pos, line){
				message += line + '</br>\n';
			});
			return message; 
		}
	}
	
	/**
	 * @param exportResult JsonExportResult {
	 * 		success: boolean,
	 * 		message: String,
	 * 		exports: Map/Object{format, content}
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
			var keys = [];
			jQuery.each(exportResult.exports, function(format, content){
				if (content.length > 0) {
					keys.push(format);
				}
			});
			
			keys.sort();
			jQuery.each(keys, function(index, key){
				var content = exportResult.exports[key];
				renderExport(key, content, exportsContainer);
			});
		}
		else {
			container.append('<div>The export of the generated terms did not complete normally with the following reason:</div>');
			container.append('<div class="term-generation-export-error-details">'+exportResult.message+'</div>');
		}
//		renderResetButton(container);
		
		function renderExport(name, content, exportsContainer) {
			exportsContainer.append('<div>'+name+'</div>');
			var replaced = "";
			var lines = content.split('\n');
			jQuery.each(lines, function(index, line){
				var containsTempId = isTempIdentifier(line);
				if (containsTempId === true) {
					var words = line.split(' ');
					jQuery.each(words, function(pos, word){
						if(isTempIdentifier(word)) {
							replaced += '<span class="termgenie-temp-identfier">' + word + '</span> ';
						}
						else {
							replaced += word + ' ';
						}
					});
					replaced += '</br>\n';
				}
				else {
					replaced += line + '</br>\n';
				}
			});
			exportsContainer.append('<div class="termgenie-pre nobr">'+replaced+'</div>');
		}
	}
	
	function renderResetButton(container) {
		var restartDiv = jQuery('<div class="term-generation-commit-restart"><span>Need to create more terms or select templates: </span></div>');
		var restartButton = jQuery('<button type="button">GoTo Step 2</button>');
		restartButton.click(function(){
			myAccordion.activatePane(1);
			myAccordion.disablePane(2);
			jQuery('#span-step2-additional-header').empty();
			myAccordion.disablePane(3);
			jQuery('#span-step3-additional-header').empty();
		});
		restartDiv.append(restartButton);
		container.append(restartDiv);
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
	
	function isTempIdentifier(identifier) {
		return identifier.indexOf('TEMP-') !== -1;
	}
	
	// HTML wrapper functions
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