package org.bbop.termgenie.rules;

/**
 * Methods for tracking changes of an object, i.e. an ontology.
 */
public interface ChangeTracker {

	/**
	 * @return true, if the object has been changed or the changes could not be
	 *         rolled back.
	 */
	public boolean hasChanges();

}
