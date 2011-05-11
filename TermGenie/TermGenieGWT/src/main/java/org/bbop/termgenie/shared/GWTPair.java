package org.bbop.termgenie.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTPair<A extends IsSerializable, B extends IsSerializable> implements IsSerializable{

	private A one;
	private B two;
	
	private GWTPair() {
	}

	/**
	 * @param one
	 * @param two
	 */
	public GWTPair(A one, B two) {
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
