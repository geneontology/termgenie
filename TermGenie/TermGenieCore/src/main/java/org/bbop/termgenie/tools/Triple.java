package org.bbop.termgenie.tools;

public class Triple<A, B, C> {

	private final A one;
	private final B two;
	private final C three;

	/**
	 * @param one
	 * @param two
	 * @param three
	 */
	public Triple(A one, B two, C three) {
		super();
		this.one = one;
		this.two = two;
		this.three = three;
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

	/**
	 * @return the three
	 */
	public C getThree() {
		return three;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Triple [");
		if (one != null) {
			builder.append("one=");
			builder.append(one);
			builder.append(", ");
		}
		if (two != null) {
			builder.append("two=");
			builder.append(two);
			builder.append(", ");
		}
		if (three != null) {
			builder.append("three=");
			builder.append(three);
		}
		builder.append("]");
		return builder.toString();
	}
}
