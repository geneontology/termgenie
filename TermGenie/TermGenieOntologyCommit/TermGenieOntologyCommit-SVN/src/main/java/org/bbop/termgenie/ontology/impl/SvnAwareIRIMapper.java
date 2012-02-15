package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.IRIMapper;
import org.bbop.termgenie.svn.SvnTool;

import com.google.inject.Singleton;

/**
 * {@link IRIMapper} which maps files from an SVN repository.<br/>
 * Uses a fall-back {@link IRIMapper} to map unknown IRIs.
 * <br/>
 * WARNING: Do not create multiple instances with the same workFolder. May
 * lead to strange side effects, as the wokrFolder is cleared in the setup process.
 */
@Singleton
public class SvnAwareIRIMapper extends AbstractScmAwareIRIMapper<SvnAwareIRIMapper.SvnHandler> {

	/**
	 * @param fallBackIRIMapper
	 * @param repositoryURL
	 * @param targetFolder
	 * @param mappedSVNFiles
	 * @param checkout
	 */
	// Do not provide an IOC constructor, as there are multiple implementations
	// use @Provides methods instead
	public SvnAwareIRIMapper(IRIMapper fallBackIRIMapper,
			String repositoryURL,
			File targetFolder,
			Map<String, String> mappedSVNFiles,
			String checkout)
	{
		super(fallBackIRIMapper, new SvnHandler(repositoryURL, targetFolder, mappedSVNFiles, checkout));
	}

	static class SvnHandler implements AbstractScmAwareIRIMapper.ReadOnlyScm {

		private final SvnTool svn;
		private final Map<String, String> mappedSVNFiles;

		SvnHandler(String repositoryURL,
				File targetFolder,
				Map<String, String> mappedSVNFiles,
				String checkout)
		{
			this.mappedSVNFiles = mappedSVNFiles;
			svn = SvnTool.createAnonymousSVN(targetFolder, repositoryURL);
			try {
				// create work directory
				targetFolder.mkdirs();
				// always clean the work directory.
				FileUtils.cleanDirectory(targetFolder);
				svn.connect();
				boolean success = svn.checkout(checkout);
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
					Logger.getLogger(getClass()).warn("Could not close cvs connection.", exception);
				}
			}
		}

		@Override
		public void update(String url) throws IOException {
			String file = mappedSVNFiles.get(url);
			try {
				svn.connect();
				svn.update(file);
			} catch (IOException exception) {
				throw exception;
			}
			finally {
				try {
					svn.close();
				} catch (Exception exception) {
					Logger.getLogger(getClass()).warn("Could not close cvs connection.", exception);
				}
			}
		}

		@Override
		public File getFile(String url) {
			String cvsFile = mappedSVNFiles.get(url);
			return new File(svn.getTargetFolder(), cvsFile);
		}
	}

	@Override
	protected boolean isScmMapped(String url, SvnHandler svn) {
		return svn.mappedSVNFiles.containsKey(url);
	}

}
