package org.bbop.termgenie.services.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class JsonSystemDetails {

	private Map<String, String> environment = null;
	private String currentHeap;
	private String maxHeap;
	private String freeHeap;

	/**
	 * @return the currentHeap
	 */
	public String getCurrentHeap() {
		return currentHeap;
	}

	/**
	 * @param currentHeap the currentHeap to set
	 */
	public void setCurrentHeap(String currentHeap) {
		this.currentHeap = currentHeap;
	}

	/**
	 * @return the maxHeap
	 */
	public String getMaxHeap() {
		return maxHeap;
	}

	/**
	 * @param maxHeap the maxHeap to set
	 */
	public void setMaxHeap(String maxHeap) {
		this.maxHeap = maxHeap;
	}

	/**
	 * @return the freeHeap
	 */
	public String getFreeHeap() {
		return freeHeap;
	}

	/**
	 * @param freeHeap the freeHeap to set
	 */
	public void setFreeHeap(String freeHeap) {
		this.freeHeap = freeHeap;
	}

	/**
	 * @return the environment
	 */
	public Map<String, String> getEnvironment() {
		return environment;
	}

	/**
	 * @param environment the environment to set
	 */
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
	
	/**
	 * @param properties the environment to set
	 * @param prefixes list of prefixes, used to filter the properties
	 */
	public void setEnvironment(Properties properties, String...prefixes) {
		this.environment = new HashMap<String, String>();
		for(Entry<Object, Object> entry : properties.entrySet()) {
			final String key = entry.getKey().toString();
			for (int i = 0; i < prefixes.length; i++) {
				if (key.startsWith(prefixes[i])) {
					environment.put(key, entry.getValue().toString());
				}
			}
		}
	}

}
