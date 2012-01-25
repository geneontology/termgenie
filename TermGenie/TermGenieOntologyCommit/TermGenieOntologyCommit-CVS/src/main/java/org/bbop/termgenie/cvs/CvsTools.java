package org.bbop.termgenie.cvs;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.bbop.termgenie.scm.VersionControlAdapter;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.LocalConnection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

/**
 * Tools for connecting to a CVS repository. This class is not ThreadSafe, each
 * thread must have its own instance.
 */
public class CvsTools implements VersionControlAdapter {

	private static final Logger logger = Logger.getLogger(CvsTools.class);

	private final File targetFolder;
	private final CVSRoot root;
	private final GlobalOptions options;

	private final Connection connection;
	private Client client;

	

	/**
	 * Create a new instance, for the given parameters.
	 * 
	 * @param cvsRoot cvsRoot
	 * @param cvsPassword unscrambled password
	 * @param targetFolder the target folder
	 */
	public CvsTools(String cvsRoot, String cvsPassword, File targetFolder) {
		super();
		this.targetFolder = targetFolder;

		root = new MyCVSRoot(cvsRoot, cvsPassword);
		String method = root.getMethod();
		if (CVSRoot.METHOD_PSERVER.equals(method)) {
			connection = new PServerConnection(root);
		}
		else if (CVSRoot.METHOD_EXT.equals(method)) {
			connection = new SshExtServerConnection(root);
		}
		else if (CVSRoot.METHOD_LOCAL.equals(method)) {
			LocalConnection connection = new LocalConnection();
			connection.setRepository(root.getRepository());
			this.connection = connection;
		}
		else{
			throw new RuntimeException("Unsupported Method: "+method);
		}
		options = new GlobalOptions();
		client = null;
	}
	
	/**
	 * @return the targetFolder
	 */
	public File getTargetFolder() {
		return targetFolder;
	}

	private static class MyCVSRoot extends CVSRoot {

		protected MyCVSRoot(String cvsroot, String password) throws IllegalArgumentException {
			super(cvsroot);
			if (password != null) {
				setPassword(password);
			}
		}
	}

	/**
	 * Open the connection to the given CVS repository. Expects that there no
	 * open connections.
	 * 
	 * @see #close()
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is already open
	 */
	@Override
	public void connect() throws IOException {
		synchronized (this) {
			if (client != null) {
				throw new IllegalStateException("You need to close() the current connection before you can create a new one.");
			}
			try {
				connection.open();
				AdminHandler adminHandler = new StandardAdminHandler();
				client = new Client(connection, adminHandler);
				client.getEventManager().addCVSListener(new BasicListener());
				client.setLocalPath(targetFolder.getAbsolutePath());
			} catch (CommandAbortedException exception) {
				throw new IOException(exception);
			} catch (AuthenticationException exception) {
				throw new IOException(exception);
			}
		}
	}

	/**
	 * Close the connection to the CVS repository. Can be called multiple times.
	 * 
	 * @see #connect()
	 */
	@Override
	public void close() throws IOException {
		synchronized (this) {
			if (client != null) {
				client = null;
			}
			if (connection.isOpen()) {
				connection.close();
			}
		}
	}

	/**
	 * Checkout the given file from the CVS repository. 
	 * 
	 * @see #connect()
	 * @see #close()
	 * @param cvsFile
	 * @return true, if the CVS check out operation successfully finished
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is not open
	 */
	@Override
	public boolean checkout(String cvsFile) throws IOException {
		checkConnection();
		CheckoutCommand co = new CheckoutCommand();
		co.setModule(cvsFile);
		co.setRecursive(true);
		co.setBuilder(null);
		co.setPruneDirectories(true);
		try {
			boolean success = client.executeCommand(co, options);
			return success;
		} catch (CommandAbortedException exception) {
			throw new IOException(exception);
		} catch (CommandException exception) {
			throw new IOException(exception);
		} catch (AuthenticationException exception) {
			throw new IOException(exception);
		}
	}

	/**
	 * Commit the current checkout with the given message. 
	 * 
	 * @param message the commit message
	 * @return true, if the CVS commit operation successfully finished
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is not open
	 */
	@Override
	public boolean commit(String message, String cvsFile) throws IOException {
		checkConnection();
		CommitCommand commit = new CommitCommand();
		commit.setMessage(message);
		commit.setFiles(new File[]{new File(targetFolder, cvsFile)});
		try {
			return client.executeCommand(commit, options);
		} catch (CommandAbortedException exception) {
			throw new IOException(exception);
		} catch (CommandException exception) {
			throw new IOException(exception);
		} catch (AuthenticationException exception) {
			throw new IOException(exception);
		}
	}
	
	@Override
	public boolean update(String cvsFile) throws IOException {
		checkConnection();
		UpdateCommand update = new UpdateCommand();
		update.setRecursive(true);
		update.setFiles(new File[]{new File(targetFolder, cvsFile)});
		try {
			return client.executeCommand(update, options);
		} catch (CommandAbortedException exception) {
			throw new IOException(exception);
		} catch (CommandException exception) {
			throw new IOException(exception);
		} catch (AuthenticationException exception) {
			throw new IOException(exception);
		}
	}

	private void checkConnection() throws IllegalStateException {
		synchronized (this) {
			if (client == null) {
				throw new IllegalStateException("You need to call connect() before you can use this method");
			}
		}
	}

	class BasicListener extends CVSAdapter {

		/**
		 * Stores a tagged line
		 */
		private final StringBuffer taggedLine = new StringBuffer();

		/**
		 * Called when the server wants to send a message to be displayed to the
		 * user. The message is only for information purposes and clients can
		 * choose to ignore these messages if they wish.
		 * 
		 * @param e the event
		 */
		@Override
		public void messageSent(MessageEvent e) {
			String line = e.getMessage();
			Priority priority = e.isError() ? Level.ERROR : Level.INFO;

			if (e.isTagged()) {
				String message = MessageEvent.parseTaggedMessage(taggedLine, line);
				// if we get back a non-null line, we have something
				// to output. Otherwise, there is more to come and we
				// should do nothing yet.
				if (message != null) {
					logger.log(priority, message);
				}
			}
			else {
				logger.log(priority, line);
			}
		}
	}
}
