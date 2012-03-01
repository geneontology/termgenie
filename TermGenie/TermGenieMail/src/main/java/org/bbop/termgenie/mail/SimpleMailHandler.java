package org.bbop.termgenie.mail;

import java.util.Collections;
import java.util.List;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SimpleMailHandler implements MailHandler {

	private final String smtpHost;

	public SimpleMailHandler(String smtpHost) {
		super();
		this.smtpHost = smtpHost;
	}
	
	@Override
	public void sendEmail(String subject,
			String body,
			String from,
			List<String> to,
			List<String> cc,
			List<String> bcc) throws EmailException
	{
		Email email = new SimpleEmail();
		email.setHostName(smtpHost);
		email.setSmtpPort(25);
		email.setFrom(from);
		email.setSubject(subject);
		email.setMsg(body);
		if (to == null || to.isEmpty()) {
			throw new EmailException("Target e-mail address may not be empty.");
		}
		for (String emailAddress : to) {
			email.addTo(emailAddress);
		}
		
		if (cc != null) {
			for (String emailAddress : cc) {
				email.addCc(emailAddress);
			}
		}
		if (bcc != null) {
			for (String emailAddress : bcc) {
				email.addBcc(emailAddress);
			}
		}
		email.send();
		
	}

	@Override
	public void sendEmail(String subject, String body, String from, String to) throws EmailException {
		sendEmail(subject, body, from, Collections.singletonList(to), null, null);
	}

}
