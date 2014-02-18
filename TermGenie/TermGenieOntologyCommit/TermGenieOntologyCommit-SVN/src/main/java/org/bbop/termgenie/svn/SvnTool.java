package org.bbop.termgenie.svn;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;


public class SvnTool implements VersionControlAdapter {
	
	private static final Logger logger = Logger.getLogger(SvnTool.class);
	
	static {
		synchronized (SVNDebugLog.class) {
			if (SVNDebugLog.getDefaultLog() != SvnLogger.INSTANCE) {
				SVNDebugLog.setDefaultLog(SvnLogger.INSTANCE);
			}
		}
	}

	private final File targetFolder;
	private final SVNURL repositoryURL;
	private final ISVNAuthenticationManager authManager;
	private final boolean loadExternal;
	
	private SVNClientManager ourClientManager;

	public static SvnTool createAnonymousSVN(File targetFolder, String repositoryURL, File svnConfigDir, boolean loadExternal) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir);
		return new SvnTool(targetFolder, repositoryURL, authManager, loadExternal);
	}
	
	public static SvnTool createUsernamePasswordSVN(File targetFolder, String repositoryURL, String username, String password, File svnConfigDir, boolean loadExternal) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir, username, password);
		return new SvnTool(targetFolder, repositoryURL, authManager, loadExternal);
	}
	
	public static SvnTool createSSHKeySVN(File targetFolder, String repositoryURL, String username, File sshKeyFile, String passphrase, File svnConfigDir, boolean loadExternal) {
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnConfigDir, username, null, sshKeyFile, passphrase, false);
		return new SvnTool(targetFolder, repositoryURL, authManager, loadExternal);
	}
	
	public static File getDefaultSvnConfigDir() {
		return SVNWCUtil.getDefaultConfigurationDirectory();
	}
	
	/**
	 * @param targetFolder
	 * @param repositoryURL
	 * @param authManager
	 * @param loadExternal
	 */
	SvnTool(File targetFolder, String repositoryURL, ISVNAuthenticationManager authManager, boolean loadExternal) {
		super();
		this.targetFolder = targetFolder;
		try {
			this.repositoryURL = SVNURL.parseURIEncoded(repositoryURL);
		} catch (SVNException exception) {
			throw new RuntimeException(exception);
		}
		this.authManager = authManager;
		this.loadExternal = loadExternal;
	}
	
	/**
	 * @param targetFolder
	 * @param repositoryURL
	 * @param authManager
	 * @param loadExternal
	 */
	SvnTool(File targetFolder, SVNURL repositoryURL, ISVNAuthenticationManager authManager, boolean loadExternal) {
		super();
		this.targetFolder = targetFolder;
		this.repositoryURL = repositoryURL;
		this.authManager = authManager;
		this.loadExternal = loadExternal;
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
	public boolean checkout(List<String> targetFiles, final ProcessState state) throws IOException {
		checkConnection();
		try {
			String startMessage = "Start checkout for files: "+targetFiles+" URL: "+repositoryURL;
			ProcessState.addMessage(state, startMessage);
			logger.info(startMessage+" Folder: "+targetFolder);
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			updateClient.setIgnoreExternals(!loadExternal);
			SVNRevision pegRevision = SVNRevision.HEAD;
			SVNRevision revision = SVNRevision.HEAD;
			SVNDepth depth = SVNDepth.INFINITY;
			ISVNEventHandler handler = new ISVNEventHandler() {

				@Override
				public void checkCancelled() throws SVNCancelException {
					// do nothing
				}

				@Override
				public void handleEvent(SVNEvent event, double progress) throws SVNException {
					SVNEventAction action = event.getAction();
					if (action != null) {
						if (SVNEventAction.UPDATE_ADD.equals(action)) {
							// add
							String message = "A   " + getRelativePath(event.getFile());
							ProcessState.addMessage(state, message);
							logger.info(message);

						}
						else if (SVNEventAction.UPDATE_EXTERNAL.equals(action)) {
							// external
							String message = "External   "+ getRelativePath(event.getFile());
							ProcessState.addMessage(state, message);
							logger.info(message);
						}
					}
				}
			};
			updateClient.setEventHandler(handler);
			updateClient.doCheckout(repositoryURL, targetFolder, pegRevision, revision, depth, true);
			
			
			boolean success = true;
			for(String targetFile : targetFiles) {
				File file = new File(targetFolder, targetFile);
				success = success && file.isFile() && file.canRead() && file.canWrite();
			}
			String endMessage = "Finished checkout for files: "+targetFiles;
			ProcessState.addMessage(state, endMessage);
			logger.info(endMessage);
			return success;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}
	
	/**
	 * Simple 'svn add' for a single file. 
	 * 
	 * @param target
	 * @param state
	 * @return true if the add in the working copy was successful
	 * @throws IOException
	 */
	public boolean add(String target, ProcessState state) throws IOException {
		checkConnection();
		SVNWCClient wcClient = ourClientManager.getWCClient();
		try {
			File path = new File(targetFolder, target).getAbsoluteFile();
			wcClient.doAdd(path, false, false, false, SVNDepth.FILES, false, false);
			return true;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean commit(String message, List<String> targets, ProcessState state) throws IOException {
		checkConnection();
		logger.info("Start commit for targets: "+targets+" URL: "+repositoryURL);
		SVNCommitClient commitClient = ourClientManager.getCommitClient();
		try {
			File[] paths = new File[targets.size()];
			for (int i = 0; i < targets.size(); i++) {
				paths[i] = new File(targetFolder, targets.get(i)).getAbsoluteFile();
			}
			SVNCommitInfo info = commitClient.doCommit(paths, false, message, null, null, false, false, SVNDepth.INFINITY);
			SVNErrorMessage errorMessage = info.getErrorMessage();
			if (errorMessage != null) {
				throw new IOException(errorMessage.getMessage(), errorMessage.getCause());
			}
			logger.info("Finished commit for targets: "+targets);
			return true;
		} catch (SVNException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean update(List<String> targets, ProcessState state) throws IOException {
		checkConnection();
		logger.info("Start update for targets: "+targets+" URL: "+repositoryURL);
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
		updateClient.setIgnoreExternals(!loadExternal);
		try {
			File[] paths = new File[targets.size()];
			for (int i = 0; i < targets.size(); i++) {
				paths[i] = new File(targetFolder, targets.get(i)).getAbsoluteFile();
			}
			updateClient.doUpdate(paths, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
			logger.info("Finished update for targets: "+targets);
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
	
	private String getRelativePath(File subPath) {
		String relative = targetFolder.toURI().relativize(subPath.toURI()).getPath();
		return relative;
	}

	static final class SvnLogger extends SVNDebugLogAdapter {
		
		static final SvnLogger INSTANCE = new SvnLogger();

		/**
		 * Only allow one instance via static INSTANCE variable.
		 */
		private SvnLogger() {
			super();
		}
		
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
