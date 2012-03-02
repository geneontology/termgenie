package org.bbop.termgenie.mail;

import java.util.Collections;
import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class TLSMailHelper implements MailHandler {

	private final String smtpHost;
	private final int smtpPort;
	private final String username;
	private final String password;

	public TLSMailHelper(String smtpHost, int smtpPort, String username, String password) {
		super();
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.username = username;
		this.password = password;
	}

	@Override
	public void sendEmail(String subject,
			String body,
			String from,
			String fromName,
			List<String> to,
			List<String> cc,
			List<String> bcc) throws EmailException
	{
		Email email = new SimpleEmail();
		email.setHostName(smtpHost);
		email.setSmtpPort(smtpPort);
		email.setAuthenticator(new DefaultAuthenticator(username, password));
		email.setTLS(true);
		email.setFrom(from, fromName);
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
	public void sendEmail(String subject, String body, String from, String fromName, String to)
			throws EmailException
	{
		sendEmail(subject, body, from, fromName, Collections.singletonList(to), null, null);
	}
}
