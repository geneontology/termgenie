package org.bbop.termgenie.services.freeform;


public class JsonFreeFormConfig {

	boolean enabled;
	boolean doAsciiCheck;
	String[] oboNamespaces;
	String[] additionalRelations;
	
	
	public JsonFreeFormConfig() {
		this(false);
	}

	/**
	 * @param enabled
	 */
	JsonFreeFormConfig(boolean enabled) {
		this(enabled, false, null, null);
	}
	
	/**
	 * @param enabled
	 * @param doAsciiCheck
	 * @param oboNamespaces
	 * @param additionalRelations
	 */
	JsonFreeFormConfig(boolean enabled, boolean doAsciiCheck, String[] oboNamespaces, String[] additionalRelations) {
		super();
		this.enabled = enabled;
		this.doAsciiCheck = doAsciiCheck;
		this.oboNamespaces = oboNamespaces;
		this.additionalRelations = additionalRelations;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * @return the oboNamespaces
	 */
	public String[] getOboNamespaces() {
		return oboNamespaces;
	}
	
	/**
	 * @param oboNamespaces the oboNamespaces to set
	 */
	public void setOboNamespaces(String[] oboNamespaces) {
		this.oboNamespaces = oboNamespaces;
	}
	
	/**
	 * @return the additionalRelations
	 */
	public String[] getAdditionalRelations() {
		return additionalRelations;
	}
	
	/**
	 * @param additionalRelations the additionalRelations to set
	 */
	public void setAdditionalRelations(String[] additionalRelations) {
		this.additionalRelations = additionalRelations;
	}

	/**
	 * @return the doAsciiCheck
	 */
	public boolean isDoAsciiCheck() {
		return doAsciiCheck;
	}

	/**
	 * @param doAsciiCheck the doAsciiCheck to set
	 */
	public void setDoAsciiCheck(boolean doAsciiCheck) {
		this.doAsciiCheck = doAsciiCheck;
	}
	
}
