package org.bbop.termgenie.svn;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.log4j.Logger;
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
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;


public class SvnTool implements VersionControlAdapter {
	
	private static final Logger logger = Logger.getLogger(SvnTool.class);
	
	static {
		SVNDebugLog.setDefaultLog(new SvnLogger());
	}

	private final File targetFolder;
	private final SVNURL repositoryURL;
	private final ISVNAuthenticationManager authManager;
	
	private SVNClientManager ourClientManager;

	public static SvnTool createAnonymousSVN(File targetFolder, String repositoryURL, File svnConfigDir) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir);
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	public static SvnTool createUsernamePasswordSVN(File targetFolder, String repositoryURL, String username, String password, File svnConfigDir) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir, username, password);
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	public static SvnTool createSSHKeySVN(File targetFolder, String repositoryURL, String username, File sshKeyFile, String passphrase, File svnConfigDir) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir, username, null, sshKeyFile, passphrase, false);
		return new SvnTool(targetFolder, repositoryURL, authManager);
	}
	
	public static File getDefaultSvnConfigDir() {
		return SVNWCUtil.getDefaultConfigurationDirectory();
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
			logger.info("Start checkout for file: "+targetFile);
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			SVNRevision pegRevision = SVNRevision.HEAD;
			SVNRevision revision = SVNRevision.HEAD;
			SVNDepth depth = SVNDepth.INFINITY;
			updateClient.doCheckout(repositoryURL, targetFolder, pegRevision, revision, depth, true);
			File file = new File(targetFolder, targetFile);
			final boolean success = file.isFile() && file.canRead() && file.canWrite();
			logger.info("Finished checkout for file: "+targetFile);
			return success;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean commit(String message, String target) throws IOException {
		checkConnection();
		logger.info("Start commit for target: "+target);
		SVNCommitClient commitClient = ourClientManager.getCommitClient();
		try {
			SVNCommitInfo info = commitClient.doCommit(new File[]{ new File(targetFolder, target) }, false, message, null, null, false, false, SVNDepth.INFINITY);
			SVNErrorMessage errorMessage = info.getErrorMessage();
			if (errorMessage != null) {
				throw new IOException(errorMessage.getMessage(), errorMessage.getCause());
			}
			logger.info("Finished commit for target: "+target);
			return true;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean update(String target) throws IOException {
		checkConnection();
		logger.info("Start update for target: "+target);
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
		try {
			File path = new File(targetFolder, target).getAbsoluteFile();
			updateClient.doUpdate(path, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
			logger.info("Finished update for target: "+target);
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

	public File getTargetFolder() {
		return targetFolder;
	}

	private static final class SvnLogger extends SVNDebugLogAdapter {

		private static org.apache.log4j.Level getLog4JLevel(Level level) {
			if (Level.SEVERE.equals(level)) {
				return org.apache.log4j.Level.ERROR;
			}
			else if (Level.INFO.equals(level)) {
				return org.apache.log4j.Level.INFO;
			}
			else if (Level.WARNING.equals(level)) {
				return org.apache.log4j.Level.WARN;
			}
			else if (Level.FINEST.equals(level) || Level.ALL.equals(level)) {
				return org.apache.log4j.Level.TRACE;
			}
			else if (Level.FINER.equals(level) || Level.FINE.equals(level)) {
				return org.apache.log4j.Level.DEBUG;
			}
			return org.apache.log4j.Level.INFO; // Default if nothing matches
		}
		
		@Override
		public void log(SVNLogType logType, Throwable th, Level logLevel) {
			org.apache.log4j.Level level = getLog4JLevel(logLevel);
			if (logger.isEnabledFor(level)) {
				logger.log(level, "SVN Exception: "+logType.getName(), th);
			}
		}

		@Override
		public void log(SVNLogType logType, String message, Level logLevel) {
			org.apache.log4j.Level level = getLog4JLevel(logLevel);
			if (logger.isEnabledFor(level)) {
				logger.log(level, message);
			}
		}

		@Override
		public void log(SVNLogType logType, String message, byte[] data) {
			if (logger.isTraceEnabled()) {
				try {
	                logger.trace(message + "\n" + new String(data, "UTF-8"));
	            } catch (UnsupportedEncodingException e) {
	                logger.trace(message + "\n" + new String(data));
	            }
			}
		}
	
	}
	
}
