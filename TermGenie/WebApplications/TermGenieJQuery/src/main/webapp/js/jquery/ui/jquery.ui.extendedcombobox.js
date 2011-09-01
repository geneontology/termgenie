/*
 * Register an alternative implementation of a combobox 
 * as jQuery plugin. This one has also an additional description field.
 * 
 * see http://jqueryui.com/demos/autocomplete/ 
 * (Combobox example) for details and original source.
 * 
 * Depends:
 *	jquery.ui.extendedautocomplete.js
 *  jquery.ui.button.js
 */
(function( $, undefined ) {
	jQuery.widget( "ui.extendedcombobox", {
		options: {
			createInfoDivContent: null,
			createInfoDiv: function() {
				return '<div></div>';
			},
			minWidth: 400,
			minHeight: 200
		},
		
		_create: function() {
			var self = this;

			var boxWidth = this.element.width();
			var select = this.element.hide();
			var selected = select.children( ":selected" );
			var value = selected.val() ? selected.text() : "";
			var input = this.input = jQuery( "<input>" )
			.insertAfter( select )
			.val( value )
			.extendedautocomplete({
				createInfoDiv: self.options.createInfoDiv,
				createInfoDivContent: self.options.createInfoDivContent,
				minWidth: self.options.minWidth,
				minHeight: self.options.minHeight,
				delay: 0,
				minLength: 0,
				source: function( request, response ) {
					var matcher = new RegExp( jQuery.ui.autocomplete.escapeRegex(request.term), "i" );
					response( select.children( "option" ).map(function() {
						var text = jQuery( this ).text();
						if ( this.value && ( !request.term || matcher.test(text) ) )
							return {
								label: text,
								value: this.value,
								option: this
							};
					}) );
				},
				getLabel: function(item) {
					return item.label;
				},
				renderItem: function( ul, item ) {
					return jQuery( "<li></li>" )
						.data( "item.autocomplete", item )
						.append( "<a>" + item.label + "</a>" )
						.appendTo( ul );
				}
			})
			.addClass( "ui-widget ui-widget-content ui-corner-left termgenie-select-template-input-field" )
			.css("width", boxWidth+20);

			this.button = jQuery( "<button type='button'>&nbsp;</button>" )
			.attr( "tabIndex", -1 )
			.attr( "title", "Show All Items" )
			.insertAfter( input )
			.button({
				icons: {
					primary: "ui-icon-triangle-1-s"
				},
				text: false
			})
			.removeClass( "ui-corner-all" )
			.addClass( "ui-corner-right ui-button-icon termgenie-select-template-input-dropdown-button" )
			.css("width", "27px")
			.click(function() {
				// close if already visible
				if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
					input.autocomplete( "close" );
					return;
				}

				// work around a bug (likely same cause as #5265)
				jQuery( this ).blur();

				// pass empty string as value to search for, displaying all results
				input.autocomplete( "search", "" );
				input.focus();
			});
		},

		destroy: function() {
			this.input.remove();
			this.button.remove();
			this.element.show();
			jQuery.Widget.prototype.destroy.call( this );
		}
	});
})( jQuery );