package org.bbop.termgenie.core.io;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ListHelperTest {

	@Test
	public void testParseString() {
		assertEqualsList(ListHelper.parseString("", '|'));
		assertEqualsList(ListHelper.parseString("a", '|'),"a");
		assertEqualsList(ListHelper.parseString("a| b", '|'),"a","b");
		assertEqualsList(ListHelper.parseString("a| b|", '|'),"a","b");
		assertEqualsList(ListHelper.parseString("a| b| ", '|'),"a","b");
		assertEqualsList(ListHelper.parseString("|a| b| ", '|'),"a","b");
		assertEqualsList(ListHelper.parseString("|a| b|| ", '|'),"a","b");
	}

	@Test
	public void testSerializeList() {
		assertEquals("", ListHelper.serializeList(Arrays.<String>asList(), '|'));
		assertEquals("", ListHelper.serializeList(Arrays.asList(""), '|'));
		assertEquals("a", ListHelper.serializeList(Arrays.asList("a"), '|'));
		assertEquals("a|b", ListHelper.serializeList(Arrays.asList("a","b"), '|'));
	}

	void assertEqualsList(List<String> result, String...expected) {
		assertEquals(expected.length, result.size());
		assertArrayEquals(expected, result.toArray(new String[result.size()]));
	}
	
}
