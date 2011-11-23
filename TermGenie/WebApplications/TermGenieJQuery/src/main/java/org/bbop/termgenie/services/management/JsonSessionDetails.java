package org.bbop.termgenie.services.management;


public class JsonSessionDetails {

	private long sessionsCreated = 0;
	private long sessionsDestroyed = 0;
	private int activeSessions = 0;
	
	/**
	 * @return the sessionsCreated
	 */
	public long getSessionsCreated() {
		return sessionsCreated;
	}
	
	/**
	 * @param sessionsCreated the sessionsCreated to set
	 */
	public void setSessionsCreated(long sessionsCreated) {
		this.sessionsCreated = sessionsCreated;
	}
	
	/**
	 * @return the sessionsDestroyed
	 */
	public long getSessionsDestroyed() {
		return sessionsDestroyed;
	}
	
	/**
	 * @param sessionsDestroyed the sessionsDestroyed to set
	 */
	public void setSessionsDestroyed(long sessionsDestroyed) {
		this.sessionsDestroyed = sessionsDestroyed;
	}
	
	/**
	 * @return the activeSessions
	 */
	public int getActiveSessions() {
		return activeSessions;
	}
	
	/**
	 * @param activeSessions the activeSessions to set
	 */
	public void setActiveSessions(int activeSessions) {
		this.activeSessions = activeSessions;
	}
}
