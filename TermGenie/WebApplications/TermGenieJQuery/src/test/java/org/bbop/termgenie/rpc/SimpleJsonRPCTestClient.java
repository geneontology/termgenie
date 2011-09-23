package org.bbop.termgenie.rpc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.bbop.termgenie.data.JsonTermSuggestion;
import org.bbop.termgenie.services.OntologyService;
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

		String[] availableOntologies = service.availableOntologies(null);
		System.out.println(Arrays.toString(availableOntologies));

		JsonTermSuggestion[] suggestions = service.autocomplete(null,
				"pig",
				new String[] { "GeneOntology" },
				5);
		for (JsonTermSuggestion suggestion : suggestions) {
			System.out.println(suggestion);
		}
	}
}
