package org.bbop.termgenie.tools;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;


public class SimpleGitTool {
	
	private static final Logger LOGGER = Logger.getLogger(SimpleGitTool.class);

	public static SimpleGitTool createAnonymousGit(File targetFolder, String repositoryURL) {
		return new SimpleGitTool(repositoryURL, targetFolder);
	}

	private final String repositoryURL;
	private final File targetFolder;
	
	private Git gitInstance = null;


	SimpleGitTool(String repositoryURL, File targetFolder) {
		super();
		this.repositoryURL = repositoryURL;
		this.targetFolder = targetFolder;
	}

	public File getTargetFolder() {
		return targetFolder;
	}

	public File checkout(String targetFile) throws IOException {
		try {
			if (LOGGER.isInfoEnabled()) {
				String msg = "Start git clone for file: "+targetFile+" URL: "+repositoryURL;
				LOGGER.info(msg+" into folder: "+targetFolder);
			}
			// git clone
			CloneCommand clone = Git.cloneRepository()
			        .setURI(repositoryURL)
			        .setDirectory(targetFolder)
			        .setProgressMonitor(new GitProgressMonitor());
			gitInstance = clone.call();
			Repository repository = gitInstance.getRepository();
			final File workTree = repository.getWorkTree();
			File file = new File(workTree, targetFile);
			if (file.exists() == false) {
				return null;
			}
			if (LOGGER.isInfoEnabled()) {
				String msg = "Finished checkout for file: "+targetFile;
				LOGGER.info(msg);
			}
			return file;
		} catch (InvalidRemoteException exception) {
			throw new IOException(exception);
		} catch (TransportException exception) {
			throw new IOException(exception);
		} catch (GitAPIException exception) {
			throw new IOException(exception);
		}
	}

	public boolean update() throws IOException {
		LOGGER.info("Start git pull for URL: "+repositoryURL);
		// git pull
		PullCommand pull = gitInstance.pull().setProgressMonitor(new GitProgressMonitor());
		try {
			PullResult pullResult = pull.call();
			LOGGER.info("Finished git pull");
			return pullResult.isSuccessful();
		} catch (WrongRepositoryStateException exception) {
			throw new IOException(exception);
		} catch (InvalidConfigurationException exception) {
			throw new IOException(exception);
		} catch (DetachedHeadException exception) {
			throw new IOException(exception);
		} catch (InvalidRemoteException exception) {
			throw new IOException(exception);
		} catch (CanceledException exception) {
			throw new IOException(exception);
		} catch (RefNotFoundException exception) {
			throw new IOException(exception);
		} catch (NoHeadException exception) {
			throw new IOException(exception);
		} catch (TransportException exception) {
			throw new IOException(exception);
		} catch (GitAPIException exception) {
			throw new IOException(exception);
		}
	}
	
	static class GitProgressMonitor implements ProgressMonitor {
		
		private LinkedList<String> currentTasks = new LinkedList<String>();

		@Override
		public void update(int completed) {
			// ignore
		}
		
		@Override
		public void start(int totalTasks) {
			// ignore
		}
		
		@Override
		public boolean isCancelled() {
			return false;
		}
		
		@Override
		public void beginTask(String title, int totalWork) {
			currentTasks.push(title);
			String msg = "Git task start: "+title;
			LOGGER.info(msg);
		}

		@Override
		public void endTask() {
			if (currentTasks.isEmpty() == false) {
				String title = currentTasks.pop();
				String msg = "Git task end: "+title;
				LOGGER.info(msg);
			}
		}
	}

}
