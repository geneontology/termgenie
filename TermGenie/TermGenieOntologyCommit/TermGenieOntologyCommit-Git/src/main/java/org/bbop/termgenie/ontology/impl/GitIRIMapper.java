package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.git.GitTool;
import org.semanticweb.owlapi.model.IRI;


public class GitIRIMapper extends AbstractScmIRIMapper<GitIRIMapper.GitHandler> {

	GitIRIMapper(GitTool git,
			List<String> checkouts,
			Map<IRI, String> mappedSVNFiles,
			String catalogXml)
	{
		super(new GitHandler(git, checkouts), mappedSVNFiles, catalogXml);
	}
	
	static class GitHandler implements AbstractScmIRIMapper.FileAwareReadOnlyScm {

		private final GitTool git;

		GitHandler(GitTool git, List<String> checkouts) {
			super();
			this.git = git;
			try {
				File targetFolder = git.getTargetFolder();
				// create work directory
				targetFolder.mkdirs();
				// always clean the work directory.
				FileUtils.cleanDirectory(targetFolder);
				git.connect();
				boolean success = git.checkout(checkouts, ProcessState.NO);
				if (!success) {
					throw new RuntimeException("Checkout not successfull");
				}
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
			finally {
				try {
					git.close();
				} catch (IOException exception) {
					Logger.getLogger(getClass()).warn("Could not close git connection.", exception);
				}
			}
		}

		@Override
		public File retrieveFile(String file) throws IOException {
			return new File(git.getTargetFolder(), file);
		}

		@Override
		public void updateFile(File file) throws IOException {
			String path = file.getCanonicalPath();
			File targetFolder = git.getTargetFolder();
			final String targetPath = targetFolder.getCanonicalPath();
			if (path.startsWith(targetPath)) {
				path = path.substring(targetPath.length());
			}
			try {
				git.connect();
				git.update(Collections.singletonList(path), ProcessState.NO);
			}
			finally {
				try {
					git.close();
				} catch (IOException exception) {
					Logger.getLogger(getClass()).warn("Could not close git connection.", exception);
				}
			}
		}
	}
}
