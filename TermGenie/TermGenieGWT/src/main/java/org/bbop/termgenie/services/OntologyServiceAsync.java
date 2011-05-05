package org.bbop.termgenie.services;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface OntologyServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see org.bbop.termgenie.services.OntologyService
     */
    void getAvailableOntologies( AsyncCallback<java.util.List<java.lang.String>> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static OntologyServiceAsync instance;

        public static final OntologyServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (OntologyServiceAsync) GWT.create( OntologyService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "ontology" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
