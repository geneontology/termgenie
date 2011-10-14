package org.bbop.termgenie.data;

import java.util.Arrays;

/**
 * Result after an export request.
 */
public class JsonExportResult extends JsonResult {

	private String[] formats;
	private String[] contents;

	public JsonExportResult() {
		super();
	}

	/**
	 * @return the formats
	 */
	public String[] getFormats() {
		return formats;
	}

	/**
	 * @param formats the formats to set
	 */
	public void setFormats(String[] formats) {
		this.formats = formats;
	}

	/**
	 * @return the contents
	 */
	public String[] getContents() {
		return contents;
	}

	/**
	 * @param contents the contents to set
	 */
	public void setContents(String[] contents) {
		this.contents = contents;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JsonExportResult [success=");
		builder.append(success);
		builder.append(", ");
		if (message != null) {
			builder.append("message=");
			builder.append(message);
			builder.append(", ");
		}
		if (formats != null) {
			builder.append("formats=");
			builder.append(Arrays.toString(formats));
			builder.append(", ");
		}
		if (contents != null) {
			builder.append("contents=");
			builder.append(Arrays.toString(contents));
		}
		builder.append("]");
		return builder.toString();
	}
}