package org.bbop.termgenie.cvs;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

public class CVSTools implements Closeable {

	private static final Logger logger = Logger.getLogger(CVSTools.class);
	
	private final File targetFolder;
	private final GlobalOptions options;

	private final PServerConnection connection;
	private Client client;

	public CVSTools(String cvsRoot, String cvsPassword, File targetFolder) {
		super();
		this.targetFolder = targetFolder;

		CVSRoot root = CVSRoot.parse(cvsRoot);
		connection = new PServerConnection(root);
		if (cvsPassword != null) {
			String password = StandardScrambler.getInstance().scramble(cvsPassword);
			connection.setEncodedPassword(password);
		}
		options = new GlobalOptions();
	}
	
	public void connect() throws IOException {
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

	@Override
	public void close() throws IOException {
		if (connection != null && connection.isOpen()) {
			connection.close();
		}
	}

	public boolean checkout(String cvsFile) throws IOException {
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

	public boolean commit(String message) throws IOException {
		CommitCommand commit = new CommitCommand();
		commit.setMessage(message);
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

	public class BasicListener extends CVSAdapter {

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
