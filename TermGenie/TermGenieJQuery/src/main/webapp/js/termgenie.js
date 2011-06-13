$(document).ready(function() {
	// create proxy for json rpc
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
		// create selector for given ontologies
		var ontselect = c_span('select-ontology-header','Select Ontology') +
		'<select id="select-ontology-select">';
		$.each(ontologies, function(intIndex, objValue){
			ontselect += '<option value="'+objValue+'">'+objValue+'</option>' 
		});
		ontselect += '</select>'+ c_button('select-ontology-button', 'Submit');
		
		// add to div
		$('#div-select-ontology').append(ontselect);
		
		// register click handler
		$('#select-ontology-button').click(function() {
			var selectedValue = $('#select-ontology-select').val();
			createTemplateSelector(selectedValue);
		});
	}
	
	function createTemplateSelector(ontology) {
		var termselect = c_div('div-template-selector', 
				c_span('select-template-header','Select Template'))+
				c_div('div-all-template-parameters','');
		
		$('#div-select-templates-and-parameters').append(termselect);
		
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
			var option = $('<option />')
			option.text(templateName);
			option.val(intIndex);
			domElement.append(option);
		});
		
		$('#button-add-template-select').click(function (){
			var intIndex = $('#select-add-template-select').val();
			addTemplate(templates[intIndex]);
		});
	}
	
	function addTemplate(template) {
		alert('Add template: '+template.name);
	}
	
	function removeTemplate(template) {
		
	}
	
	function createUserPanel() {
		var userPanel =
			'<table><tr><td>'+
			c_button('button-submit-for-term-generation', 'Submit')+
			'</td><td>'+
			'<input type="checkbox" id="checkbox-try-commit" /> commit'+
			'</td></tr><tr><td>'+
			'Username:</td><td><input type="text" id="input-user-name" disabled=true/></td></tr><tr><td>'+
			'Password:</td><td><input type="password" id="input-user-password" disabled=true/>'+
			'</td></tr></table>';
		
		$('#div-submit-and-credentials').append(userPanel);
		$('#checkbox-try-commit').click(function(){
			if ($('#checkbox-try-commit').is(':checked')) {
				$('#input-user-name').removeAttr("disabled");
				$('#input-user-password').removeAttr("disabled");
			} else {
				$('#input-user-name').attr("disabled", true);
				$('#input-user-password').attr("disabled", true);
			}
		});
	}

	// HTML wrapper functions
	
	function c_div(id, content) {
		return '<div id="'+id+'">'+content+'</div>';
	}
	
	function c_span(css, content) {
		return '<span class="'+css+'">'+content+'</span>'
	}
	
	function c_button(id, text) {
		return '<button type="button" id="'+id+'">'+text+'</button>';
	}
	
});