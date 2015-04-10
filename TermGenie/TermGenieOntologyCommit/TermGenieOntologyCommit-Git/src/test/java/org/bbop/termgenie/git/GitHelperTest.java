package org.bbop.termgenie.git;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.git.GitTool;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class GitHelperTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testAnonymousGit() throws IOException {
		ProcessState state = ProcessState.NO;
		File gitfolder = folder.newFolder("git");
		String repositoryURL = "https://github.com/geneontology/termgenie.git";
		String target = "TermGenie/TermGenieOntologyCommit/TermGenieOntologyCommit-Git/pom.xml";
		GitTool gitTool = GitTool.createAnonymousGit(gitfolder, repositoryURL);
		gitTool.connect();
		assertTrue(gitTool.checkout(Collections.singletonList(target), state));
		File testFile = new File(gitfolder, target);
		assertTrue(testFile.exists());
		assertTrue(testFile.isFile());
		assertTrue(testFile.canRead());
		assertTrue(testFile.canWrite());
		assertTrue(gitTool.update(Collections.singletonList(target), state));
		gitTool.close();
		gitTool.close();
		gitTool.close();
	}
}
