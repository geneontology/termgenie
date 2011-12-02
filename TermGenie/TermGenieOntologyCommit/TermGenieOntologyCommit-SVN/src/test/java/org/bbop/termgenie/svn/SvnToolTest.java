package org.bbop.termgenie.svn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.bbop.termgenie.tools.TempTestFolderTools;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class SvnToolTest {

	private static File testFolder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testFolder = TempTestFolderTools.createTestFolder(SvnToolTest.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TempTestFolderTools.deleteTestFolder(testFolder);
	}

	@Test
	public void testAnonymousSVN() throws IOException {
		File svnfolder = new File(testFolder, "svn");
		String repositoryURL = "http://termgenie.googlecode.com/svn/trunk/";
		String targetFolder = "TermGenie/TermGenieOntologyCommit/TermGenieOntologyCommit-SVN/src/main/java/org/bbop/termgenie/svn/";
		SvnTool svnTool = SvnTool.createAnonymousSVN(svnfolder, repositoryURL+targetFolder);
		svnTool.connect();
		String targetFileName = SvnTool.class.getSimpleName()+".java";
		assertTrue(svnTool.checkout(targetFileName));
		File testFile = new File(svnfolder, targetFileName);
		assertTrue(testFile.exists());
		assertTrue(testFile.isFile());
		assertTrue(testFile.canRead());
		assertTrue(testFile.canWrite());
		assertTrue(svnTool.update(targetFileName));
		svnTool.close();
		svnTool.close();
		svnTool.close();
	}

}
