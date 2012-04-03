package org.bbop.termgenie.core.process;

import static org.junit.Assert.*;

import org.junit.Test;


public class ProcessStateTest {

	@Test
	public void testRenderTimeString() {
		assertEquals("0.001s", ProcessState.renderTimeString(0, 1));
		assertEquals("0.010s", ProcessState.renderTimeString(0, 10));
		assertEquals("0.100s", ProcessState.renderTimeString(0, 100));
		assertEquals("1.000s", ProcessState.renderTimeString(0, 1000));
		assertEquals("1.001s", ProcessState.renderTimeString(0, 1001));
		assertEquals("1.010s", ProcessState.renderTimeString(0, 1010));
	}

}
