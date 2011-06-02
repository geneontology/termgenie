package org.bbop.termgenie.index;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.QueryParser;
import org.junit.Test;

/**
 * Tests for {@link AutoCompletionTools}.
 */
public class AutoCompletionToolsTest {

	@Test
	public void simpleSplit() {
		cmp("");
		cmp(" ");
		cmp("  ");
		cmp("a ","a");
		cmp(" a ","a");
		cmp(" ab ","ab");
		cmp(" ab","ab");
		cmp("ab","ab");
		cmp("ab cd","ab","cd");
		cmp("ab   \t cd","ab","cd");
		cmp("   ab   \t cd   ","ab","cd");
	}

	private static class MyAutoCompletionTools extends AutoCompletionTools<String> {

		@Override
		protected String escape(String string) {
			return QueryParser.escape(string);
		}

		@Override
		protected String getLabel(String t) {
			return t;
		}
		
	}
	
	@Test
	public void ttt() {
		MyAutoCompletionTools instance = new MyAutoCompletionTools();
		
		assertEquals(null, instance.preprocessQuery(""));
		assertEquals(null, instance.preprocessQuery(" "));
		assertEquals(null, instance.preprocessQuery(" a "));
		assertEquals("(ab*) OR (\"ab\"^2)", instance.preprocessQuery(" ab "));
		assertEquals("(a* AND b*) OR (\"a b\"^2)", instance.preprocessQuery(" a  b "));
		assertEquals("(a\\:b*) OR (\"a\\:b\"^2)", instance.preprocessQuery(" a:b "));
		assertEquals("(me* AND a\\:b*) OR (\"me a\\:b\"^2)", instance.preprocessQuery(" me  a:b "));
	}
	
	private void cmp(String s, String...strings) {
		List<String> splitC1 = splitCompare(s);
		assertArrayEquals(strings, splitC1.toArray());
		
		List<String> splitC2 = splitCompare2(s);
		assertArrayEquals(strings, splitC2.toArray());
		
		List<String> splitt2 = AutoCompletionTools.split(s);
		assertArrayEquals(splitC1.toArray(), splitt2.toArray());
	}
	
	/**
	 * Reference implementation using regex.
	 * 
	 * @param s
	 * @return list of substrings
	 */
	private static List<String> splitCompare(String s) {
		if (s.isEmpty()) {
			return Collections.emptyList();
		}
		// slow version
		s = s.replaceAll("\\s+", " "); // replace all white space with a single whitespace
		if (s.length() == 1 && Character.isWhitespace(s.charAt(0))) {
			return Collections.emptyList();
		}
		s = s.trim();
		String[] split = s.split("\\s");
		return Arrays.asList(split);
	}
	
	private static final Pattern whitespacePattern = Pattern.compile("\\s+");
	private static final Pattern splitPattern = Pattern.compile("\\s");
	
	/**
	 * Reference implementation using pre-compiled regex.
	 * 
	 * @param s
	 * @return list of substrings
	 */
	private static List<String> splitCompare2(String s) {
		if (s.isEmpty()) {
			return Collections.emptyList();
		}
		// slow version
		s = whitespacePattern.matcher(s).replaceAll(" "); // replace all white space with a single whitespace
		if (s.length() == 1 && Character.isWhitespace(s.charAt(0))) {
			return Collections.emptyList();
		}
		s = s.trim();
		String[] split = splitPattern.split(s);
		return Arrays.asList(split);
	}
	
	
	/**
	 * Main for doing a simple benchmark about the speedup of using a one pass
	 * algorithm vs. regular expressions.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		for (int j = 0; j < 10; j++) {
			long start1 = System.currentTimeMillis();
			// takes about 995 ms
			for (int i = 0; i < 100000; i++) {
				splitCompare("");
				splitCompare(" ");
				splitCompare("  ");
				splitCompare(" a ");
				splitCompare(" ab ");
				splitCompare(" ab");
				splitCompare("ab");
				splitCompare("ab cd");
				splitCompare("ab   \t cd");
				splitCompare("   ab   \t cd   ");
			}
			long end1 = System.currentTimeMillis();
			System.out.println(end1 - start1);
			
			long start2 = System.currentTimeMillis();
			// takes about 640 ms
			for (int i = 0; i < 100000; i++) {
				splitCompare2("");
				splitCompare2(" ");
				splitCompare2("  ");
				splitCompare2(" a ");
				splitCompare2(" ab ");
				splitCompare2(" ab");
				splitCompare2("ab");
				splitCompare2("ab cd");
				splitCompare2("ab   \t cd");
				splitCompare2("   ab   \t cd   ");
			}
			long end2 = System.currentTimeMillis();
			System.out.println(end2 - start2);
			
			long start3 = System.currentTimeMillis();
			// takes about 47 ms
			for (int i = 0; i < 100000; i++) {
				AutoCompletionTools.split("");
				AutoCompletionTools.split(" ");
				AutoCompletionTools.split("  ");
				AutoCompletionTools.split(" a ");
				AutoCompletionTools.split(" ab ");
				AutoCompletionTools.split(" ab");
				AutoCompletionTools.split("ab");
				AutoCompletionTools.split("ab cd");
				AutoCompletionTools.split("ab   \t cd");
				AutoCompletionTools.split("   ab   \t cd   ");
			}
			long end3 = System.currentTimeMillis();
			System.out.println(end3 - start3);
		}
	}
}
