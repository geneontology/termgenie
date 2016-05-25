package org.bbop.termgenie.mail.review;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;
import org.bbop.termgenie.mail.SimpleMailHandler;

public class DefaultReviewMailHandlerModule extends IOCModule {

	private final String defaultFromAddress;
	private final String defaultFromName;
	private final String smtpHost;

	public DefaultReviewMailHandlerModule(Properties applicationProperties,
			String fromAddress, String fromName, String smtpHost) {
		super(applicationProperties);
		this.defaultFromAddress = fromAddress;
		this.defaultFromName = fromName;
		this.smtpHost = smtpHost;
	}

	@Override
	protected void configure() {
		bind(ReviewMailHandler.class, DefaultReviewMailHandler.class);
		bind(MailHandler.class, new SimpleMailHandler(smtpHost));
		bind("DefaultReviewMailHandlerFromAddress", defaultFromAddress);
		bind("DefaultReviewMailHandlerFromName", defaultFromName);
	}
}
