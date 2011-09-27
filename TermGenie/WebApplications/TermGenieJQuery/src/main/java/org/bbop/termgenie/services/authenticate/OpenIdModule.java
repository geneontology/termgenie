package org.bbop.termgenie.services.authenticate;

import org.bbop.termgenie.core.ioc.IOCModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class OpenIdModule extends IOCModule {

	private final String openIdHandlerReturnToUrl;
	
	/**
	 * @param termgenieUrl
	 * @param openIdServletPath
	 */
	public OpenIdModule(String termgenieUrl, String openIdServletPath) {
		super();
		this.openIdHandlerReturnToUrl = termgenieUrl + openIdServletPath;
	}

	@Override
	protected void configure() {
		bind("OpenIdHandlerReturnToUrl", openIdHandlerReturnToUrl);
		bind(OpenIdHandler.class).to(OpenIdHandlerImpl.class);
	}
	
	@Provides
	@Singleton
	protected OpenIdRequestHandler providesOpenIdRequestHandler(OpenIdHandler openIdHandler) {
		return openIdHandler;
	}

}
