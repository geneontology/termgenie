package org.bbop.termgenie.rpc;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.ValidateUserCredentialService;
import org.json.rpc.commons.GsonTypeChecker;
import org.json.rpc.commons.TypeChecker;

public class JsonRpcConformityTest {

	public static void main(String[] args) {
		TypeChecker checker = new GsonTypeChecker();
		
		boolean validInterface = checker.isValidInterface(GenerateTermsService.class);
		if (!validInterface) {
			System.err.println("invalid");
		}
		
		validInterface = checker.isValidInterface(OntologyService.class);
		if (!validInterface) {
			System.err.println("invalid");
		}
		
		validInterface = checker.isValidInterface(ValidateUserCredentialService.class);
		if (!validInterface) {
			System.err.println("invalid");
		}
	}
}
