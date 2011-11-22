package org.bbop.termgenie.services.management;

import java.util.List;
import java.util.Map;


public class JsonModuleConfigDetails {

	private String moduleName;
	private String description;
	
	private Map<String, String> implementations;
	private Map<String, String> parameters;
	private List<JsonPair> provides;
	private List<JsonPair> additionalData;
	
	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}
	
	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the implementations
	 */
	public Map<String, String> getImplementations() {
		return implementations;
	}
	
	/**
	 * @param implementations the implementations to set
	 */
	public void setImplementations(Map<String, String> implementations) {
		this.implementations = implementations;
	}
	
	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the provides
	 */
	public List<JsonPair> getProvides() {
		return provides;
	}

	/**
	 * @param provides the provides to set
	 */
	public void setProvides(List<JsonPair> provides) {
		this.provides = provides;
	}
	
	/**
	 * @return the additionalData
	 */
	public List<JsonPair> getAdditionalData() {
		return additionalData;
	}

	/**
	 * @param additionalData the additionalData to set
	 */
	public void setAdditionalData(List<JsonPair> additionalData) {
		this.additionalData = additionalData;
	}
	
	public static class JsonPair {
		
		private String one;
		private String two;
		
		public JsonPair() {
			super();
		}

		/**
		 * @param one
		 * @param two
		 */
		public JsonPair(String one, String two) {
			this();
			this.one = one;
			this.two = two;
		}

		/**
		 * @return the one
		 */
		public String getOne() {
			return one;
		}
		
		/**
		 * @param one the one to set
		 */
		public void setOne(String one) {
			this.one = one;
		}
		
		/**
		 * @return the two
		 */
		public String getTwo() {
			return two;
		}
		
		/**
		 * @param two the two to set
		 */
		public void setTwo(String two) {
			this.two = two;
		}
	}
}
