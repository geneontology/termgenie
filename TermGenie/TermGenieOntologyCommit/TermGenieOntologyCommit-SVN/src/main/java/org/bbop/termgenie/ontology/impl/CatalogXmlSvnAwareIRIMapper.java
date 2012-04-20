package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;

public class CatalogXmlSvnAwareIRIMapper extends AbstractCatalogXmlScmAwareIRIMapper<CatalogXmlSvnAwareIRIMapper.SvnHandler>
{

	CatalogXmlSvnAwareIRIMapper(IRIMapper fallBackIRIMapper,
			SvnTool svn,
			String checkout,
			String catalogXml)
	{
		super(fallBackIRIMapper, new SvnHandler(svn, checkout), catalogXml);
	}

	static class SvnHandler implements AbstractCatalogXmlScmAwareIRIMapper.FileAwareReadOnlyScm {

		private final SvnTool svn;

		SvnHandler(SvnTool svn, String checkout) {
			super();
			this.svn = svn;
			try {
				File targetFolder = svn.getTargetFolder();
				// create work directory
				targetFolder.mkdirs();
				// always clean the work directory.
				FileUtils.cleanDirectory(targetFolder);
				svn.connect();
				boolean success = svn.checkout(Collections.singletonList(checkout));
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
			svn.update(Collections.singletonList(path));
		}

	}
}
