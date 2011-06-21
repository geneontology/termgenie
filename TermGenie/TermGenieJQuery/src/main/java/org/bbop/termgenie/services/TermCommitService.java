package org.bbop.termgenie.services;

import java.util.Arrays;

import org.bbop.termgenie.data.JsonOntologyTerm;


public interface TermCommitService {

	/**
	 * @param username
	 * @param password
	 * @param ontology
	 * @return true, if the username and password are valid.
	 */
	public boolean isValidUser(String username, String password, String ontology);
	
	/**
	 * Prepare the terms for export. 
	 * 
	 * @param terms
	 * @param ontology
	 * @return {@link JsonExportResult}
	 */
	public JsonExportResult exportTerms(JsonOntologyTerm[] terms, String ontology);
	
	/**
	 * Commit the terms to the ontology.
	 * 
	 * @param terms
	 * @param ontology
	 * @param username
	 * @param password
	 * @return {@link JsonCommitResult}
	 */
	public JsonCommitResult commitTerms(JsonOntologyTerm[] terms, String ontology, String username, String password);
	
	
	public static class JsonCommitResult {
		private boolean success;
		private String message;
		private JsonOntologyTerm[] terms;
		
		public JsonCommitResult() {
			super();
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @param success the success to set
		 */
		public void setSuccess(boolean success) {
			this.success = success;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @param message the message to set
		 */
		public void setMessage(String message) {
			this.message = message;
		}

		/**
		 * @return the terms
		 */
		public JsonOntologyTerm[] getTerms() {
			return terms;
		}

		/**
		 * @param terms the terms to set
		 */
		public void setTerms(JsonOntologyTerm[] terms) {
			this.terms = terms;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JsonCommitResult [success=");
			builder.append(success);
			builder.append(", ");
			if (message != null) {
				builder.append("message=");
				builder.append(message);
				builder.append(", ");
			}
			if (terms != null) {
				builder.append("terms=");
				builder.append(Arrays.toString(terms));
			}
			builder.append("]");
			return builder.toString();
		}
	}
	
	
	public static class JsonExportResult {
		private boolean success;
		private String message;
		private String[] formats;
		private String[] contents;
		
		public JsonExportResult() {
			super();
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @param success the success to set
		 */
		public void setSuccess(boolean success) {
			this.success = success;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @param message the message to set
		 */
		public void setMessage(String message) {
			this.message = message;
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
}
