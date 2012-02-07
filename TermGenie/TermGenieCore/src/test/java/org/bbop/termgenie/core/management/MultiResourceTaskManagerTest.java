package org.bbop.termgenie.core.management;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask;
import org.bbop.termgenie.core.management.GenericTaskManager.ManagedTask.Modified;
import org.bbop.termgenie.core.management.MultiResourceTaskManager.MultiResourceManagedTask;
import org.junit.Test;

public class MultiResourceTaskManagerTest {

	@Test
	public void testRunManagedTask() throws Exception {
		
		TestGenericTaskManager[] managers = createManagerList();
		MultiResourceTaskManager<String, String> testManager = createMultiTaskManager(managers);

		List<String> events = Collections.synchronizedList(new ArrayList<String>());
		List<Thread> threads = new ArrayList<Thread>();
		threads.add(new TestMultiThread(testManager, events, "A", "B"));
		threads.add(new TestMultiThread(testManager, events, "C", "D"));
		threads.add(new TestMultiThread(testManager, events, "B", "C"));
		threads.add(new TestMultiThread(testManager, events, "A", "B"));

		for (Thread thread : threads) {
			thread.start();
			Thread.sleep(10);
		}
		for (Thread thread : threads) {
			thread.join();
		}
		assertEquals(4, events.size());

		// first two tasks in parallel
		assertEquals("[A, B]100", events.get(0));
		assertEquals("[C, D]100", events.get(1));

		// wait for both to finish
		assertEquals("[B, C]590", events.get(2));

		// wait for previous to finish
		assertEquals("[A, B]1080", events.get(3));
	}

	@Test
	public void testMixedRunManagedTask() throws Exception {
		
		TestGenericTaskManager[] managers = createManagerList();
		MultiResourceTaskManager<String, String> testManager = createMultiTaskManager(managers);

		List<String> events = Collections.synchronizedList(new ArrayList<String>());
		List<Thread> threads = new ArrayList<Thread>();
		threads.add(new TestMultiThread(testManager, events, "A", "B"));
		threads.add(new TestSingleThread(managers[0], events)); // A
		threads.add(new TestMultiThread(testManager, events, "C", "D"));
		threads.add(new TestSingleThread(managers[0], events)); // A
		threads.add(new TestMultiThread(testManager, events, "B", "C"));
		threads.add(new TestSingleThread(managers[1], events)); // B
		threads.add(new TestMultiThread(testManager, events, "A", "B"));

		for (Thread thread : threads) {
			thread.start();
			Thread.sleep(10);
		}
		for (Thread thread : threads) {
			thread.join();
		}
		assertEquals(7, events.size());

		// first two tasks in parallel
		assertEquals("[A, B]100", events.get(0));
		assertEquals("[C, D]100", events.get(1));
		
		// wait for both to finish
		assertEquals("A-590", events.get(2));
		assertEquals("[B, C]580", events.get(3));

		// two single jobs in parallel
		assertEquals("A-1070", events.get(4));
		assertEquals("B-1070", events.get(5));
		
		// wait for previous to finish
		assertEquals("[A, B]1560", events.get(6));
	}
	
	private MultiResourceTaskManager<String, String> createMultiTaskManager(TestGenericTaskManager[] managers)
	{
		String name = "TestMultiResourceTaskManager";
		MultiResourceTaskManager<String, String> testManager = new MultiResourceTaskManager<String, String>(name, managers)
		{

			@Override
			protected String[] getAdditionalInformations(GenericTaskManager<String>...managers) {
				String[] infos = new String[managers.length];
				for (int i = 0; i < managers.length; i++) {
					infos[i] = ((TestGenericTaskManager) managers[i]).identifier;
				}
				return infos;
			}

			@Override
			protected boolean matchRequested(String i1, String i2) {
				return i1.equals(i2);
			}

		};
		return testManager;
	}

	private TestGenericTaskManager[] createManagerList() {
		TestGenericTaskManager[] managers = new TestGenericTaskManager[4];
		for (char c = 'A'; c <= 'D'; c++) {
			String s = Character.toString(c);
			managers[c - 'A'] = new TestGenericTaskManager(s, s);
		}
		return managers;
	}

	private static class TestMultiThread extends Thread {

		final MultiResourceTaskManager<String, String> testManager;
		final TestTask task;
		final String[] requested;
		long startTime;

		/**
		 * @param testManager
		 * @param task
		 * @param requested
		 */
		protected TestMultiThread(MultiResourceTaskManager<String, String> testManager,
				List<String> events,
				String...requested)
		{
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
			public List<Modified> run(List<String> requested) {
				try {
					Thread.sleep(100);
					long current = System.currentTimeMillis() - startTime;
					// round to 10
					current = (current / 10L) * 10L;

					events.add(Arrays.toString(requested.toArray()) + Long.toString(current));
					Thread.sleep(400);
					return null;
				} catch (InterruptedException exception) {
					throw new RuntimeException(exception);
				}
			}

		}
	}
	
	private static class TestSingleThread extends Thread {

		final TestGenericTaskManager testManager;
		final TestTask task;
		long startTime;

		/**
		 * @param testManager
		 * @param task
		 * @param requested
		 */
		protected TestSingleThread(TestGenericTaskManager testManager,
				List<String> events)
		{
			super();
			this.testManager = testManager;
			this.task = new TestTask(events);
		}

		@Override
		public void run() {
			startTime = System.currentTimeMillis();
			testManager.runManagedTask(task);
		}

		private class TestTask implements ManagedTask<String> {

			private final List<String> events;

			TestTask(List<String> events) {
				super();
				this.events = events;
			}

			@Override
			public Modified run(String requested) {
				try {
					Thread.sleep(100);
					long current = System.currentTimeMillis() - startTime;
					// round to 10
					current = (current / 10L) * 10L;

					events.add(requested + "-" + Long.toString(current));
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
		private String currentName;

		public TestGenericTaskManager(String name, String identifier) {
			super(name);
			this.identifier = identifier;
			currentName = name;
		}

		@Override
		protected String createManaged() {
			return currentName;
		}

		@Override
		protected String updateManaged(String managed) {
			currentName += "-update";
			return currentName;
		}

		@Override
		protected String resetManaged(String managed) {
			return currentName;
		}

		@Override
		protected void setChanged(boolean reset) {
			// Do nothing in tests
		}
	}
}
