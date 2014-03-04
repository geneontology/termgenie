package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.cvs.CvsTools;
import org.semanticweb.owlapi.model.IRI;


public class CvsIRIMapper extends AbstractScmIRIMapper<CvsIRIMapper.CvsHandler> {

	
	public CvsIRIMapper(String cvsRoot,
			String cvsPassword,
			File workFolder,
			String checkout,
			Map<IRI, String> mappedCVSFiles,
			String catalogXml)
	{
		super(new CvsHandler(cvsRoot, cvsPassword, workFolder, checkout), mappedCVSFiles, catalogXml);
	}
	
	static class CvsHandler implements AbstractScmIRIMapper.FileAwareReadOnlyScm {

		private final CvsTools cvs;
		
		CvsHandler(String cvsRoot,
				String cvsPassword,
				File targetFolder,
				String checkout)
		{
			super();
			cvs = new CvsTools(cvsRoot, cvsPassword, targetFolder);
			try {
				// create work directory
				targetFolder.mkdirs();
				// always clean the work directory.
				FileUtils.cleanDirectory(targetFolder);
				cvs.connect();
				cvs.checkout(Collections.singletonList(checkout), ProcessState.NO);
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
			finally {
				try {
					cvs.close();
				} catch (IOException exception) {
					Logger.getLogger(getClass()).warn("Could not close cvs connection.", exception);
				}
			}
		}
		
		@Override
		public File retrieveFile(String file) throws IOException {
			return new File(cvs.getTargetFolder(), file);
		}

		@Override
		public void updateFile(File file) throws IOException {
			String path = file.getCanonicalPath();
			File targetFolder = cvs.getTargetFolder();
			final String targetPath = targetFolder.getCanonicalPath();
			if (path.startsWith(targetPath)) {
				path = path.substring(targetPath.length());
			}
			try {
				cvs.connect();
				cvs.update(Collections.singletonList(path), ProcessState.NO);
			}
			finally {
				cvs.close();
			}
		}
	}
}
