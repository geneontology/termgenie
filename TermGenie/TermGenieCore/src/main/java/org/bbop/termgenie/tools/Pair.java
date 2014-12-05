package org.bbop.termgenie.tools;

public class Pair<A, B> {

	private A one;
	private B two;

	/**
	 * @param one
	 * @param two
	 */
	public Pair(A one, B two) {
		super();
		this.one = one;
		this.two = two;
	}

	public static <A,B> Pair<A,B> of(A a, B b) {
		return new Pair<A, B>(a, b);
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
	 * @param one the one to set
	 */
	public void setOne(A one) {
		this.one = one;
	}
	
	/**
	 * @param two the two to set
	 */
	public void setTwo(B two) {
		this.two = two;
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
