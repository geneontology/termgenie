package org.bbop.termgenie.ontology;

/**
 * Information about a single entry for a commit.
 * 
 * @param <T> the type of the entry to be committed.
 */
public class CommitObject<T> {

	private final T object;
	private final CommitObject.Modification type;

	/**
	 * Possible states for a change in a commit.
	 */
	public enum Modification {
		add, modify, remove
	}

	/**
	 * Mark a new object of type <T> as added for the commit.
	 * 
	 * @param <T> type
	 * @param object the object to be committed
	 * @return commit object
	 */
	public static <T> CommitObject<T> add(T object) {
		return new CommitObject<T>(object, Modification.add);
	}

	/**
	 * Mark a new object of type <T> as modified for the commit.
	 * 
	 * @param <T> type
	 * @param object the object to be committed
	 * @return commit object
	 */
	public static <T> CommitObject<T> modify(T object) {
		return new CommitObject<T>(object, Modification.modify);
	}

	/**
	 * Mark a new object of type <T> as to be deleted for the commit.
	 * 
	 * @param <T> type
	 * @param object the object to be committed
	 * @return commit object
	 */
	public static <T> CommitObject<T> del(T object) {
		return new CommitObject<T>(object, Modification.remove);
	}

	/**
	 * Constructor only visible in package. Use static methods to create the
	 * instances.
	 * 
	 * @param object
	 * @param type
	 */
	public CommitObject(T object, CommitObject.Modification type) {
		super();
		this.object = object;
		this.type = type;
	}

	/**
	 * @return the object
	 */
	public T getObject() {
		return object;
	}

	/**
	 * @return the type
	 */
	public CommitObject.Modification getType() {
		return type;
	}
}
