package com.google.gwt.user.client.ui;

import java.util.Collection;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * Used to display suggestions to the user.
 */
public abstract class GenericSuggestionDisplay<T extends Suggestion> {

	/**
     * Get the currently selected {@link Suggestion} in the display.
     * 
     * @return the current suggestion, or null if none selected
     */
    protected abstract T getCurrentSelection();

    /**
     * Hide the list of suggestions from view.
     */
    protected abstract void hideSuggestions();

    /**
     * Highlight the suggestion directly below the current selection in the
     * list.
     */
    protected abstract void moveSelectionDown();

    /**
     * Highlight the suggestion directly above the current selection in the
     * list.
     */
    protected abstract void moveSelectionUp();

    /**
     * Set the debug id of widgets used in the SuggestionDisplay.
     * 
     * @param suggestBoxBaseID the baseID of the {@link SuggestBox}
     * @see UIObject#onEnsureDebugId(String)
     */
    protected void onEnsureDebugId(String suggestBoxBaseID) {
    }

    /**
     * Accepts information about whether there were more suggestions matching
     * than were provided to {@link #showSuggestions}.
     * 
     * @param hasMoreSuggestions true if more matches were available
     * @param numMoreSuggestions number of more matches available. If the
     *     specific number is unknown, 0 will be passed.
     */
    protected void setMoreSuggestions(boolean hasMoreSuggestions, 
        int numMoreSuggestions) {
      // Subclasses may optionally implement.
    }

    /**
     * Update the list of visible suggestions.
     * 
     * Use care when using isDisplayStringHtml; it is an easy way to expose 
     * script-based security problems. 
     * 
     * @param suggestBox the suggest box where the suggestions originated
     * @param suggestions the suggestions to show
     * @param isDisplayStringHTML should the suggestions be displayed as HTML
     * @param isAutoSelectEnabled if true, the first item should be selected
     *          automatically
     * @param callback the callback used when the user makes a suggestion
     */
    protected abstract void showSuggestions(GenericSuggestBox<T> suggestBox,
        Collection<T> suggestions,
        boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
        GenericSuggestionCallback<T> callback);

}