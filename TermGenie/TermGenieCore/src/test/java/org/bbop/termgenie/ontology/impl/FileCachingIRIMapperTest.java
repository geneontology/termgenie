package org.bbop.termgenie.ontology.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class FileCachingIRIMapperTest {

	@Test
	public void testLocalCacheFilename() throws Exception {
		assertEquals("www.foo.bar/test.obo", FileCachingIRIMapper.localCacheFilename(new URL("http://www.foo.bar/test.obo")));
	}

	@Test
	public void testEscapeToBuffer() {
		StringBuilder sb = new StringBuilder();
		FileCachingIRIMapper.escapeToBuffer(sb, "/t?ui+7.obo");
		assertEquals(File.separator+"t_ui_7.obo", sb.toString());
	}

}
