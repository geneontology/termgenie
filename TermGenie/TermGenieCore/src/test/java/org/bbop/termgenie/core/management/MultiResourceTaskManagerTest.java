package org.bbop.termgenie.core.management;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.junit.Test;

public class MultiResourceTaskManagerTest {

	@Test
	public void testRunManagedTask() throws Exception {
		String name = "TestMultiResourceTaskManager";
		TestGenericTaskManager[] managers = new TestGenericTaskManager[4];
		for (char c = 'A'; c <= 'D'; c++) {
			String s = Character.toString(c);
			managers[c - 'A'] = new TestGenericTaskManager(s, s);
		}
		
		MultiResourceTaskManager<String, String> testManager = new MultiResourceTaskManager<String, String>(name, managers){

			@Override
			protected String[] getAdditionalInformations(GenericTaskManager<String>... managers) {
				String[] infos = new String[managers.length];
				for (int i = 0; i < managers.length; i++) {
					infos[i] = ((TestGenericTaskManager)managers[i]).identifier;
				}
				return infos;
			}

			@Override
			protected boolean matchRequested(String i1, String i2) {
				return i1.equals(i2);
			}
			
		};
		
		List<String> events = Collections.synchronizedList(new ArrayList<String>());
		List<Thread> threads = new ArrayList<Thread>();
		threads.add(new TestThread(testManager, events, "A","B"));
		threads.add(new TestThread(testManager, events, "C","D"));
		threads.add(new TestThread(testManager, events, "B","C"));
		threads.add(new TestThread(testManager, events, "A","B"));
		
		for (Thread thread : threads) {
			thread.start();
			Thread.sleep(10);
		}
		for (Thread thread : threads) {
			thread.join();
		}
		assertEquals(4, events.size());
		
		// first two task in parallel
		assertEquals("[A, B]100", events.get(0));
		assertEquals("[C, D]100", events.get(1));
		
		// wait for both to finish
		assertEquals("[B, C]590", events.get(2));
		
		// wait for previous to finish
		assertEquals("[A, B]1080", events.get(3));
	}
	
	private static class TestThread extends Thread {

		final MultiResourceTaskManager<String, String> testManager;
		final TestTask task;
		final String[] requested;
		long startTime;
		
		/**
		 * @param testManager
		 * @param task
		 * @param requested
		 */
		protected TestThread(MultiResourceTaskManager<String, String> testManager, List<String> events,
				String...requested) {
			super();
			this.testManager = testManager;
			this.task = new TestTask(events);
			this.requested = requested;
		}

		@Override
		public void run() {
			startTime = System.currentTimeMillis();
			testManager.runManagedTask(task, requested);
		}
		
		private class TestTask implements MultiResourceManagedTask<String, String> {
			
			private final List<String> events;

			TestTask(List<String> events) {
				super();
				this.events = events;
			}

			@Override
			public List<Boolean> run(List<String> requested) {
				try {
					Thread.sleep(100);
					long current = System.currentTimeMillis() - startTime;
					// round to 10
					current = (current / 10L) * 10L;
					
					events.add(Arrays.toString(requested.toArray())+Long.toString(current));
					Thread.sleep(400);
					return null;
				} catch (InterruptedException exception) {
					throw new RuntimeException(exception);
				}
			}
			
		}
	}

	private static class TestGenericTaskManager extends GenericTaskManager<String> {

		private final String identifier;

		public TestGenericTaskManager(String name, String identifier) {
			super(name);
			this.identifier = identifier;
		}

		@Override
		protected String createManaged() {
			return name;
		}

		@Override
		protected String updateManaged(String managed) {
			return createManaged();
		}

		@Override
		protected String resetManaged(String managed) {
			return createManaged();
		}
		
		@Override
		protected void setChanged() {
			// Do nothing in tests
		}
	}
}
