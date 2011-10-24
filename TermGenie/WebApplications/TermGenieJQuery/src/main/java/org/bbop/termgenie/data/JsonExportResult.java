package org.bbop.termgenie.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Result after an export request.
 */
public class JsonExportResult extends JsonResult {

	private Map<String, String> exports;

	public JsonExportResult() {
		super();
	}

	/**
	 * @return the exports
	 */
	public Map<String, String> getExports() {
		return exports;
	}

	/**
	 * @param exports the exports to set
	 */
	public void setExports(Map<String, String> exports) {
		this.exports = exports;
	}
	
	public synchronized void addExport(String format, String content) {
		if (exports == null) {
			exports = new HashMap<String, String>();
		}
		exports.put(format, content);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonExportResult [success=");
		builder.append(success);
		if (message != null) {
			builder.append(", ");
			builder.append("message=");
			builder.append(message);
		}
		if (exports != null) {
			builder.append(", ");
			builder.append("exports=");
			builder.append(exports);
		}
		builder.append("]");
		return builder.toString();
	}
}
