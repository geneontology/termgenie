package org.bbop.termgenie.index;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.bbop.termgenie.index.LuceneMemoryOntologyIndex.SearchResult;

/**
 * Tools for auto-completion.
 */
public class AutoCompletionTools {

	/**
	 * Split the string into tokens using white spaces. Ignore multiple white
	 * spaces (begin, end, and in-between)
	 * 
	 * @param s
	 * @return list of non-white space sub strings
	 */
	public static List<String> split(String s) {
		if (s.isEmpty()) {
			return Collections.emptyList();
		}
		String[] split = StringUtils.split(s);
		return Arrays.asList(split);
	}

	/**
	 * Pre-process the query String, return null, if no valid tokens are
	 * identified
	 * 
	 * @param queryString
	 * @param idField name of the ID Field
	 * @return string or null
	 */
	public static String preprocessQuery(String queryString, String idField) {
		StringBuilder subquery1 = new StringBuilder();
		StringBuilder subquery2 = new StringBuilder();
		StringBuilder subquery3 = new StringBuilder();
		List<String> list = AutoCompletionTools.split(queryString);
		if (list.isEmpty()) {
			return null;
		}
		int charCount = 0;
		for (String string : list) {
			charCount += string.length();
			string = QueryParser.escape(string);
			if (subquery1.length() == 0) {
				subquery1.append('(');
				subquery2.append("(\"");
				if (idField != null) {
					subquery3.append("(");
					subquery3.append(idField);
					subquery3.append(":\"");
				}
			}
			else {
				subquery1.append(" AND ");
				subquery2.append(' ');
				if (idField != null) {
					subquery3.append(' ');
				}
			}
			subquery1.append(string);
			subquery1.append('*');
			subquery2.append(string);
			if (idField != null) {
				subquery3.append(string);
			}
		}
		subquery1.append(')');
		subquery2.append("\"^2)");
		subquery3.append("\")");

		if (charCount < 2) {
			// at least two non-whitespace characters are required
			return null;
		}

		StringBuilder sb = new StringBuilder(subquery1);
		sb.append(" OR ");
		sb.append(subquery2);
		if (idField != null) {
			sb.append(" OR ");
			sb.append(subquery3);
		}
		return sb.toString();
	}

	public static void sortbyLabelLength(List<SearchResult> documents) {
		
		Collections.sort(documents, new Comparator<SearchResult>() {

			@Override
			public int compare(SearchResult o1, SearchResult o2) {
				final int l1 = o1.length;
				final int l2 = o2.length;
				return (l1 < l2 ? -1 : (l1 == l2 ? 0 : 1));
			}
		});
	}

	public static boolean fEquals(float f1, float f2) {
		return Math.abs(f1 - f2) < 0.0001f;
	}

}
