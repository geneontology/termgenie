package org.bbop.termgenie.mail;

import org.junit.Ignore;
import org.junit.Test;


public class MailHandlerTest {

	@Test
	@Ignore
	public void test() throws Exception {
		MailHandler h = new SimpleMailHandler("smtp.lbl.gov");
		h.sendEmail("Test 1",
				"This is a test message send via java",
				"help@go.termgenie.org",
				"hdietze@lbl.gov");
	}

}
