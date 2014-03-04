package org.bbop.termgenie.rpc;

import java.net.MalformedURLException;
import java.net.URL;

import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.OntologyService.JsonOntologyStatus;
import org.json.rpc.client.HttpJsonRpcClientTransport;
import org.json.rpc.client.JsonRpcInvoker;
import org.json.rpc.server.InjectingGsonTypeChecker;

public class SimpleJsonRPCTestClient {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws MalformedURLException {
		String url = "http://127.0.0.1:8080/termgenie/jsonrpc";

		HttpJsonRpcClientTransport transport = new HttpJsonRpcClientTransport(new URL(url));

		JsonRpcInvoker invoker = new JsonRpcInvoker(new InjectingGsonTypeChecker());
		OntologyService service = invoker.get(transport, "ontology", OntologyService.class);

		JsonOntologyStatus ontologyStatus = service.getOntologyStatus();
		System.out.println("Ontology status: ");
		System.out.println(" ontology: "+ontologyStatus.ontology);
		System.out.println(" okay:     "+ontologyStatus.okay);
		System.out.println(" messages:  "+ontologyStatus.messages);
		System.out.println();
		
		JsonTermSuggestion[] suggestions = service.autocomplete(null,
				"pig",
				"GeneOntology",
				5);
		for (JsonTermSuggestion suggestion : suggestions) {
			System.out.println(suggestion);
		}
	}
}
