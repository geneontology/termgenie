package org.bbop.termgenie.reasoning;

public class Timer {
	
	private final long start;
	private long stop = 0;
	
	public Timer() {
		start = System.currentTimeMillis();
	}
	
	public void stop() {
		stop = System.currentTimeMillis();
	}
	
	public String getTimeString() {
		long ellapsed = stop - start;
		String ms = Long.toString(ellapsed % 1000L);
		while (ms.length() < 3) {
			ms = "0" + ms;
		}
		long s = ellapsed / 1000L;
		return Long.toString(s)+"."+ms+" s";
	}
}