package org.bbop.termgenie.services.info;

public class JsonInfoDetails {

	private String owlapiVersion;
	private String termgenieBuildTimestamp;
	private String termgenieRevision;
	private String termgenieRevisionUrl;
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
	 * @return the termgenieBuildTimestamp
	 */
	public String getTermgenieBuildTimestamp() {
		return termgenieBuildTimestamp;
	}

	/**
	 * @param termgenieBuildTimestamp the termgenieBuildTimestamp to set
	 */
	public void setTermgenieBuildTimestamp(String termgenieBuildTimestamp) {
		this.termgenieBuildTimestamp = termgenieBuildTimestamp;
	}

	/**
	 * @return the termgenieRevision
	 */
	public String getTermgenieRevision() {
		return termgenieRevision;
	}

	/**
	 * @param termgenieRevision the termgenieRevision to set
	 */
	public void setTermgenieRevision(String termgenieRevision) {
		this.termgenieRevision = termgenieRevision;
	}

	/**
	 * @return the termgenieRevisionUrl
	 */
	public String getTermgenieRevisionUrl() {
		return termgenieRevisionUrl;
	}

	/**
	 * @param termgenieRevisionUrl the termgenieRevisionUrl to set
	 */
	public void setTermgenieRevisionUrl(String termgenieRevisionUrl) {
		this.termgenieRevisionUrl = termgenieRevisionUrl;
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
