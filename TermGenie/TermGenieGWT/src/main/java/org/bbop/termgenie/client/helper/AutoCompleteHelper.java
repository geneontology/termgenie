package org.bbop.termgenie.client.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.client.LoggingCallback;
import org.bbop.termgenie.services.OntologyServiceAsync;
import org.bbop.termgenie.services.TermSuggestion;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.GenericSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;

/**
 * Tools for handling auto complete functionality.
 */
public class AutoCompleteHelper {
	
	private static final int MAX_SUGGESTIONS = 10;
	
	private final static Map<String, GenericSuggestOracle<TermSuggestion>> oracles = new HashMap<String, GenericSuggestOracle<TermSuggestion>>();
	
	/**
	 * Retrieve or create (if it not exists) an oracle for the given ontology.
	 * 
	 * @param strings
	 * @return oracle
	 */
	public static GenericSuggestOracle<TermSuggestion> getSuggestOracle(final String[] strings) {
		synchronized (oracles) {
			String key = calculateKey(strings);
			GenericSuggestOracle<TermSuggestion> suggestOracle = oracles.get(key);
			if (suggestOracle == null) {
				suggestOracle = new GenericSuggestOracle<TermSuggestion>() {

					@Override
					public void requestSuggestions(final Request request, final GenericOracleCallback<TermSuggestion> callback) {
						String query = request.getQuery();
						AsyncCallback<List<TermSuggestion>> t = new LoggingCallback<List<TermSuggestion>>() {
							
							@Override
							public void onSuccess(List<TermSuggestion> result) {
								callback.onSuggestionsReady(request, new GenericResponse<TermSuggestion>(result));
							}
						};
						OntologyServiceAsync.Util.getInstance().autocompleteQuery(query, strings, MAX_SUGGESTIONS, t);
						
					}

					@Override
					public boolean isDisplayStringHTML() {
						return true;
					}
				};
				oracles.put(key, suggestOracle);
			}
			return suggestOracle;
		}
	}

	private static String calculateKey(String[] strings) {
		if (strings.length == 1) {
			return strings[0];
		}
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append("||");
			sb.append(string);
		}
		return sb.toString();
	}
}
