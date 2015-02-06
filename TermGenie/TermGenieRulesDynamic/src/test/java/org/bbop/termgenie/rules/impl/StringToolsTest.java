package org.bbop.termgenie.rules.impl;

import static org.junit.Assert.*;

import org.junit.Test;


public class StringToolsTest {

	@Test
	public void test() throws Exception {
		assertEquals("", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase(""));
		assertEquals("A", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("a"));
		assertEquals("A", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("A"));
		assertEquals("1", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("1"));
		assertEquals("123456789", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("123456789"));
		assertEquals("Abcdedfg", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("abcdedfg"));
		assertEquals("Abcdedfg", TermGenieScriptFunctionsMDefImpl.firstLetterToUpperCase("Abcdedfg"));
	}
}
