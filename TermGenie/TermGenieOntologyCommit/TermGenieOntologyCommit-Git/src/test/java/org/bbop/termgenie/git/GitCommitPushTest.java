package org.bbop.termgenie.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.tools.ResourceLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class GitCommitPushTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	private File currentFolder = null;
	
	@Before
	public void beforeClass() throws Exception {
		currentFolder = this.testFolder.newFolder();
		
		// general layout
		File repositoryDirectory = new File(currentFolder, "repository");
		FileUtils.forceMkdir(repositoryDirectory);
		
		File stagingDirectory = new File(currentFolder, "staging");
		FileUtils.forceMkdir(stagingDirectory);
		
		File readOnlyDirectory = new File(currentFolder, "readonly");
		FileUtils.forceMkdir(readOnlyDirectory);
		
		TestResourceLoader loader = new TestResourceLoader();
		
		// file staging with content
		File trunk = new File(stagingDirectory, "trunk");
		trunk.mkdirs();
		File ontologyFolder = new File(trunk, "ontology");
		ontologyFolder.mkdirs();
		File ontologyFile = new File(ontologyFolder, "git-test-main.obo");
		loader.copyResource("git-test-main.obo", ontologyFile);
		
		// create repository and java adapter
	}
	
	@Test
	public void testCommitPush() throws Exception {
		// TODO
	}
	
	private static class TestResourceLoader extends ResourceLoader {

		protected TestResourceLoader() {
			super(false);
		}
		
		public void copyResource(String resource, File target) throws IOException {
			InputStream input = loadResource(resource);
			OutputStream output = new FileOutputStream(target);
			IOUtils.copy(input, output);
			input.close();
			output.close();
		}
	}
}
