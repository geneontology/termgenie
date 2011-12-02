package org.bbop.termgenie.scm;

import java.io.Closeable;
import java.io.IOException;

public interface VersionControlAdapter extends Closeable {

	/**
	 * Open the connection to the given VC repository. Expects that there are no
	 * open connections.
	 * 
	 * @see #close()
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is already open
	 */
	public void connect() throws IOException;

	/**
	 * Close the connection to the VC repository. Can be called multiple times.
	 * 
	 * @throws IOException
	 * @see #connect()
	 */
	@Override
	public void close() throws IOException;

	/**
	 * Checkout the given file from the VC repository.
	 * 
	 * @see #connect()
	 * @see #close()
	 * @param targetFile
	 * @return true, if the VC check out operation successfully finished
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is not open
	 */
	public boolean checkout(String targetFile) throws IOException;

	/**
	 * Commit the current checkout with the given message.
	 * 
	 * @param message the commit message
	 * @param targetFile the file or folder to commit
	 * @return true, if the VC commit operation successfully finished
	 * @throws IOException in case of connection problems
	 * @throws IllegalStateException in case the connection is not open
	 */
	public boolean commit(String message, String targetFile) throws IOException;

	/**
	 * Update the current working copy from the VC repository.
	 * 
	 * @param targetFile the file or folder to update
	 * @return true, if the VC update operation successfully finished
	 * @throws IOException
	 */
	public boolean update(String targetFile) throws IOException;

}
