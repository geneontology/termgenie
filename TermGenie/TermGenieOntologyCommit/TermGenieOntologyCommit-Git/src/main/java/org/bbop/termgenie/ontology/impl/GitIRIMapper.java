package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.git.GitTool;
import org.semanticweb.owlapi.model.IRI;


public class GitIRIMapper extends AbstractScmIRIMapper<GitIRIMapper.GitHandler> {

	// generated
	private static final long serialVersionUID = 624601115344754544L;

	GitIRIMapper(GitTool git,
			List<String> checkouts,
			Map<IRI, String> mappedSVNFiles,
			Set<IRI> updateTriggers,
			String catalogXml)
	{
		super(new GitHandler(git, checkouts), mappedSVNFiles, updateTriggers, catalogXml);
	}
	
	static class GitHandler implements AbstractScmIRIMapper.FileAwareReadOnlyScm {

		private final GitTool git;
		private List<String> checkouts;

		GitHandler(GitTool git, List<String> checkouts) {
			super();
			this.git = git;
			this.checkouts = checkouts;
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
		public void updateSCM() throws IOException {
			try {
				git.connect();
				git.update(checkouts, ProcessState.NO);
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
