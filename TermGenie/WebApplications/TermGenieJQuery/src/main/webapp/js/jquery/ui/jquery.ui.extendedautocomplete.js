/*
 * jQuery plugin for an auto-complete field with an additional description.
 * This is auto-complete is designed for complex data types, with extra 
 * methods for rendering the suggested items.
 * 
 * Required functions:
 * 
 *   source:  function( request, response ) //  expected to use RPC
 *   createInfoDivContent:  function( item ) // create the info for an item
 *   
 * Optional functions:
 * 
 *   createInfoDiv:  function()    // overwrite to create custom div for wrapping the content.
 *   getLabel:  function( item )   // overwrite to extract a custom the label
 *   onSelect: function()          // overwrite to do work during an select
 *   renderItem: function( item )  // overwrite to render a custom item
 * 
 * Depends:
 *	jquery.ui.autocomplete.js
 *	jquery.ui.resizable.js
 */
(function( $, undefined ) {

	jQuery.widget( "ui.extendedautocomplete", {
		
		options: {
			appendTo: "body",
			autoFocus: false,
			delay: 300,
			minLength: 1,
			position: {
				my: "left top",
				at: "left bottom",
				collision: "none"
			},
			source: null,
			createInfoDiv: function() {
				return '<div></div>';
			},
			createInfoDivContent: null,
			getLabel: function( item ) {
				return item;
			}, 
			renderItem: function( item ) {
				return item;
			},
			onSelect: null
		},
		
		_create: function() {
			var self = this;
			self._selectedItem = undefined;
			
			var descriptionDiv = null;
			
			/**
			 * update the description div parameters, 
			 * to fit with the auto-complete box. 
			 */
			this._updateDescriptionDiv = function(ofElement) {
				var w = ofElement.outerWidth();
				if (w < 400) {
					w = 400;
				}
				var h = ofElement.outerHeight();
				if (h < 200) {
					h = 200;
				}
				if (descriptionDiv === null) {
					descriptionDiv = jQuery('<div>'+self.options.createInfoDiv()+'</div>')
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
				}
				else {
					descriptionDiv.resizable( "option", "minHeight", h );
					descriptionDiv.resizable( "option", "minWidth", w );
				}
				descriptionDiv.position({
					my: 'left top',
					at: 'right top',
					of: self.element.autocomplete('widget'),
					collision: 'none none'
				});
			}
			
			/**
			 * Remove the description and its div.
			 */
			this._removeDescriptionDiv = function() {
				if (descriptionDiv !== null) {
					descriptionDiv.removeClass('ui-autocomplete-input');
					descriptionDiv.remove();
					descriptionDiv = null;
				}
			}
			
			/**
			 * Set the description for the current selected item.
			 * 
			 * @param item {JsonTermSuggestion} the current item
			 */
			this._setContentDescriptionDiv = function(item) {
				var content = descriptionDiv.children().first();
				content.empty();
				var layout = self.options.createInfoDivContent(item);
				content.append(layout);
			}

			// setup the auto-completion widget, includes an
			// additional description div for terms
			
			self.element.autocomplete({
				autoFocus: self.options.autoFocus,
				delay: self.options.delay,
				minLength: self.options.minLength,
				position: self.options.position,
				source: function( request, response ) {
					// clean up: remove old description div
					self._removeDescriptionDiv();
					self.options.source(request, response);
				},
				// overwrite method to remove the additional description div
				select : function(event, ui) {
					if (self.options.onSelect && self.options.onSelect !== null) {
						self.options.onSelect();
					}
					self.element.val(self.options.getLabel(ui.item));
					self._selectedItem = ui.item;
					self._removeDescriptionDiv();
					return false;
				},
				// over write method to update the description div
				focus : function(event, ui) {
					self.element.val(self.options.getLabel(ui.item));
					self._updateDescriptionDiv(self.element.autocomplete('widget'));
					self._setContentDescriptionDiv(ui.item);
					return false;
				},
				// overwrite method to remove the additional description div
				close : function(event, ui) {
					self._removeDescriptionDiv();
				} 
			})
			// overwrite rendering method for data items
			.data( 'autocomplete' )._renderItem = self.options.renderItem;
		},
		destroy: function() {
			this._removeDescriptionDiv();
			jQuery.Widget.prototype.destroy.call( this );
		},
		getSelected : function() {
			return this._selectedItem;
		}
	});
})( jQuery );
