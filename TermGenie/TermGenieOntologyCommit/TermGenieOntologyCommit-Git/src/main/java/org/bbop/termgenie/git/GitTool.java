package org.bbop.termgenie.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.RejectCommitException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class GitTool implements VersionControlAdapter {
	
	private static final Logger LOGGER = Logger.getLogger(GitTool.class);

	public static GitTool createAnonymousGit(File targetFolder, String repositoryURL) {
		return new GitTool(repositoryURL, targetFolder, null, null);
	}
	
	public static GitTool createUsernamePasswordGit(File targetFolder, String repositoryURL, String username, String password) {
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
		return new GitTool(repositoryURL, targetFolder, credentialsProvider, null);
	}
	
	public static GitTool createSSHKeyGit(File targetFolder, String repositoryURL, File sshKeyFile, String passphrase) {
		TransportConfigCallback sshCallback = createSshCallBack(sshKeyFile, passphrase);
		return new GitTool(repositoryURL, targetFolder, null, sshCallback);
	}
	
	private static TransportConfigCallback createSshCallBack(final File sshKeyFile, final String passphrase) {
		final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure( Host host, Session session ) {
				// do nothing
			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				JSch defaultJSch = super.createDefaultJSch(fs);
				defaultJSch.addIdentity(sshKeyFile.getAbsolutePath(), passphrase);
				return super.createDefaultJSch(fs);
			}
		};
		return new TransportConfigCallback() {

			@Override
			public void configure(Transport transport) {
				if (transport instanceof SshTransport) {
					SshTransport sshTransport = (SshTransport) transport;
					sshTransport.setSshSessionFactory(sshSessionFactory);
				}
				
			}
			
		};
		
	}

	private final String repositoryURL;
	private final File targetFolder;
	private final CredentialsProvider credentialsProvider;
	private final TransportConfigCallback sshCallback;
	
	private Git gitInstance = null;
	
	
	
	GitTool(String repositoryURL, File targetFolder, CredentialsProvider credentialsProvider, TransportConfigCallback sshCallback) {
		super();
		this.repositoryURL = repositoryURL;
		this.targetFolder = targetFolder;
		this.credentialsProvider = credentialsProvider;
		this.sshCallback = sshCallback;
	}

	public File getTargetFolder() {
		return targetFolder;
	}
	
	@Override
	public void connect() throws IOException {
		// do nothing
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

	private void setCredentials(TransportCommand<?, ?> c) {
		if (credentialsProvider != null) {
			c.setCredentialsProvider(credentialsProvider);
		}
		else if (sshCallback != null) {
			c.setTransportConfigCallback(sshCallback);
		}
	}
	
	@Override
	public boolean checkout(List<String> targetFiles, ProcessState state) throws IOException {
		try {
			// git clone
			CloneCommand clone = Git.cloneRepository()
			        .setURI(repositoryURL)
			        .setDirectory(targetFolder)
			        .setProgressMonitor(new GitProgressMonitor(state));
			setCredentials(clone);
			gitInstance = clone.call();
			Repository repository = gitInstance.getRepository();
			final File workTree = repository.getWorkTree();
			for (String targetFile : targetFiles) {
				File file = new File(workTree, targetFile);
				if (file.exists() == false) {
					return false;
				}
			}
			return true;
		} catch (InvalidRemoteException exception) {
			throw new IOException(exception);
		} catch (TransportException exception) {
			throw new IOException(exception);
		} catch (GitAPIException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean commit(String message, List<String> targetFiles, String user, String userEmail, ProcessState state)
			throws IOException
	{
		// git commit
		CommitCommand commit = gitInstance.commit().setMessage(message).setAuthor(user, userEmail);
		for (String targetFile : targetFiles) {
			commit = commit.setOnly(targetFile);
		}
		try {
			commit.call();
		} catch (NoHeadException exception) {
			throw new IOException(exception);
		} catch (NoMessageException exception) {
			throw new IOException(exception);
		} catch (UnmergedPathsException exception) {
			throw new IOException(exception);
		} catch (ConcurrentRefUpdateException exception) {
			throw new IOException(exception);
		} catch (WrongRepositoryStateException exception) {
			throw new IOException(exception);
		} catch (RejectCommitException exception) {
			throw new IOException(exception);
		} catch (GitAPIException exception) {
			throw new IOException(exception);
		}
		
		// git push
		PushCommand push = gitInstance.push()
				.setProgressMonitor(new GitProgressMonitor(state))
				.setCredentialsProvider(credentialsProvider);
		setCredentials(push);
		try {
			push.call();
			return true;
		} catch (InvalidRemoteException exception) {
			throw new IOException(exception);
		} catch (TransportException exception) {
			throw new IOException(exception);
		} catch (GitAPIException exception) {
			throw new IOException(exception);
		}
	}

	@Override
	public boolean update(List<String> targetFiles, ProcessState state) throws IOException {
		// git pull
		PullCommand pull = gitInstance.pull().setProgressMonitor(new GitProgressMonitor(state));
		setCredentials(pull);
		try {
			PullResult pullResult = pull.call();
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
		
		private final ProcessState state;
		
		private String currentTask = null;

		GitProgressMonitor(ProcessState state) {
			this.state = state;
		}

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
			currentTask = title;
			if (ProcessState.NO != state) {
				ProcessState.addMessage(state, "Git task start: "+title);
			}
			LOGGER.info("Git task start: "+title);
		}

		@Override
		public void endTask() {
			if (currentTask != null) {
				if (ProcessState.NO != state) {
					ProcessState.addMessage(state, "Git task end: "+currentTask);
				}
				LOGGER.info("Git task end: "+currentTask);
			}
		}
	}

}
