package org.bbop.termgenie.server;

import java.util.Arrays;
import java.util.List;

import org.bbop.termgenie.services.OntologyService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {

	@Override
	public List<String> getAvailableOntologies() {
		// TODO Where do you get the list of available ontologies from?
		return Arrays.asList("GeneOntology", "Test1","Test2");
	}

}
