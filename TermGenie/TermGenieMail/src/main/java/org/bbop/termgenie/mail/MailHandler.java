package org.bbop.termgenie.mail;

import java.util.List;

import org.apache.commons.mail.EmailException;

public interface MailHandler {

	public void sendEmail(String subject,
			String body,
			String from,
			String fromName,
			String to) throws EmailException;
	
	public void sendEmail(String subject,
			String body,
			String from,
			String fromName,
			List<String> to,
			List<String> cc,
			List<String> bcc) throws EmailException;

}
