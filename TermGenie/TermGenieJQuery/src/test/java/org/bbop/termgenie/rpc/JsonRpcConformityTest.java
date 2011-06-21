package org.bbop.termgenie.rpc;

import org.bbop.termgenie.services.GenerateTermsService;
import org.bbop.termgenie.services.OntologyService;
import org.bbop.termgenie.services.TermCommitService;
import org.json.rpc.commons.GsonTypeChecker;
import org.json.rpc.commons.TypeChecker;

public class JsonRpcConformityTest {

	public static void main(String[] args) {
		TypeChecker checker = new GsonTypeChecker();
		
		boolean validInterface = checker.isValidInterface(GenerateTermsService.class, true);
		if (!validInterface) {
			System.err.println("invalid generateTerms");
		}
		
		validInterface = checker.isValidInterface(OntologyService.class, true);
		if (!validInterface) {
			System.err.println("invalid ontology");
		}
		
		validInterface = checker.isValidInterface(TermCommitService.class, true);
		if (!validInterface) {
			System.err.println("invalid term commit service");
		}
	}
}
