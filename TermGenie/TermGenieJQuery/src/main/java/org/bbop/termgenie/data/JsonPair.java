package org.bbop.termgenie.data;


public class JsonPair<A, B> {

	private A one;
	private B two;
	
	private JsonPair() {
	}

	/**
	 * @param one
	 * @param two
	 */
	public JsonPair(A one, B two) {
		this();
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

	/**
	 * @param one the one to set
	 */
	void setOne(A one) {
		this.one = one;
	}

	/**
	 * @param two the two to set
	 */
	void setTwo(B two) {
		this.two = two;
	}
}
