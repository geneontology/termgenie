package org.bbop.termgenie.services;

import org.bbop.termgenie.shared.GWTTermTemplate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface GenerateTermsServiceAsync
{
	void getAvailableGWTTermTemplates(String ontology, AsyncCallback<GWTTermTemplate[]> callback);
	
    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.GenerateTermsService
     */
    void checkTerms( AsyncCallback<java.lang.Boolean> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.GenerateTermsService
     */
    void generateTerms( AsyncCallback<java.lang.Boolean> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static GenerateTermsServiceAsync instance;

        public static final GenerateTermsServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = GWT.create( GenerateTermsService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "generate" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
