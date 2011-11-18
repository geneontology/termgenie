package org.bbop.termgenie.svn;

import java.io.File;
import java.io.IOException;

import org.bbop.termgenie.scm.VersionControlAdapter;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


public class SvnTool implements VersionControlAdapter {

	private final File targetFolder;
	private final SVNURL repositoryURL;
	private final ISVNAuthenticationManager authManager;
	
	private SVNClientManager ourClientManager;

	public static SvnTool createAnonymousSVN(File targetFolder, String repositoryURL) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	public static SvnTool createUsernamePasswordSVN(File targetFolder, String repositoryURL, String username, String password) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	public static SvnTool createSSHKeySVN(File targetFolder, String repositoryURL, String username, File sshKeyFile, String passphrase) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(null, username, null, sshKeyFile, passphrase, false);
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	/**
	 * @param targetFolder 
	 * @param repositoryURL 
	 * @param authManager
	 */
	SvnTool(File targetFolder, String repositoryURL, ISVNAuthenticationManager authManager) {
		super();
		this.targetFolder = targetFolder;
		try {
			this.repositoryURL = SVNURL.parseURIEncoded(repositoryURL);
		} catch (SVNException exception) {
			throw new RuntimeException(exception);
		}
		this.authManager = authManager;
	}

	@Override
	public synchronized void connect() throws IOException {
		synchronized (this) {
			if (ourClientManager != null) {
				throw new IllegalStateException("You need to close() the current connection before you can create a new one.");
			}
			ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			ourClientManager = SVNClientManager.newInstance(options, authManager);
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (this) {
			if (ourClientManager != null) {
				ourClientManager.dispose();
				ourClientManager = null;
			}
		}
	}

	@Override
	public boolean checkout(String targetFile) throws IOException {
		checkConnection();
		try {
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			SVNRevision pegRevision = SVNRevision.HEAD;
			SVNRevision revision = SVNRevision.HEAD;
			SVNDepth depth = SVNDepth.INFINITY;
			updateClient.doCheckout(repositoryURL, targetFolder, pegRevision, revision, depth, true);
			File file = new File(targetFolder, targetFile);
			return file.isFile() && file.canRead() && file.canWrite();
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean commit(String message) throws IOException {
		checkConnection();
		SVNCommitClient commitClient = ourClientManager.getCommitClient();
		try {
			SVNCommitInfo info = commitClient.doCommit(new File[]{ targetFolder }, false, message, null, null, false, false, SVNDepth.INFINITY);
			SVNErrorMessage errorMessage = info.getErrorMessage();
			if (errorMessage != null) {
				throw new IOException(errorMessage.getMessage(), errorMessage.getCause());
			}
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
		return false;
	}

	@Override
	public boolean update() throws IOException {
		checkConnection();
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
		try {
			updateClient.doUpdate(targetFolder, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
			return true;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}
	
	private void checkConnection() throws IllegalStateException {
		synchronized (this) {
			if (ourClientManager == null) {
				throw new IllegalStateException("You need to call connect() before you can use this method");
			}
		}
	}

}
