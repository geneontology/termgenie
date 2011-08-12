package org.bbop.termgenie.tools;

public class Pair<A, B> {

	private final A one;
	private final B two;

	/**
	 * @param one
	 * @param two
	 */
	public Pair(A one, B two) {
		super();
		this.one = one;
		this.two = two;
	}

	/**
	 * @return the one
	 */
	public A getOne() {
		return one;
	}

	/**
	 * @return the two
	 */
	public B getTwo() {
		return two;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pair [");
		if (one != null) {
			builder.append("one=");
			builder.append(one);
			builder.append(", ");
		}
		if (two != null) {
			builder.append("two=");
			builder.append(two);
		}
		builder.append("]");
		return builder.toString();
	}
}
