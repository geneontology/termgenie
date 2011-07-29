package org.bbop.termgenie.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Tools for auto-completion.
 * 
 * @param <T> 
 */
public abstract class AutoCompletionTools<T> {

	/**
	 * Split the string into tokens using white spaces. 
	 * Ignore multiple white spaces (begin, end, and in-between)
	 * 
	 * @param s
	 * @return list of non-white space sub strings
	 */
	public static List<String> split(String s) {
		if (s.isEmpty()) {
			return Collections.emptyList();
		}
		
		int start = -1;
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isWhitespace(s.charAt(i))) {
				start = i;
				break;
			}
		}
		if (start < 0) {
			return Collections.emptyList();
		}
		List<String> tokens = new ArrayList<String>();
		for (int i = start; i < s.length(); i++) {
			if (Character.isWhitespace(s.charAt(i))) {
				if (start >= 0) {
					tokens.add(s.substring(start, i));
					start = -1;
				}
			}
			else {
				if (start < 0) {
					start = i;
				}
			}
		}
		if (start >= 0) {
			tokens.add(s.substring(start, s.length()));
		}
		return tokens;
	}
	
	/**
	 * Pre-process the query String, return null, if no valid tokens are identified 
	 * 
	 * @param queryString
	 * @param idField name of the ID Field
	 * @return string or null
	 */
	public String preprocessQuery(String queryString, String idField) {
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
			string = escape(string);
			if (subquery1.length() == 0) {
				subquery1.append('(');
				subquery2.append("(\"");
				if (idField != null) {
					subquery3.append("(");
					subquery3.append(idField);
					subquery3.append(":\"");
				}
			}
			else{
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
	
	protected abstract String escape(String string);
	
	public void sortbyLabelLength(List<T> documents) {
		Collections.sort(documents, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				final String label1 = getLabel(o1);
				final String label2 = getLabel(o2);
				int l1 = label1.length();
				int l2 = label2.length();
				return (l1 < l2 ? -1 : (l1 == l2 ? 0 : 1));
			}
		});
	}
	
	public static boolean fEquals(float f1, float f2) {
		return Math.abs(f1 - f2) < 0.0001f;
	}
	
	protected abstract String getLabel(T t);
}
