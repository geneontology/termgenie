package org.bbop.termgenie.xrefs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class XrefTools {
	
	private static final Pattern def_xref_Pattern = Pattern.compile("\\S+:\\S+");

	private final static Pattern PMID_PATTERN = Pattern.compile("PMID:\\d+", Pattern.CASE_INSENSITIVE);
	private final static Pattern ISBN_PATTERN = Pattern.compile("ISBN:\\d+", Pattern.CASE_INSENSITIVE);
	private final static Pattern DOI_PATTERN = Pattern.compile("DOI:\\S+", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Check, whether the given string is an xref for a literature citation.
	 * Accepts ISBN, PMID, and DOI as valid literature references.
	 * 
	 * @param s
	 * @return boolean
	 */
	public static boolean isLiteratureReference(String s) {
		Matcher pmidMatcher = PMID_PATTERN.matcher(s);
		if (pmidMatcher.matches()) {
			return true;
		}
		Matcher isbnMatcher = ISBN_PATTERN.matcher(s);
		if (isbnMatcher.matches()) {
			return true;
		}
		Matcher doiMatcher = DOI_PATTERN.matcher(s);
		return doiMatcher.matches();
	}
	
	/**
	 * Syntax check that the given xref string is in the expected format.
	 * 
	 * @param xref
	 * @return null or error message
	 */
	public static String checkXref(String xref) {
		// simple db xref check
		if (xref.length() < 3) {
			return "The db xref " + xref + " is too short. A Definition db xref consists of a prefix and suffix with a colon (:) as separator";
		}
		if (!def_xref_Pattern.matcher(xref).matches()) {
			return "The db xref " + xref + " does not conform to the expected pattern. A db xref consists of a prefix and suffix with a colon (:) as separator and contains no whitespaces.";
		}
		return null;
	}
}
