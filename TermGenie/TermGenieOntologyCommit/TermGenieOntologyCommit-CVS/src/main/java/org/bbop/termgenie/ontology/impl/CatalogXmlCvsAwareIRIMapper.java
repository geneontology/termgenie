package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.cvs.CvsTools;
import org.bbop.termgenie.ontology.IRIMapper;


public class CatalogXmlCvsAwareIRIMapper extends AbstractCatalogXmlScmAwareIRIMapper<CatalogXmlCvsAwareIRIMapper.CatalogXmlAwareCvsHandler> {

	
	public CatalogXmlCvsAwareIRIMapper(IRIMapper fallBackIRIMapper,
			String cvsRoot,
			String cvsPassword,
			File workFolder,
			String checkout,
			String catalogXml)
	{
		super(fallBackIRIMapper, new CatalogXmlAwareCvsHandler(cvsRoot, cvsPassword, workFolder, checkout), catalogXml);
	}
	
	

	static class CatalogXmlAwareCvsHandler implements AbstractCatalogXmlScmAwareIRIMapper.FileAwareReadOnlyScm {

		private final CvsTools cvs;
		
		CatalogXmlAwareCvsHandler(String cvsRoot,
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
				cvs.checkout(checkout);
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
			cvs.update(path);
		}
		
	}
}
