package org.bbop.termgenie.services;

import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermTemplate;
import org.bbop.termgenie.shared.GWTGenerationResponse;
import org.bbop.termgenie.shared.GWTPair;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface GenerateTermsServiceAsync
{
	/**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.GenerateTermsService#getAvailableGWTTermTemplates
     */
	void getAvailableGWTTermTemplates(String ontology, AsyncCallback<GWTTermTemplate[]> callback);
	
    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.GenerateTermsService#generateTerms
     */
    void generateTerms(String ontology,
    		GWTPair<GWTTermTemplate, GWTTermGenerationParameter>[] allParameters,
			boolean commit, String username, String password, AsyncCallback<GWTGenerationResponse> callback);


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
