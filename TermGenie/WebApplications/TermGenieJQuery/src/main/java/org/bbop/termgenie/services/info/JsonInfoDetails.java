package org.bbop.termgenie.services.info;

public class JsonInfoDetails {

	private String owlapiVersion;
	private String termgenieVersion;
	private String reasonerName;
	private String reasonerVersion;

	/**
	 * @return the owlapiVersion
	 */
	public String getOwlapiVersion() {
		return owlapiVersion;
	}

	/**
	 * @param owlapiVersion the owlapiVersion to set
	 */
	public void setOwlapiVersion(String owlapiVersion) {
		this.owlapiVersion = owlapiVersion;
	}

	/**
	 * @return the termgenieVersion
	 */
	public String getTermgenieVersion() {
		return termgenieVersion;
	}

	/**
	 * @param termgenieVersion the termgenieVersion to set
	 */
	public void setTermgenieVersion(String termgenieVersion) {
		this.termgenieVersion = termgenieVersion;
	}

	/**
	 * @return the reasonerName
	 */
	public String getReasonerName() {
		return reasonerName;
	}

	/**
	 * @param reasonerName the reasonerName to set
	 */
	public void setReasonerName(String reasonerName) {
		this.reasonerName = reasonerName;
	}

	/**
	 * @return the reasonerVersion
	 */
	public String getReasonerVersion() {
		return reasonerVersion;
	}

	/**
	 * @param reasonerVersion the reasonerVersion to set
	 */
	public void setReasonerVersion(String reasonerVersion) {
		this.reasonerVersion = reasonerVersion;
	}
}
