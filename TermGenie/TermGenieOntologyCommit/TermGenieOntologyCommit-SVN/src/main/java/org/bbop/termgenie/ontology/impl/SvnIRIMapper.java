package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;
import org.semanticweb.owlapi.model.IRI;

public class SvnIRIMapper extends AbstractScmIRIMapper<SvnIRIMapper.SvnHandler>
{

	SvnIRIMapper(IRIMapper fallBackIRIMapper,
			SvnTool svn,
			List<String> checkouts,
			Map<IRI, String> mappedSVNFiles,
			String catalogXml)
	{
		super(fallBackIRIMapper, new SvnHandler(svn, checkouts), mappedSVNFiles, catalogXml);
	}

	static class SvnHandler implements AbstractScmIRIMapper.FileAwareReadOnlyScm {

		private final SvnTool svn;

		SvnHandler(SvnTool svn, List<String> checkouts) {
			super();
			this.svn = svn;
			try {
				File targetFolder = svn.getTargetFolder();
				// create work directory
				targetFolder.mkdirs();
				// always clean the work directory.
				FileUtils.cleanDirectory(targetFolder);
				svn.connect();
				boolean success = svn.checkout(checkouts, ProcessState.NO);
				if (!success) {
					throw new RuntimeException("Checkout not successfull");
				}
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
			finally {
				try {
					svn.close();
				} catch (IOException exception) {
					Logger.getLogger(getClass()).warn("Could not close svn connection.", exception);
				}
			}
		}

		@Override
		public File retrieveFile(String file) throws IOException {
			return new File(svn.getTargetFolder(), file);
		}

		@Override
		public void updateFile(File file) throws IOException {
			String path = file.getCanonicalPath();
			File targetFolder = svn.getTargetFolder();
			final String targetPath = targetFolder.getCanonicalPath();
			if (path.startsWith(targetPath)) {
				path = path.substring(targetPath.length());
			}
			try {
				svn.connect();
				svn.update(Collections.singletonList(path), ProcessState.NO);
			}
			finally {
				try {
					svn.close();
				} catch (IOException exception) {
					Logger.getLogger(getClass()).warn("Could not close svn connection.", exception);
				}
			}
		}
	}
}
