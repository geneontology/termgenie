package org.bbop.termgenie.services.authenticate;

public class UserData {
	
	private String screenname;
	private String guid;
	private String error;
	
	public UserData() {
		guid = null;
		screenname = null;
		error = null;
	}
	
	public UserData(String guid, String screenname) {
		this.guid = guid;
		this.screenname = screenname;
		this.error = null;
	}

	public UserData(String error) {
		this.guid = null;
		this.screenname = null;
		this.error = error;
	}
	
	/**
	 * @return the screenname
	 */
	public String getScreenname() {
		return screenname;
	}

	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	
	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * @param screenname the screenname to set
	 */
	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}
	
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserData [screenname=");
		builder.append(screenname);
		builder.append(", guid=");
		builder.append(guid);
		builder.append(", error=");
		builder.append(error);
		builder.append("]");
		return builder.toString();
	}
}