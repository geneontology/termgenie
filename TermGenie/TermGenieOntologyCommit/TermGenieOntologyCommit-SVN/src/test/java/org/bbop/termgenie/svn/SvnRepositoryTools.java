package org.bbop.termgenie.svn;

import java.io.File;

import org.bbop.termgenie.svn.SvnTool.SvnLogger;
import org.bbop.termgenie.tools.Triple;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import org.tmatesoft.svn.util.SVNDebugLog;


/**
 * Tools for creating a local SVN repositories. For test purposes only.
 */
public class SvnRepositoryTools {

	static {
		synchronized (SVNDebugLog.class) {
			if (SVNDebugLog.getDefaultLog() != SvnLogger.INSTANCE) {
				SVNDebugLog.setDefaultLog(SvnLogger.INSTANCE);
			}
		}
	}
	
	static Triple<SvnTool, SVNURL, ISVNAuthenticationManager> createLocalRepository(File repositoryDirectory, File stagingDirectory, File checkoutDirectory) {
		final File svnConfigDir = SvnTool.getDefaultSvnConfigDir();
		final ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir);
		final ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		final SVNClientManager ourClientManager = SVNClientManager.newInstance(options, authManager);
		
		try {
			final SVNAdminClient adminClient = new SVNAdminClient(authManager, options);
			adminClient.doCreateRepository(repositoryDirectory, null, false, false);
			
			final SVNCommitClient commitClient = new SVNCommitClient(authManager, options);
			final SVNURL dstURL = SVNURL.fromFile(repositoryDirectory);
			commitClient.doImport(stagingDirectory, dstURL, "Initial Import", null, false, true, SVNDepth.INFINITY);
			SvnTool tool= new SvnTool(checkoutDirectory, dstURL, authManager);
			return new Triple<SvnTool, SVNURL, ISVNAuthenticationManager>(tool, dstURL, authManager);
		} catch (SVNException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			ourClientManager.dispose();			
		}
	}
}
