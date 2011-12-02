package org.bbop.termgenie.cvs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CvsToolsTest {

	private static File testFolder = null;
	
	@BeforeClass
	public static void beforeClass() {
		testFolder = TempTestFolderTools.createTestFolder(CvsToolsTest.class);
	}
	
	@AfterClass
	public static void afterClass() {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}
	
	@Test
	public void testCVSTools() throws IOException {
		String cvsRoot = ":pserver:anonymous@cvs.geneontology.org:/anoncvs";
		CvsTools tools = new CvsTools(cvsRoot, null, testFolder);
		tools.connect();
		boolean checkout = tools.checkout("go/ontology/obo_format_1_2");
		tools.close();
		assertTrue(checkout);
		String content = FileUtils.readFileToString(new File(testFolder,"go/ontology/obo_format_1_2/README"));
		assertNotNull(content);
		
		tools.connect();
		boolean update = tools.update("go/ontology/obo_format_1_2");
		tools.close();
		assertTrue(update);
	}

}
