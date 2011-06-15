$(function() {
	
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
			// public methods
			activatePane : function(pos) {
				$(id).accordion("activate", pos);
			},
			
			setPaneState : function(pos, state) {
				selections["Pane_" + pos] = state;
			},
		
			enablePane : function(pos) {
				selections["Pane_" + pos] = true;
			},
			
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
			createOntologySelector(result);
			createUserPanel();
		},
		onException: function(e) {
			alert("Unable to compute because: " + e);
			return true;
		}
	});
	
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
	
	function setStep1Header(ontology) {
		var elem = $('#span-step1-additional-header');
		elem.empty();
		elem.append('<span class="step1-additional-header">'+ontology+'</span>');
	}
	
	function setStep2Active(step1Available) {
		myAccordion.setPaneState(0, step1Available);
		myAccordion.enablePane(1);
		myAccordion.activatePane(1);
	}
	
	var temTemplateWidgetList = TemTemplateWidgetList();
	
	function createTemplateSelector(ontology) {
		var termselect = c_div('div-template-selector', 
				c_span('select-template-header','Select Template'))+
				c_div('div-all-template-parameters','');
		
		var elem = $('#div-select-templates-and-parameters');
		elem.empty();
		elem.append(termselect);
		
		var submitButton = $('#button-termgeneration-start');
		submitButton.click(function(){
			// TODO verification using Javascript
			// TODO submit to server
			alert("Verification and Generation");
			myAccordion.enablePane(2);
			myAccordion.activatePane(2);
		});
		
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
	
	function createTemplateSelectorMenu(ontology, templates) {
		$('#div-template-selector')
			.append('<select id="select-add-template-select"></select>'+
				c_button('button-add-template-select', 'Add template'));
		
		var domElement = $('#select-add-template-select');
		$.each(templates, function(intIndex, objValue) {
			var templateName = objValue.name;
			var option = $('<option />');
			option.text(templateName);
			option.val(intIndex);
			domElement.append(option);
		});
		
		$('#button-add-template-select').click(function (){
			var intIndex = $('#select-add-template-select').val();
			temTemplateWidgetList.addTemplate(templates[intIndex]);
		});
	}
	
	function TemTemplateWidgetList(){
		// private members
		var templateMap = {};
		
		// private methods
		function createTemplateSubList(template, id, wrapperId) {
			$('#div-all-template-parameters').append(c_div(wrapperId,c_div(id, '')));
			var wrapperElem = $('#'+wrapperId);
			wrapperElem.addClass('templatelistwrapper');
			wrapperElem.prepend('<div>Template: <span class="label-template-name">'+template.name+'</span></div>')
			var addButtonId = wrapperId+'-button-add';
			var removeButtonId = wrapperId+'-button-remove';
			var buttons = '<div>'+
			 c_button(addButtonId, 'Add line')+
			 c_button(removeButtonId, 'Remove line')+
			 '</div>';
			wrapperElem.append(buttons);
		}
		
		return {
			//public methods
			addTemplate : function (template) {
				var templateListContainer = templateMap[template.name];
				if (!templateListContainer) {
					templateListContainer = {
						count : 0,
						list : new Array(),
						id : 'div-all-template-parameters-'+template.name,
						wrapperId : 'div-all-template-parameters-wrapper'+template.name
					}
					createTemplateSubList(template, templateListContainer.id, templateListContainer.wrapperId);
					templateMap[template.name] = templateListContainer;
				}
				var templateWidget = TemTemplateWidget(template,templateListContainer.count);
				templateListContainer.list[templateListContainer.count] = templateWidget;
				
				var listElem = $('#'+templateListContainer.id);
				listElem.append(templateWidget.getLine());
				if (templateListContainer.count === 0) {
					listElem.append(templateWidget.getFooter());
					listElem.prepend(templateWidget.getHeader());
				}
				templateListContainer.count += 1;
			},
			
			removeTemplate : function(template) {
				var templateList = templateMap[template.name];
				if (templateList) {
					if (condition) {
						
					}
					else {
						
					}
				}
			},
			
			clear : function() {
				
			}
		};
	};
	
	function TemTemplateWidget(template, id) {
		
		return {
			getId : function() {
				return id;
			},
			getHeader : function() {
				return '<div>Header</div>';
			},
			getFooter : function() {
				return '<div>Footer</div>';;
			},
			getLine : function() {
				return '<div> id:'+id+' Name:'+template.name + ' Field count: '+template.fields.length+'</div>';
			},
			getTermGenerationInput : function() {
				
			}
		};
	}
	
	function createUserPanel() {
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
		$('#button-submit-for-commit-or-export').click(function(){
			// select mode
			if (checkBoxElem.is(':checked')) {
				// try to commit
				alert('Commit');
			} else {
				alert('Prepare for export');
			}
			myAccordion.enablePane(3);
			myAccordion.activatePane(3);
		});
	}

	// HTML wrapper functions
	
	function c_div(id, content) {
		return '<div id="'+id+'">'+content+'</div>';
	}
	
	function c_span(css, content) {
		return '<span class="'+css+'">'+content+'</span>';
	}
	
	function c_button(id, text) {
		return '<button type="button" id="'+id+'">'+text+'</button>';
	}

});