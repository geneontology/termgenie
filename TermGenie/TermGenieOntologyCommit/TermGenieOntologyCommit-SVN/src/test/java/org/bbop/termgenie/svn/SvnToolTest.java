package org.bbop.termgenie.svn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.bbop.termgenie.core.process.ProcessState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class SvnToolTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testAnonymousSVN() throws IOException {
		ProcessState state = ProcessState.NO;
		File svnfolder = folder.newFolder("svn");
		String repositoryURL = "http://termgenie.googlecode.com/svn/trunk/";
		String targetFolder = "TermGenie/TermGenieOntologyCommit/TermGenieOntologyCommit-SVN/src/main/java/org/bbop/termgenie/svn/";
		SvnTool svnTool = SvnTool.createAnonymousSVN(svnfolder, repositoryURL+targetFolder, SvnTool.getDefaultSvnConfigDir(), true);
		svnTool.connect();
		String targetFileName = SvnTool.class.getSimpleName()+".java";
		assertTrue(svnTool.checkout(Collections.singletonList(targetFileName), state));
		File testFile = new File(svnfolder, targetFileName);
		assertTrue(testFile.exists());
		assertTrue(testFile.isFile());
		assertTrue(testFile.canRead());
		assertTrue(testFile.canWrite());
		assertTrue(svnTool.update(Collections.singletonList(targetFileName), state));
		svnTool.close();
		svnTool.close();
		svnTool.close();
	}

}
