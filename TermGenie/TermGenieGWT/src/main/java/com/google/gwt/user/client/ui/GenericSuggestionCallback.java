package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * The callback used when a user selects a {@link Suggestion}.
 */
public interface GenericSuggestionCallback<T extends Suggestion> {
	
	void onSuggestionSelected(T suggestion);
	
}