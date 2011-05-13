package org.bbop.termgenie.server;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class OntologyServiceImplTest {

	@Test
	public void testMergeLists() {
		testLists(5, 5, new int[]{0,10,1,11,2,12,3,13,4,14});
		testLists(4, 5, new int[]{0,10,1,11,2,12,3,13,14});
		testLists(5, 4, new int[]{0,10,1,11,2,12,3,13,4});
		testLists(0, 0, new int[0]);
		testLists(0, 1, new int[]{10});
		testLists(1, 0, new int[]{0});
		testLists(1, 1, new int[]{0,10});
		testLists(1, 10, new int[]{0,10,11,12,13,14,15,16,17,18,19});
		testLists(10, 1, new int[]{0,10,1,2,3,4,5,6,7,8,9});
	}

	protected void testLists(int l1, int l2, int[] expected) {
		List<Integer> target = createList(l1);
		List<Integer> insert = createListC(l2);
		OntologyServiceImpl.mergeLists(target, insert);

		int[] actuals = new int[target.size()];
		for (int i = 0; i < actuals.length; i++) {
			actuals[i] = target.get(i);
		}
		assertArrayEquals(expected, actuals);
	}
	
	private List<Integer> createList(int length) {
		List<Integer> list = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++) {
			list.add(new Integer(i));
		}
		return list;
	}

	private List<Integer> createListC(int length) {
		List<Integer> list = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++) {
			list.add(new Integer(10+i));
		}
		return list;
	}
}
