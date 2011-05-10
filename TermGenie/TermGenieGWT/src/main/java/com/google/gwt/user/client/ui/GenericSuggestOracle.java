/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.client.ui;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link com.google.gwt.user.client.ui.GenericSuggestOracle} can be used to
 * create suggestions associated with a specific query string. It is currently
 * used by {@link SuggestBox}.
 * 
 * @see SuggestBox
 */
public abstract class GenericSuggestOracle<T extends Suggestion> {
	private GenericResponse<T> emptyResponse = new GenericResponse<T>(new ArrayList<T>());

	/**
	 * Callback for {@link com.google.gwt.user.client.ui.GenericSuggestOracle}.
	 * Every {@link Request} should be associated with a callback that should be
	 * called after a {@link GenericResponse} is generated.
	 */
	public interface GenericOracleCallback<T extends Suggestion> {
		/**
		 * Consume the suggestions created by a
		 * {@link com.google.gwt.user.client.ui.GenericSuggestOracle} in
		 * response to a {@link Request}.
		 * 
		 * @param request
		 *            the request
		 * @param response
		 *            the response
		 */
		void onSuggestionsReady(Request request, GenericResponse<T> response);
	}

	/**
	 * {@link com.google.gwt.user.client.ui.GenericSuggestOracle} response.
	 * 
	 * <p>
	 * Can optionally have truncation information provided. To indicate that
	 * there are more results but the number is not known, use:
	 * 
	 * <p>
	 * <code>response.setMoreSuggestions(true);</code>
	 * 
	 * <p>
	 * Or to indicate more results with an exact number, use:
	 * 
	 * <p>
	 * <code>response.setMoreSuggestionsCount(102);</code>
	 */
	public static class GenericResponse<T extends Suggestion> implements IsSerializable {
		private Collection<T> suggestions;

		/**
		 * The response is considered to have "more suggestions" when the number
		 * of matching suggestions exceeds {@link Request#getLimit}, so the
		 * response suggestion list is truncated.
		 */
		private boolean moreSuggestions = false;

		/**
		 * Number of truncated suggestions.
		 */
		private int numMoreSuggestions = 0;

		/**
		 * Constructor for {@link GenericResponse}.
		 */
		public GenericResponse() {
		}

		/**
		 * Constructor for {@link GenericResponse}.
		 * 
		 * @param suggestions
		 *            each element of suggestions must implement the
		 *            {@link Suggestion} interface
		 */
		public GenericResponse(Collection<T> suggestions) {
			setSuggestions(suggestions);
		}

		/**
		 * Gets how many more suggestions there are.
		 * 
		 * @return the count. if there no more suggestions or the number of more
		 *         suggestions is unknown, returns 0.
		 */
		public int getMoreSuggestionsCount() {
			return this.numMoreSuggestions;
		}

		/**
		 * Gets the collection of suggestions. Each suggestion must implement
		 * the {@link Suggestion} interface.
		 * 
		 * @return the collection of suggestions
		 */
		public Collection<T> getSuggestions() {
			return this.suggestions;
		}

		/**
		 * Gets whether or not the suggestion list was truncated due to the
		 * {@link Request#getLimit}.
		 */
		public boolean hasMoreSuggestions() {
			return this.moreSuggestions;
		}

		/**
		 * Sets whether or not the suggestion list was truncated due to the
		 * {@link Request#getLimit}.
		 */
		public void setMoreSuggestions(boolean moreSuggestions) {
			this.moreSuggestions = moreSuggestions;
		}

		/**
		 * Sets whether or not the suggestion list was truncated due to the
		 * {@link Request#getLimit}, by providing an exact count of remaining
		 * suggestions.
		 * 
		 * @param count
		 *            number of truncated suggestions. Pass 0 to indicate there
		 *            are no other suggestions, which is equivalent to
		 *            {@link #setMoreSuggestions(boolean)
		 *            setMoreSuggestions(false)}.
		 */
		public void setMoreSuggestionsCount(int count) {
			this.numMoreSuggestions = count;
			this.moreSuggestions = (count > 0);
		}

		/**
		 * Sets the suggestions for this response. Each suggestion must
		 * implement the {@link Suggestion} interface.
		 * 
		 * @param suggestions
		 *            the suggestions
		 */
		public void setSuggestions(Collection<T> suggestions) {
			this.suggestions = suggestions;
		}
	}

	/**
	 * Constructor for
	 * {@link com.google.gwt.user.client.ui.GenericSuggestOracle}.
	 */
	public GenericSuggestOracle() {
	}

	/**
	 * Should {@link Suggestion} display strings be treated as HTML? If true,
	 * this all suggestions' display strings will be interpreted as HTML,
	 * otherwise as text.
	 * 
	 * @return by default, returns false
	 */
	public boolean isDisplayStringHTML() {
		return false;
	}

	/**
	 * Generate a {@link GenericResponse} based on a default request. The
	 * request query must be null as it represents the results the oracle should
	 * return based on no query string.
	 * <p>
	 * After the {@link GenericResponse} is created, it is passed into
	 * {@link GenericOracleCallback#onSuggestionsReady(com.google.gwt.user.client.ui.GenericSuggestOracle.Request, com.google.gwt.user.client.ui.GenericSuggestOracle.GenericResponse)}
	 * .
	 * </p>
	 * 
	 * @param request
	 *            the request
	 * @param callback
	 *            the callback to use for the response
	 */
	public void requestDefaultSuggestions(Request request, GenericOracleCallback<T> callback) {
		callback.onSuggestionsReady(request, emptyResponse);
	}

	/**
	 * Generate a {@link GenericResponse} based on a specific {@link Request}.
	 * After the {@link GenericResponse} is created, it is passed into
	 * {@link GenericOracleCallback#onSuggestionsReady(com.google.gwt.user.client.ui.GenericSuggestOracle.Request, com.google.gwt.user.client.ui.GenericSuggestOracle.GenericResponse)}
	 * .
	 * 
	 * @param request
	 *            the request
	 * @param callback
	 *            the callback to use for the response
	 */
	public abstract void requestSuggestions(Request request, GenericOracleCallback<T> callback);
}
