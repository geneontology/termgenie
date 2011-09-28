package org.bbop.termgenie.services.authenticate;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class OpenIdModule extends IOCModule {

	/**
	 * This is used to construct the callback URLs for the OpenId authentication.
	 * If a change is required use a command-line parameter to overwrite for the
	 * named parameter "DefaultTermGenieUrl".
	 */
	private static final String DEFAULT_TERMGENIE_URL = "http://localhost:8080/termgenie/";
	
	private final String defaultTermGenieUrl;
	private final String openIdServletPath;
	
	
	/**
	 * @param openIdServletPath
	 */
	public OpenIdModule(String openIdServletPath) {
		this(DEFAULT_TERMGENIE_URL, openIdServletPath);
	}
	
	/**
	 * @param defaultTermGenieUrl
	 * @param openIdServletPath
	 */
	public OpenIdModule(String defaultTermGenieUrl, String openIdServletPath) {
		super();
		this.openIdServletPath = openIdServletPath;
		this.defaultTermGenieUrl = defaultTermGenieUrl;
	}

	@Override
	protected void configure() {
		bind("DefaultTermGenieUrl", defaultTermGenieUrl);
		bind(OpenIdHandler.class).to(OpenIdHandlerImpl.class);
	}
	
	@Named("OpenIdHandlerReturnToUrl")
	@Singleton
	@Provides
	protected String provideOpenIdHandlerReturnToUrl(@Named("DefaultTermGenieUrl") String termgenieURL) {
		if (termgenieURL.length() < 5 || !termgenieURL.startsWith("http")) {
			throw new RuntimeException("Unexpected format for DefaultTermGenieUrl: "+termgenieURL);
		}
		if (termgenieURL.endsWith("/")) {
			termgenieURL = termgenieURL.substring(0, termgenieURL.length() - 1);
		}
		return termgenieURL + openIdServletPath;
	}
	
	@Provides
	@Singleton
	protected OpenIdRequestHandler providesOpenIdRequestHandler(OpenIdHandler openIdHandler) {
		return openIdHandler;
	}

}
