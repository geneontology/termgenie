package com.google.gwt.user.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.PopupPanel.AnimationType;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

public class CellListSuggestionDisplay<T extends Suggestion> extends GenericSuggestionDisplay<T> {

	private T selected = null;
	private final CellList<T> suggestionMenu;
	private final PopupPanel suggestionPopup;

	/**
	 * We need to keep track of the last {@link SuggestBox} because it acts as
	 * an autoHide partner for the {@link PopupPanel}. If we use the same
	 * display for multiple {@link SuggestBox}, we need to switch the autoHide
	 * partner.
	 */
	private GenericSuggestBox<T> lastSuggestBox = null;

	/**
	 * Object to position the suggestion display next to, instead of the
	 * associated suggest box.
	 */
	private UIObject positionRelativeTo;

	/**
	 * Construct a new {@link DefaultSuggestionDisplay}.
	 */
	public CellListSuggestionDisplay() {
		// Create a cell to render each value.
		suggestionMenu = new CellList<T>(new AbstractCell<T>() {

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, T value,
					SafeHtmlBuilder sb) {
				// TODO Auto-generated method stub

			}
		});
//		suggestionMenu.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

		// Add a selection model to handle user selection.
		final SingleSelectionModel<T> selectionModel = new SingleSelectionModel<T>();
		suggestionMenu.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				selected = selectionModel.getSelectedObject();
			}
		});

		suggestionPopup = createPopup();
		suggestionPopup.setWidget(decorateSuggestionList(suggestionMenu));
	}

	/**
	 * Create the PopupPanel that will hold the list of suggestions.
	 * 
	 * @return the popup panel
	 */
	protected PopupPanel createPopup() {
		PopupPanel p = new DecoratedPopupPanel(true, false, "suggestPopup");
		p.setStyleName("gwt-SuggestBoxPopup");
		p.setPreviewingAllNativeEvents(true);
		p.setAnimationType(AnimationType.ROLL_DOWN);
		return p;
	}

	/**
	 * Wrap the list of suggestions before adding it to the popup. You can
	 * override this method if you want to wrap the suggestion list in a
	 * decorator.
	 * 
	 * @param suggestionList
	 *            the widget that contains the list of suggestions
	 * @return the suggestList, optionally inside of a wrapper
	 */
	protected Widget decorateSuggestionList(Widget suggestionList) {
		return suggestionList;
	}

	@Override
	public void hideSuggestions() {
		suggestionPopup.hide();
	}

	/**
	 * Check whether or not the list of suggestions is being shown.
	 * 
	 * @return true if the suggestions are visible, false if not
	 */
	public boolean isSuggestionListShowing() {
		return suggestionPopup.isShowing();
	}

	public boolean isAnimationEnabled() {
		return suggestionPopup.isAnimationEnabled();
	}

	/**
	 * Sets the UI object where the suggestion display should appear next to.
	 * 
	 * @param uiObject
	 *            the uiObject used for positioning, or null to position
	 *            relative to the suggest box
	 */
	public void setPositionRelativeTo(UIObject uiObject) {
		positionRelativeTo = uiObject;
	}

	@Override
	protected T getCurrentSelection() {
		synchronized (suggestionMenu) {
			if (!isSuggestionListShowing()) {
				return null;
			}
			return selected;
		}
	}

	@Override
	protected void moveSelectionDown() {
		synchronized (suggestionMenu) {
			List<T> items = suggestionMenu.getVisibleItems();
			SelectionModel<? super T> selectionModel = suggestionMenu.getSelectionModel();
			
		}
	}

	@Override
	protected void moveSelectionUp() {
		// TODO Auto-generated method stub
		synchronized (suggestionMenu) {
			List<T> items = suggestionMenu.getVisibleItems();
			SelectionModel<? super T> selectionModel = suggestionMenu.getSelectionModel();
		}
	}

	@Override
	protected void showSuggestions(GenericSuggestBox<T> suggestBox, Collection<T> suggestions,
			boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
			GenericSuggestionCallback<T> callback) {
		// Hide the popup if there are no suggestions to display.
		boolean anySuggestions = (suggestions != null && suggestions.size() > 0);
		if (!anySuggestions) {
			hideSuggestions();
			return;
		}

		// Hide the popup before we manipulate the menu within it. If we do
		// not
		// do this, some browsers will redraw the popup as items are removed
		// and added to the menu.
		if (suggestionPopup.isAttached()) {
			suggestionPopup.hide();
		}
		
		synchronized (suggestionMenu) {
			ArrayList<T> suggestionList = new ArrayList<T>(suggestions);
			suggestionMenu.setRowData(suggestionList);
			if (isAutoSelectEnabled && anySuggestions) {
				T firstItem = suggestionList.get(0);
				suggestionMenu.getSelectionModel().setSelected(firstItem, true);
				selected = firstItem;
			}
		}
		// Link the popup autoHide to the TextBox.
		if (lastSuggestBox != suggestBox) {
			// If the suggest box has changed, free the old one first.
			if (lastSuggestBox != null) {
				suggestionPopup.removeAutoHidePartner(lastSuggestBox.getElement());
			}
			lastSuggestBox = suggestBox;
			suggestionPopup.addAutoHidePartner(suggestBox.getElement());
		}

		// Show the popup under the TextBox.
		suggestionPopup.showRelativeTo(positionRelativeTo != null ? positionRelativeTo
				: suggestBox);
	}

}
