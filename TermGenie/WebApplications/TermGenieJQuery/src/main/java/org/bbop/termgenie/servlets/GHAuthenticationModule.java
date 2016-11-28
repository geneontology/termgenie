package org.bbop.termgenie.servlets;

import org.bbop.termgenie.core.ioc.IOCModule;

import java.util.Properties;

/**
 * Created by nathandunn on 11/28/16.
 */
public class GHAuthenticationModule extends IOCModule{

    public GHAuthenticationModule(Properties properties){
        super(properties);
    }

    @Override
    protected void configure() {
        bindSecret("github_client_id");
        bindSecret("github_client_secret");
    }
}
