package org.bbop.termgenie.services.review.mail;

import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;
import org.bbop.termgenie.mail.MailHandler;

import com.google.inject.Provides;
import com.google.inject.Singleton;


public abstract class DefaultReviewMailHandlerModule extends IOCModule {

	private final String defaultFromAddress;
	private final String defaultFromName;

	public DefaultReviewMailHandlerModule(Properties applicationProperties, String fromAddress,  String fromName) {
		super(applicationProperties);
		this.defaultFromAddress = fromAddress;
		this.defaultFromName = fromName;
	}

	@Override
	protected void configure() {
		bind(ReviewMailHandler.class, DefaultReviewMailHandler.class);
		bind("DefaultReviewMailHandlerFromAddress", defaultFromAddress);
		bind("DefaultReviewMailHandlerFromName", defaultFromName);
	}
	
	@Singleton
	@Provides
	protected abstract MailHandler provideMailHandler();
	
}
