package org.bbop.termgenie.core.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool for reading and writing a list of strings.
 * TODO: encode or replace separator chars and newline chars in the input string.
 */
public class ListHelper {

	public static List<String> parseString(String serializedString, char separatorChar) {
		List<String> prefixes = null;
		if (serializedString != null) {
			serializedString = serializedString.trim();
			prefixes = new ArrayList<String>();
			int start = 0;
			int pos;
			int length = serializedString.length();
			while ((pos = serializedString.indexOf(separatorChar, start)) >= 0) {
				prefixes.add(serializedString.substring(start, pos));
				start = Math.min(pos + 1, length);
			}
			if (start <  length) {
				prefixes.add(serializedString.substring(start));
			}
		}
		return prefixes;
	}
	
	public static String serializeList(List<String> list, char separatorChar) {
		StringBuilder sb = new StringBuilder();
		for (String element : list) {
			if (sb.length() > 0) {
				sb.append(separatorChar);
			}
			sb.append(element);
		}
		return sb.toString();
	}
}
