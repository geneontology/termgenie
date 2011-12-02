package org.bbop.termgenie.cvs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.AbstractConnection;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.ConnectionModifier;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;

/**
 * Implements a connection to a cvs server via ssh. 
 */
public class SshExtServerConnection extends AbstractConnection {

    /**
     * The default port number to use.
     */
    public static final int DEFAULT_PORT = 22;

    private final String userName;
    private final String password;
    private final int port;
    private final String hostName;
    private Connection sshConnection = null;
	private Session session = null;

    /**
     * Setup the connection propeties with the CVSRoot object and password
     * 
     * @param cvsRoot the {@link CVSRoot}
     * @throws IllegalArgumentException if the cvsRoot does not represent ext connection type.
     */
    public SshExtServerConnection(CVSRoot cvsRoot) {
        if (!CVSRoot.METHOD_EXT.equals(cvsRoot.getMethod())) {
            throw new IllegalArgumentException("CVS Root '" + cvsRoot
                    + "' does not represent :ext: connection type.");
        }
        userName = cvsRoot.getUserName();
        password = cvsRoot.getPassword();
        hostName = cvsRoot.getHostName();
        if (hostName == null) {
			throw new IllegalArgumentException("hostName is null");
		}
        setRepository(cvsRoot.getRepository());
        int port = cvsRoot.getPort();
        if (port <= 0) {
            port = DEFAULT_PORT;
        }
        this.port = port;
    }

    /**
     * Authenticate a connection with the server.
     *
     * @throws AuthenticationException if an error occurred
     */
    private void openConnection() throws AuthenticationException {
        try {
        	sshConnection = new Connection(hostName, port);
        	sshConnection.connect();
        	sshConnection.authenticateWithPassword(userName, password);
        	session = sshConnection.openSession();
        	session.execCommand("cvs server");

            BufferedOutputStream bos = new BufferedOutputStream(session.getStdin(), 32768);
            LoggedDataOutputStream outputStream = new LoggedDataOutputStream(bos);
            setOutputStream(outputStream);

            BufferedInputStream bis = new BufferedInputStream(session.getStdout(), 32768);
            LoggedDataInputStream inputStream = new LoggedDataInputStream(bis);
            setInputStream(inputStream);
        } catch (IOException e) {
            reset();
            String message = "IOException host: "+hostName;
            throw new AuthenticationException(message, e, message);
        }
    }

    private void reset() {
        sshConnection = null;
        session = null;
        setInputStream(null);
        setOutputStream(null);
    }

    @Override
	public synchronized void verify() throws AuthenticationException {
        openConnection();
        if (sshConnection == null) {
            return;
        }
        sshConnection.close();
        reset();
    }

    @Override
	public synchronized void open() throws AuthenticationException {
        openConnection();
    }

    @Override
	public int getPort() {
        return port;
    }

    @Override
	public synchronized void close() throws IOException {
        if (!isOpen()) {
            return;
        }

        try {
            sshConnection.close();
        } finally {
            reset();
        }
    }

    @Override
	public void modifyInputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    @Override
	public void modifyOutputStream(ConnectionModifier modifier)
            throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }

    @Override
	public synchronized boolean isOpen() {
        return sshConnection != null && session != null;
    }

}