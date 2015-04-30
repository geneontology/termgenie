package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.svn.SvnTool;
import org.semanticweb.owlapi.model.IRI;

public class SvnIRIMapper extends AbstractScmIRIMapper<SvnIRIMapper.SvnHandler>
{

	SvnIRIMapper(SvnTool svn,
			List<String> checkouts,
			Map<IRI, String> mappedSVNFiles,
			Set<IRI> triggers,
			String catalogXml)
	{
		super(new SvnHandler(svn, checkouts), mappedSVNFiles, triggers, catalogXml);
	}

	static class SvnHandler implements AbstractScmIRIMapper.FileAwareReadOnlyScm {

		private final SvnTool svn;
		private final List<String> checkouts;

		SvnHandler(SvnTool svn, List<String> checkouts) {
			super();
			this.svn = svn;
			this.checkouts = checkouts;
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
		public void updateSCM() throws IOException {
			try {
				svn.connect();
				svn.update(checkouts, ProcessState.NO);
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
