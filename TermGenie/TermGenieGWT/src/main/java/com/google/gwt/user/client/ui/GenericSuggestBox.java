package com.google.gwt.user.client.ui;


import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HandlesAllKeyEvents;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.GenericSuggestOracle.GenericOracleCallback;
import com.google.gwt.user.client.ui.GenericSuggestOracle.GenericResponse;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class GenericSuggestBox<T extends Suggestion> extends Composite implements HasText,
		HasAnimation, HasAllKeyHandlers, HasValue<String>, HasSelectionHandlers<Suggestion> {

	private int limit = 20;
	private boolean selectsFirstItem = true;
	private GenericSuggestOracle<T> oracle;
	private String currentText;
	private T currentItem;
	private final GenericSuggestionDisplay<T> display;
	private final TextBoxBase box;
	private final GenericOracleCallback<T> callback = new GenericOracleCallback<T>() {
		public void onSuggestionsReady(Request request, GenericResponse<T> response) {
			display.setMoreSuggestions(response.hasMoreSuggestions(),
					response.getMoreSuggestionsCount());
			display.showSuggestions(GenericSuggestBox.this, response.getSuggestions(),
					oracle.isDisplayStringHTML(), isAutoSelectEnabled(), suggestionCallback);
		}
	};
	private final GenericSuggestionCallback<T> suggestionCallback = new GenericSuggestionCallback<T>() {
		public void onSuggestionSelected(T suggestion) {
			setNewSelection(suggestion);
		}
	};

	/**
	 * Constructor for {@link SuggestBox}. Creates a {@link TextBox} to use with
	 * this {@link SuggestBox}.
	 * 
	 * @param oracle
	 *            the oracle for this <code>SuggestBox</code>
	 */
	public GenericSuggestBox(GenericSuggestOracle<T> oracle) {
		this(oracle, new TextBox());
	}

	/**
	 * Constructor for {@link SuggestBox}. The text box will be removed from
	 * it's current location and wrapped by the {@link SuggestBox}.
	 * 
	 * @param oracle
	 *            supplies suggestions based upon the current contents of the
	 *            text widget
	 * @param box
	 *            the text widget
	 */
	public GenericSuggestBox(GenericSuggestOracle<T> oracle, TextBoxBase box) {
		this(oracle, box, new DefaultSuggestionDisplay<T>());
	}

	/**
	 * Constructor for {@link SuggestBox}. The text box will be removed from
	 * it's current location and wrapped by the {@link SuggestBox}.
	 * 
	 * @param oracle
	 *            supplies suggestions based upon the current contents of the
	 *            text widget
	 * @param box
	 *            the text widget
	 * @param suggestDisplay
	 *            the class used to display suggestions
	 */
	public GenericSuggestBox(GenericSuggestOracle<T> oracle, TextBoxBase box, GenericSuggestionDisplay<T> suggestDisplay) {
		this.box = box;
		this.display = suggestDisplay;
		initWidget(box);

		addEventsToTextBox();

		setOracle(oracle);
		setStyleName(STYLENAME_DEFAULT);
	}

	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return addDomHandler(handler, KeyDownEvent.getType());
	}

	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return addDomHandler(handler, KeyPressEvent.getType());
	}

	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return addDomHandler(handler, KeyUpEvent.getType());
	}

	public HandlerRegistration addSelectionHandler(SelectionHandler<Suggestion> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	/**
	 * Gets the limit for the number of suggestions that should be displayed for
	 * this box. It is up to the current {@link GenericSuggestOracle} to enforce this
	 * limit.
	 * 
	 * @return the limit for the number of suggestions
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Get the {@link GenericSuggestionDisplay} used to display suggestions.
	 * 
	 * @return the {@link GenericSuggestionDisplay}
	 */
	public GenericSuggestionDisplay<T> getSuggestionDisplay() {
		return display;
	}

	/**
	 * Gets the suggest box's
	 * {@link com.google.gwt.user.client.ui.GenericSuggestOracle}.
	 * 
	 * @return the {@link GenericSuggestOracle}
	 */
	public GenericSuggestOracle<T> getSuggestOracle() {
		return oracle;
	}

	public int getTabIndex() {
		return box.getTabIndex();
	}

	public String getText() {
		return box.getText();
	}

	/**
	 * Get the text box associated with this suggest box.
	 * 
	 * @return this suggest box's text box
	 */
	public TextBoxBase getTextBox() {
		return box;
	}

	public String getValue() {
		return box.getValue();
	}

	public void setFocus(boolean focused) {
		box.setFocus(focused);
	}

	/**
	 * Sets the limit to the number of suggestions the oracle should provide. It
	 * is up to the oracle to enforce this limit.
	 * 
	 * @param limit
	 *            the limit to the number of suggestions provided
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setTabIndex(int index) {
		box.setTabIndex(index);
	}

	public void setText(String text) {
		box.setText(text);
	}

	public void setValue(String newValue) {
		box.setValue(newValue);
	}

	public void setValue(String value, boolean fireEvents) {
		box.setValue(value, fireEvents);
	}

	/**
	 * Show the current list of suggestions.
	 */
	public void showSuggestionList() {
		if (isAttached()) {
			currentText = null;
			currentItem = null;
			refreshSuggestions();
		}
	}

	@Override
	protected void onEnsureDebugId(String baseID) {
		super.onEnsureDebugId(baseID);
		display.onEnsureDebugId(baseID);
	}

	void showSuggestions(String query) {
		if (query.length() == 0) {
			oracle.requestDefaultSuggestions(new Request(null, limit), callback);
		} else {
			oracle.requestSuggestions(new Request(query, limit), callback);
		}
	}

	private void addEventsToTextBox() {
		class TextBoxEvents extends HandlesAllKeyEvents implements ValueChangeHandler<String> {

			public void onKeyDown(KeyDownEvent event) {
				switch (event.getNativeKeyCode()) {
				case KeyCodes.KEY_DOWN:
					display.moveSelectionDown();
					break;
				case KeyCodes.KEY_UP:
					display.moveSelectionUp();
					break;
				case KeyCodes.KEY_ENTER:
				case KeyCodes.KEY_TAB:
					T suggestion = display.getCurrentSelection();
					if (suggestion == null) {
						display.hideSuggestions();
					} else {
						setNewSelection(suggestion);
					}
					break;
				}
				delegateEvent(GenericSuggestBox.this, event);
			}

			public void onKeyPress(KeyPressEvent event) {
				delegateEvent(GenericSuggestBox.this, event);
			}

			public void onKeyUp(KeyUpEvent event) {
				// After every user key input, refresh the popup's suggestions.
				refreshSuggestions();
				delegateEvent(GenericSuggestBox.this, event);
			}

			public void onValueChange(ValueChangeEvent<String> event) {
				delegateEvent(GenericSuggestBox.this, event);
			}
		}

		TextBoxEvents events = new TextBoxEvents();
		events.addKeyHandlersTo(box);
		box.addValueChangeHandler(events);
	}

	private void fireSuggestionEvent(Suggestion selectedSuggestion) {
		SelectionEvent.fire(this, selectedSuggestion);
	}

	private void refreshSuggestions() {
		// Get the raw text.
		String text = getText();
		if (text.equals(currentText)) {
			return;
		} else {
			currentText = text;
		}
		showSuggestions(text);
	}

	/**
	 * Set the new suggestion in the text box.
	 * 
	 * @param curSuggestion
	 *            the new suggestion
	 */
	private void setNewSelection(T curSuggestion) {
		assert curSuggestion != null : "suggestion cannot be null";
		currentText = curSuggestion.getReplacementString();
		currentItem = curSuggestion;
		setText(currentText);
		display.hideSuggestions();
		fireSuggestionEvent(curSuggestion);
	}
	
	/**
	 * Sets the suggestion oracle used to create suggestions.
	 * 
	 * @param oracle
	 *            the oracle
	 */
	private void setOracle(GenericSuggestOracle<T> oracle) {
		this.oracle = oracle;
	}

	/**
	 * Returns whether or not the first suggestion will be automatically
	 * selected. This behavior is on by default.
	 * 
	 * @return true if the first suggestion will be automatically selected
	 */
	public boolean isAutoSelectEnabled() {
		return selectsFirstItem;
	}

	private static final String STYLENAME_DEFAULT = "gwt-SuggestBox";

	/**
	 * Creates a {@link SuggestBox} widget that wraps an existing &lt;input
	 * type='text'&gt; element.
	 * 
	 * This element must already be attached to the document. If the element is
	 * removed from the document, you must call
	 * {@link RootPanel#detachNow(Widget)}.
	 * 
	 * @param oracle
	 *            the suggest box oracle to use
	 * @param element
	 *            the element to be wrapped
	 */
	public static <T extends Suggestion> GenericSuggestBox<T> wrap(GenericSuggestOracle<T> oracle, Element element) {
		// Assert that the element is attached.
		assert Document.get().getBody().isOrHasChild(element);

		TextBox textBox = new TextBox(element);
		GenericSuggestBox<T> suggestBox = new GenericSuggestBox<T>(oracle, textBox);

		// Mark it attached and remember it for cleanup.
		suggestBox.onAttach();
		RootPanel.detachOnWindowClose(suggestBox);

		return suggestBox;
	}

	/**
	 * Check whether or not the {@link DefaultSuggestionDisplay} has animations
	 * enabled. Note that this method only has a meaningful return value when
	 * the {@link DefaultSuggestionDisplay} is used.
	 * 
	 * @deprecated use {@link DefaultSuggestionDisplay#isAnimationEnabled()}
	 *             instead
	 */
	@Deprecated
	public boolean isAnimationEnabled() {
		return true;
	}

	/**
	 * Enable or disable animations in the {@link DefaultSuggestionDisplay}.
	 * Note that this method is a no-op unless the
	 * {@link DefaultSuggestionDisplay} is used.
	 * 
	 * @deprecated use
	 *             {@link DefaultSuggestionDisplay#setAnimationEnabled(boolean)}
	 *             instead
	 */
	@Deprecated
	public void setAnimationEnabled(boolean enable) {
		// Do nothing
	}
	
	public T getCurrentItem() {
		return currentItem;
	}
}
