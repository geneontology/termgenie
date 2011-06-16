$(function() {
// execute when document-ready
	
	/**
	 * Provide an Accordion with the additional functionality to 
	 * enable/disable individual panes for click events.
	 * 
	 * @param id html-id for the accordian div tag
	 */
	function MyAccordion(id) {
		// private variables;
		var selections = {};
		selections.Pane_0 = true;
		selections.Pane_1 = false;
		selections.Pane_2 = false;
		selections.Pane_3 = false;
		
		$(id).accordion({ clearStyle: true, autoHeight: false, event: "" });
		
		// implement a custom click function
		// allow only to open panes, which are enabled in the selections object
		$(id+' h3').click(function() {
			var idx = $(id+' h3').index(this);
			var activate = selections["Pane_" + idx];
			if (activate) {
				$(id).accordion("activate", idx);
			}
		});
		
		return {
			/**
			 * Active the specified panel.
			 * 
			 * @param pos postion to activate (zero-based)
			 */
			activatePane : function(pos) {
				$(id).accordion("activate", pos);
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
	var myAccordion = MyAccordion('#accordion');
	
	//create proxy for json rpc
	var jsonService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: true,
	    methods: ['generate.availableTermTemplates', 
	              'generate.generateTerms', 
	              'ontology.availableOntologies', 
	              'ontology.autocomplete', 
	              'user.isValidUser']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonService, true);

	
	// use json-rpc to retrieve available ontologies
	jsonService.ontology.availableOntologies({
		onSuccess: function(result) {
			/*
			 * Actual start code for the page.
			 * 
			 * Retrieve and create the content for the step 1. 
			 */
			createOntologySelector(result);
			createUserPanel();
		},
		onException: function(e) {
			alert("Unable to compute because: " + e);
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
			$.each(ontologies, function(intIndex, objValue){
				ontselect += '<option value="'+objValue+'">'+objValue+'</option>';
			});
			ontselect += '</select>'+ c_button('select-ontology-button', 'Submit');
			
			// add to div
			elem = $('#div-select-ontology');
			elem.empty();
			elem.append(ontselect);
			
			// register click handler
			$('#select-ontology-button').click(function() {
				var selectedValue = $('#select-ontology-select').val();
				setStep1Header(selectedValue);
				createTemplateSelector(selectedValue);
				setStep2Active(true);
			});
		}
	}
	
	/**
	 * Set the selected ontology name in the header.
	 * 
	 * @param ontology selected ontology name
	 */
	function setStep1Header(ontology) {
		var elem = $('#span-step1-additional-header');
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
	
	/**
	 * Variable for keeping the template widgets 
	 */
	var temTemplateWidgetList = TemTemplateWidgetList();
	
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
		var elem = $('#div-select-templates-and-parameters');
		// clear, from possible previous templates
		elem.empty();
		// append layout
		elem.append(termselect);
		
		// register click handler for submit button
		var submitButton = $('#button-termgeneration-start');
		submitButton.click(function(){
			// TODO verification using Javascript
			// TODO submit to server
			alert("Verification and Generation");
			myAccordion.enablePane(2);
			myAccordion.activatePane(2);
		});
		
		// start async rpc to retrieve available templates
		jsonService.generate.availableTermTemplates({
			params:[ontology],
			onSuccess: function(result) {
				createTemplateSelectorMenu(ontology, result);
			},
			onException: function(e) {
				alert("Unable to compute because: " + e);
				return true;
			}
		});
	}
	
	/**
	 * Create the selection menu for the list of templates
	 * 
	 * @param ontology ontology name
	 * @param templates list of term templates
	 */
	function createTemplateSelectorMenu(ontology, templates) {
		// create layout
		$('#div-template-selector')
			.append('<select id="select-add-template-select"></select>'+
				c_button('button-add-template-select', 'Add template'));
		
		// select dom element
		var domElement = $('#select-add-template-select');

		// foreach template create a menu entry, use index for retrieval
		$.each(templates, function(intIndex, objValue) {
			var templateName = objValue.name;
			var option = $('<option />');
			option.text(templateName);
			option.val(intIndex);
			domElement.append(option);
		});
		
		// click handler for adding a selected template
		$('#button-add-template-select').click(function (){
			var intIndex = $('#select-add-template-select').val();
			temTemplateWidgetList.addTemplate(templates[intIndex]);
		});
	}
	
	/**
	 * Holder of all template widgets. Provides methods for adding 
	 * and removing of widgets.
	 */
	function TemTemplateWidgetList(){
		// private members
		var templateMap = {};
		
		// private methods
		function createTemplateSubList(template, id, wrapperId) {
			var templateContainer = $('<div id="'+wrapperId+'" class="template-list-wrapper"></div>');
			templateContainer.appendTo($('#div-all-template-parameters'));
			var templateTitle = $('<div class="termgenie-template-header">Template: <span class="label-template-name">'+template.name+'</span></div>');
			createAddRemoveWidget(templateTitle, 
					function(){
						privateAddTemplate(template);
					}, 
					function(){
						privateRemoveTemplate(template);
					});
			
			templateContainer.append(templateTitle);
			templateContainer.append('<div id="'+id+'"></div>')
			
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
			var templateWidget = TemTemplateWidget(template,templateListContainer.count);
			templateListContainer.list[templateListContainer.count] = templateWidget;
			
			var listElem = $('#'+templateListContainer.id);
			
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
					$('#'+domId).remove();
					templateListContainer.list[pos] = undefined;
					templateListContainer.count = pos;
				}
				else {
					$('#'+templateListContainer.wrapperId).remove();
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
		};
	};
	
	/**
	 * TODO
	 * 
	 * @param template termgeneration template
	 * @param id internal id number
	 */
	function TemTemplateWidget(template, id) {
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
				
				var layout = createLayoutTableOpenTag()+'<thead><tr><td>Required</td>';
				
				// write top level requirements
				var first = true;
				for (i = 1; i < templateFields.length; i+=1) {
					field = templateFields[i];
					if (first && field.required === false) {
						first = false;
						layout += '<td>Optional</td>';
					}
					else {
						layout += '<td></td>';
					}
				}
				layout += '</tr><tr>';
				
				// write field names
				for (i = 0; i < templateFields.length; i+=1) {
					field = templateFields[i];
					layout += '<td>'+field.name+'</td>';
				}
				
				// write empty body and footer
				layout += '</tr></thead><tbody></tbody></table>';
				
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
				
				var trElement = $('<tr id="'+domId+'"></tr>');
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
								inputFields[i] = AutoCompleteOntologyInputPrefix(tdElement, field.ontologies, prefixes);
							}
							else {
								inputFields[i] = AutoCompleteOntologyInput(tdElement, field.ontologies);	
							}
						}
						else {
							inputFields[i] = AutoCompleteOntologyInputList(tdElement, field.ontologies, cardinality.min, cardinality.max);
						}
					}
					else {
						var cardinality = field.cardinality;
						if (cardinality.min === 1 && cardinality.max === 1) {
							inputFields[i] = TextFieldInput(tdElement);
						}
						else {
							inputFields[i] = TextFieldInputList(tdElement, cardinality.min, cardinality.max);
						}
					}
				}
			},
			
			/**
			 * 
			 */
			getTermGenerationInput : function() {
				
			}
		};
	}
	
	
	function TextFieldInput(elem) {
		var inputElement = $('<input type="text"/>'); 
		elem.append(inputElement);
		
		return {
			extractParameter : function(parameter, field, pos) {
				if (!pos) {
					pos = 0;
				}
				var text = elem.val();
				if (text !== null && text.length > 0) {
					var list = parameter.strings.values[field.name];
					if (!list) {
						list = [];
						parameter.strings.values[field.name] = list;
					}
					list[pos] = text;
					return true;
				}
				var success = (field.required === false);
				return success;
			}
		};
	}
	
	function TextFieldInputList(container, min, max) {
		
		var count = 0;
		var list = [];
		var listParent = createLayoutTable();
		listParent.appendTo(container);
		for ( var i = 0; i < min; i++) {
			appendInput(count);
		}
		createAddRemoveWidget(container, appendInput, removeInput);
		
		function appendInput() {
			if (count <  max) {
				var listElem = $('<tr><td></td></tr>');
				listElem.appendTo(listParent);
				list[count] = TextFieldInput(listElem.children().first());
				count += 1;
			}
		}
		
		function removeInput() {
			if (count > min) {
				count -= 1;
				listParent.find('tr').last().remove();
				list[count] = undefined;
			}
		}
		
		return {
			extractParameter : function(parameter, field, pos) {
				var success = true;
				for ( var i = 0; i < count; i++) {
					var inputElem = list[i];
					if (inputElem) {
						var csuccess = input.extractParameter(parameter, field, i);
						success = success && csuccess;
					}
				}
				return success;
			}
		};
	}
	
	var jsonSyncService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: false,
	    methods: ['generate.availableTermTemplates', 
	              'generate.generateTerms', 
	              'ontology.availableOntologies', 
	              'ontology.autocomplete', 
	              'user.isValidUser']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonSyncService, false);
	
	function AutoCompleteOntologyInput(elem, ontologies) {
		
		var term = undefined;
		var inputElement = $('<input/>');
		elem.append(inputElement);
		var descriptionDiv = null;
		
		function updateDescriptionDiv(ofElement) {
			var w = ofElement.outerWidth();
			var h = ofElement.outerHeight();
			if (descriptionDiv === null) {
				descriptionDiv = $('<div><div class="term-description-content"></div></div>')
					.addClass( 'ui-widget-content ui-autocomplete ui-corner-all' )
					.css({
						'width': w,
						'height': h,
						'padding': '5px',
						'overflow': 'auto' 
					})
					.appendTo('body');
				descriptionDiv.resizable({
					minHeight: h,
					minWidth: w
				});
				descriptionDiv.draggable();
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
			layout += '<tr><td>Ontology</td><td>'+item.identifier.ontology+'</td></tr>';
			layout += '<tr><td>Label</td><td>'+item.label+'</td></tr>';
			layout += '<tr><td>Identfier</td><td>'+item.identifier.termId+'</td></tr>';
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
		
		inputElement.autocomplete({
			minLength: 3,
			source: function( request, response ) {
				removeDescriptionDiv();
				var term = request.term;
				try {
					var data = jsonSyncService.ontology.autocomplete(term, ontologies, 5);
					if (data !== null || data.length > 0) {
						response(data);	
					}
				} catch (e) {
				    alert("Unable to compute because: " + e);
				}
			},
			select : function(event, ui) {
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
			return $( '<li class="termgenie-autocomplete-menu-item"></li>' )
				.data( 'item.autocomplete', item )
				.append( '<a><span class="termgenie-autocomplete-menu-item-label">' + 
						item.label + '</span></a>' )
				.appendTo( ul );
		};

		
		
		return {
			extractParameter : function(parameter, field, pos) {
				if (!pos) {
					pos = 0;
				}
				if (term) {
					var text = inputElement.val();
					if (term.label == text) {
						var identifier = term.identifier;
						var list = parameter.terms.values[field.name];
						if (!list) {
							list = [];
							parameter.terms.values[field.name] = list;
						}
						list[pos] = identifier;
						return true;
					}
				}
				return false;
			}
		};
	}
	
	function AutoCompleteOntologyInputList(container, ontologies, min, max) {
		
		var count = 0;
		var list = [];
		var listParent = createLayoutTable();
		listParent.appendTo(container);
		for ( var i = 0; i < min; i++) {
			appendInput(count);
		}
		createAddRemoveWidget(container, appendInput, removeInput);
		
		function appendInput() {
			if (count <  max) {
				var listElem = $('<tr><td></td></tr>');
				listElem.appendTo(listParent);
				list[count] = AutoCompleteOntologyInput(listElem.children().first(), ontologies);
				count += 1;
			}
		}
		
		function removeInput() {
			if (count > min) {
				count -= 1;
				listParent.find('tr').last().remove();
				list[count] = undefined;
			}
		}
		
		return {
			extractParameter : function(parameter, field, pos) {
				var success = true;
				for ( var i = 0; i < count; i++) {
					var inputElem = list[i];
					if (inputElem) {
						var csuccess = input.extractParameter(parameter, field, i);
						success = success && csuccess;
					}
				}
				return success;
			}
		};
	}
	
	function AutoCompleteOntologyInputPrefix (elem, ontologies, prefixes) {
		var checkbox, i, j;
		
		var container = createLayoutTable();
		container.appendTo(elem);
		var inputContainer = $('<tr><td></td></tr>');
		inputContainer.appendTo(container);
		
		var inputField = AutoCompleteOntologyInput(inputContainer, ontologies);
		
		var checkboxes = [];
		for ( i = 0; i < prefixes.length; i++) {
			checkbox = $('<input type="checkbox" checked="true"/>');
			checkboxes[i] = checkbox;
			inputContainer = $('<tr><td></td></tr>');
			inputContainer.append(checkbox);
			inputContainer.append('<span class="term-prefix-label"> '+prefixes[i]+' </span>');
			inputContainer.appendTo(container);
		}
		
		return {
			extractParameter : function(parameter, field, pos) {
				if (!pos) {
					pos = 0;
				}
				var success = inputField.extractParameter(parameter, field, pos);
				
				var count = 0;
				var cPrefixes = [];
				
				for ( j = 0; j < checkboxes.length; j++) {
					checkbox = checkboxes[j];
					if(checkbox.is(':checked')) {
						cPrefixes[count] = prefixes[j];
						count += 1;
					}
				}
				
				var list = parameter.prefixes.values[field.name];
				if (!list) {
					list = [];
					parameter.prefixes.values[field.name] = list;
				}
				list[pos] = cPrefixes;
			}
		};
	}
	
	/**
	 * Create the dynamic part of the user panel
	 */
	function createUserPanel() {
		/* 
		 * add functionality to the commit checkbox:
		 * only enable username and password fields, 
		 * if the checkbox is enabled.
		 */ 
		var checkBoxElem = $('#checkbox-try-commit');
		checkBoxElem.click(function(){
			if (checkBoxElem.is(':checked')) {
				$('#input-user-name').removeAttr("disabled");
				$('#input-user-password').removeAttr("disabled");
			} else {
				$('#input-user-name').attr("disabled", true);
				$('#input-user-password').attr("disabled", true);
			}
		});
		
		/*
		 * add functionality to the submit button:
		 * only try to commit, if the check box is enabled,
		 * otherwise prepare for export.
		 */
		$('#button-submit-for-commit-or-export').click(function(){
			// select mode
			if (checkBoxElem.is(':checked')) {
				// try to commit
				// TODO fill with method call
				alert('Commit');
			} else {
				//  just generate the info for the export a obo/owl
				// TODO fill with method call
				alert('Prepare for export');
			}
			myAccordion.enablePane(3);
			myAccordion.activatePane(3);
		});
	}

	// HTML wrapper functions
	
	function createAddRemoveWidget(parent, addfunction, removeFunction) {
		var addButton = $('<a href="#">More</a>'); 
		var delButton = $('<a href="#">Less</a>');
		var buttons = $('<span class="more-less-buttons"></span>');
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
	
	function createLayoutTable() {
		return $(createLayoutTableOpenTag()+'</table>');
	}
	
	function createLayoutTableOpenTag() {
		return '<table class="termgenie-layout-table" cellSpacing="0" cellPadding="0">';
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

});