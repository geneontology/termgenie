package org.bbop.termgenie.client.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.client.LoggingCallback;
import org.bbop.termgenie.services.OntologyServiceAsync;
import org.bbop.termgenie.services.TermSuggestion;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Tools for handling auto complete functionality.
 */
public class AutoCompleteHelper {
	
	private final static Map<String, SuggestOracle> oracles = new HashMap<String, SuggestOracle>();
	
	/**
	 * Retrieve or create (if it not exists) an oracle for the given ontology.
	 * 
	 * @param ontology
	 * @return oracle
	 */
	public static SuggestOracle getSuggestOracle(final String ontology) {
		synchronized (oracles) {
			SuggestOracle suggestOracle = oracles.get(ontology);
			if (suggestOracle == null) {
				suggestOracle = new SuggestOracle() {

					@Override
					public void requestSuggestions(final Request request, final Callback callback) {
						String query = request.getQuery();
						AsyncCallback<List<TermSuggestion>> t = new LoggingCallback<List<TermSuggestion>>() {
							
							@Override
							public void onSuccess(List<TermSuggestion> result) {
								callback.onSuggestionsReady(request, new Response(result));
							}
						};
						OntologyServiceAsync.Util.getInstance().autocompleteQuery(query, ontology, t);
						
					}
				};
				oracles.put(ontology, suggestOracle);
			}
			return suggestOracle;
		}
	}
}
