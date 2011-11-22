package org.bbop.termgenie.user;


public class UserDataImpl implements UserData {

	private String screenname;
	private String guid;
	private String email;
	private String xref;
	private String scmAlias;
	
	public UserDataImpl() {
		super();
		this.screenname = null;
		this.guid = null;
		this.email = null;
		this.xref = null;
		this.scmAlias = null;
	}

	/**
	 * @param screenname
	 * @param guid
	 * @param email
	 * @param xref
	 * @param scmAlias
	 */
	public UserDataImpl(String screenname, String guid, String email, String xref, String scmAlias) {
		super();
		this.screenname = screenname;
		this.guid = guid;
		this.email = email;
		this.xref = xref;
		this.scmAlias = scmAlias;
	}

	/**
	 * @return the screenname
	 */
	@Override
	public String getScreenname() {
		return screenname;
	}

	/**
	 * @param screenname the screenname to set
	 */
	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	/**
	 * @return the guid
	 */
	@Override
	public String getGuid() {
		return guid;
	}

	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @return the email
	 */
	@Override
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the xref
	 */
	@Override
	public String getXref() {
		return xref;
	}

	/**
	 * @param xref the xref to set
	 */
	public void setXref(String xref) {
		this.xref = xref;
	}

	/**
	 * @return the scmAlias
	 */
	@Override
	public String getScmAlias() {
		return scmAlias;
	}

	/**
	 * @param scmAlias the scmAlias to set
	 */
	public void setScmAlias(String scmAlias) {
		this.scmAlias = scmAlias;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserDataImpl [screenname=");
		builder.append(screenname);
		builder.append(", guid=");
		builder.append(guid);
		builder.append(", email=");
		builder.append(email);
		builder.append(", xref=");
		builder.append(xref);
		builder.append(", scmAlias=");
		builder.append(scmAlias);
		builder.append("]");
		return builder.toString();
	}
}
