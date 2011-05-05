package org.bbop.termgenie.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ValidateUserCredentialServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.ValidateUserCredentialService
     */
    void isValidUser( java.lang.String username, java.lang.String password, AsyncCallback<java.lang.Boolean> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static ValidateUserCredentialServiceAsync instance;

        public static final ValidateUserCredentialServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (ValidateUserCredentialServiceAsync) GWT.create( ValidateUserCredentialService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "checkuser" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
