package org.bbop.termgenie.core.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide information about a process.
 */
public class ProcessState {

	public static final ProcessState NO = null;
	private static final Map<String, ProcessState> REGISTRY = new HashMap<String, ProcessState>();

	private final List<ProcessStateMessage> messages = new ArrayList<ProcessStateMessage>();
	private final long startTime;
	private boolean finished = false;

	private ProcessState() {
		// provide no accessible constructor
		super();
		startTime = System.currentTimeMillis();
	}

	/**
	 * Retrieve the list of current messages, clear the internal message buffer.
	 * 
	 * @return messages
	 */
	public synchronized List<ProcessStateMessage> getMessagesAndClear() {
		// defensive copy
		final ArrayList<ProcessStateMessage> messages = new ArrayList<ProcessStateMessage>(this.messages);
		this.messages.clear();
		return messages;
	}

	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished;
	}
	
	public static void addMessage(ProcessState state, String message) {
		addMessage(state, message, null);
	}

	public static void addMessage(ProcessState state, String message, String details) {
		if (state != null && message != null && !message.isEmpty()) {
			long current = System.currentTimeMillis();
			String timeString = renderTimeString(state.startTime, current);
			synchronized (state) {
				state.messages.add(new ProcessStateMessage(timeString, message, details));
			}
		}
	}
	
	static String renderTimeString(long startTime, long currentTime) {
		long elapsed = currentTime - startTime;
		StringBuilder sb = new StringBuilder(Long.toString(elapsed/1000)).append('.');
		long ms = elapsed % 1000;
		if (ms < 100) {
			sb.append('0');
		}
		if (ms < 10) {
			sb.append('0');
		}
		sb.append(ms).append('s');
		return sb.toString();
	}

	/**
	 * Create and start a new {@link ProcessState} object for the given UUID.
	 * 
	 * @param uuid
	 * @return process state object
	 * @throws DuplicateUUIDException If this UUID is already in use
	 */
	public static ProcessState start(String uuid) throws DuplicateUUIDException {
		if (uuid == null || uuid.isEmpty()) {
			return ProcessState.NO;
		}
		ProcessState state = null;
		synchronized (REGISTRY) {
			state = REGISTRY.get(uuid);
			if (state != null) {
				throw new DuplicateUUIDException(uuid);
			}
			state = new ProcessState();
			REGISTRY.put(uuid, state);
		}
		return state;
	}

	/**
	 * Stop tracking the {@link ProcessState} for the given UUID.
	 * 
	 * @param uuid
	 */
	public static void stop(String uuid) {
		if (uuid != null && !uuid.isEmpty()) {
			synchronized (REGISTRY) {
				REGISTRY.remove(uuid);
			}
		}
	}

	/**
	 * Retrieve the {@link ProcessState} for a given UUID.
	 * 
	 * @param uuid
	 * @return {@link ProcessState} or null
	 */
	public static ProcessState getProcessState(String uuid) {
		if (uuid == null || uuid.isEmpty()) {
			return ProcessState.NO;
		}
		synchronized (REGISTRY) {
			return REGISTRY.get(uuid);
		}
	}

	/**
	 * Exception indicating a duplicate UUID in the {@link ProcessState}
	 * handling system.
	 */
	public static class DuplicateUUIDException extends Exception {

		// generated
		private static final long serialVersionUID = 5150600242203558973L;

		public DuplicateUUIDException(String uuid) {
			super("Another call is already using the UUID: " + uuid);
		}
	}
}
