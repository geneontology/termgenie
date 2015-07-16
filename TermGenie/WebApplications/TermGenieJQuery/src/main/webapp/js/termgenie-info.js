/**
 * Setup the TermGenie management page.
 *
 * @returns empty object
 */
function TermGenieInfo(){

	// main elements from the static html page
	var mainMessagePanel = jQuery('#MainMessagePanel');
	var mainContentPanel = jQuery('#MainContentPanel');

	//create proxy for json rpc
	var jsonService = new JsonRpc.ServiceProxy("jsonrpc", {
	    asynchronous: true,
	    methods: ['info.getInfoDetails']
	});
	// asynchronous
	JsonRpc.setAsynchronous(jsonService, true);

	// add link to error console
	jQuery('#termgenie-error-console-link').click(function(){
		jQuery.openLogPanel();
	});
	// startup
	onStartup();

	function onStartup() {
		mainMessagePanel.append(createBusyMessage('Loading information from server.'));
		jsonService.info.getInfoDetails({
			params: [],
			onSuccess: function(infos){
				mainMessagePanel.empty();
				if (infos !== undefined) {
					renderInfos(infos);
				}
				else {
					mainContentPanel.append("Server returned with a negative response. Please reload the page to try again.");
				}
			},
			onException: function(e) {
				mainMessagePanel.empty();
				jQuery.logSystemError('Could not retrieve infos form the server',e);
				return true;
			}
		});
	}

	function renderInfos(infos) {
		mainContentPanel.empty();
		var content = '<table>';
		content += '<tr><td class="termgenie-module-table-header">Name</td><td class="termgenie-module-table-header">Value</td></tr>';

		var names = [];
		jQuery.each(infos, function(name){
			names.push(name);
		});
		names.sort();

		jQuery.each(names, function(index, name){
			var value = infos[name];
			content += '<tr><td>'+name+'</td><td>'+value+'</td></tr>';
		});
		content += '</table>';

		mainContentPanel.append(content);
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

}
// actual call in jQuery to execute the TermGenie info scripts
// after the document is ready
jQuery(document).ready(function(){
	// start term genie.
	TermGenieInfo();
});
