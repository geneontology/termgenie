package org.bbop.termgenie.services.authenticate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Module configuring different authentication implementations. Currently
 * supported: BrowserID and OpenId.
 */
public class AuthenticationModule extends IOCModule {

	/**
	 * This is used to construct the callback URLs for the OpenId and BrowserID
	 * authentication. If a change is required use a command-line parameter to
	 * overwrite for the named parameter "DefaultTermGenieUrl".
	 */
	private static final String DEFAULT_TERMGENIE_URL = "http://localhost:8080/termgenie/";
	
	private static final String DEFAUL_BROWSER_ID_VERFICATION_URL = "https://browserid.org/verify";

	private final String defaultTermGenieUrl;
	private final String openIdServletPath;

	/**
	 * @param openIdServletPath
	 * @param applicationProperties
	 */
	public AuthenticationModule(String openIdServletPath, Properties applicationProperties) {
		this(DEFAULT_TERMGENIE_URL, openIdServletPath, applicationProperties);
	}

	/**
	 * @param defaultTermGenieUrl
	 * @param openIdServletPath
	 * @param applicationProperties
	 */
	public AuthenticationModule(String defaultTermGenieUrl, String openIdServletPath, Properties applicationProperties) {
		super(applicationProperties);
		this.openIdServletPath = openIdServletPath;
		this.defaultTermGenieUrl = defaultTermGenieUrl;
	}

	@Override
	protected void configure() {
		bind("DefaultTermGenieUrl", defaultTermGenieUrl);
		bind(OpenIdHandler.class, OpenIdHandlerImpl.class);
		bind(BrowserIdHandler.class, BrowserIdHandlerImpl.class);
		bind("BrowserIdVerificationUrl",DEFAUL_BROWSER_ID_VERFICATION_URL);
	}

	@Named("OpenIdHandlerReturnToUrl")
	@Singleton
	@Provides
	protected String provideOpenIdHandlerReturnToUrl(@Named("DefaultTermGenieUrl") String termgenieURL)
	{
		if (termgenieURL.length() < 5 || !termgenieURL.startsWith("http")) {
			throw new RuntimeException("Unexpected format for DefaultTermGenieUrl: " + termgenieURL);
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
	
	@Provides
	@Singleton
	@Named("TermGenieBrowserIdAudience")
	protected String providesTermGenieBrowserIdAudience(@Named("DefaultTermGenieUrl") String termgenieURL) {
		try {
			URL url = new URL(termgenieURL);
			int port = url.getPort();
			String host = url.getHost();
			if (port < 0 || port == url.getDefaultPort()) {
				return host;
			}
			return host+":"+Integer.toString(port);
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}

}
