package org.bbop.termgenie.index;

import static org.junit.Assert.*;

import java.util.List;

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
		cmp("a ", "a");
		cmp(" a ", "a");
		cmp(" ab ", "ab");
		cmp(" ab", "ab");
		cmp("ab", "ab");
		cmp("ab cd", "ab", "cd");
		cmp("ab   \t cd", "ab", "cd");
		cmp("   ab   \t cd   ", "ab", "cd");
	}

	@Test
	public void ttt() {
		assertEquals(null, AutoCompletionTools.preprocessQuery("", null));
		assertEquals(null, AutoCompletionTools.preprocessQuery("", "id"));
		assertEquals(null, AutoCompletionTools.preprocessQuery(" ", null));
		assertEquals(null, AutoCompletionTools.preprocessQuery(" ", "id"));

		assertEquals(null, AutoCompletionTools.preprocessQuery(" a ", null));
		assertEquals(null, AutoCompletionTools.preprocessQuery(" a ", "id"));

		assertEquals("(ab*) OR (\"ab\"^2)", AutoCompletionTools.preprocessQuery(" ab ", null));
		assertEquals("(ab*) OR (\"ab\"^2) OR (id:\"ab\")", AutoCompletionTools.preprocessQuery(" ab ", "id"));
		assertEquals("(ab\\(2\\+*) OR (\"ab\\(2\\+\"^2) OR (id:\"ab\\(2\\+\")", AutoCompletionTools.preprocessQuery(" ab(2+ ", "id"));
		

		assertEquals("(a* AND b*) OR (\"a b\"^2)", AutoCompletionTools.preprocessQuery(" a  b ", null));
		assertEquals("(a* AND b*) OR (\"a b\"^2) OR (id:\"a b\")",
				AutoCompletionTools.preprocessQuery(" a  b ", "id"));

		assertEquals("(a\\:b*) OR (\"a\\:b\"^2)", AutoCompletionTools.preprocessQuery(" a:b ", null));
		assertEquals("(a\\:b*) OR (\"a\\:b\"^2) OR (id:\"a\\:b\")",
				AutoCompletionTools.preprocessQuery(" a:b ", "id"));

		assertEquals("(me* AND a\\:b*) OR (\"me a\\:b\"^2)",
				AutoCompletionTools.preprocessQuery(" me  a:b ", null));
		assertEquals("(me* AND a\\:b*) OR (\"me a\\:b\"^2) OR (id:\"me a\\:b\")",
				AutoCompletionTools.preprocessQuery(" me  a:b ", "id"));
	}

	private void cmp(String s, String...strings) {
		List<String> splitt2 = AutoCompletionTools.split(s);
		assertArrayEquals(strings, splitt2.toArray());
	}
}
