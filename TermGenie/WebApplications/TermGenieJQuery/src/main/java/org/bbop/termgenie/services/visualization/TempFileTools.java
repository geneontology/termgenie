package org.bbop.termgenie.services.visualization;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Tool to create unique files and delete files after a specified time-out in a given directory.
 * 
 * Intended to be used only in conjunction with the {@link TermHierarchyRenderer}.
 */
class TempFileTools {
	
	private static final Logger logger = Logger.getLogger(TempFileTools.class);

	private static int FILE_LIMIT = 50000;
	private static final Map<File, TempFileTools> instances = new HashMap<File, TempFileTools>();

	private final File targetFolder;
	private final CleanerThread cleaner;

	/**
	 * @param targetFolder
	 * @param timeUnit
	 * @param time
	 * @param useCleaner if set to false, deactivate the cleaner functionality (i.e. for test purposes) 
	 */
	private TempFileTools(final File targetFolder, TimeUnit timeUnit, long time, boolean useCleaner)
	{
		super();
		this.targetFolder = targetFolder;
		targetFolder.mkdirs();
		final long validityPeriod = timeUnit.toMillis(time);
		if (useCleaner) {
			cleaner = new CleanerThread(validityPeriod, targetFolder);
			cleaner.start();
		}
		else {
			cleaner = null;
		}
	}

	static synchronized TempFileTools getInstance(File targetFolder,
			TimeUnit timeUnit,
			long time,
			boolean useCleaner) throws IOException
	{
		targetFolder = targetFolder.getCanonicalFile();
		TempFileTools instance = instances.get(targetFolder);
		if (instance == null) {
			instance = new TempFileTools(targetFolder, timeUnit, time, useCleaner);
		}
		return instance;
	}

	File createTempFile(String prefix, String suffix) throws IOException {
		int fileCount = targetFolder.list().length;
		
		// check max files limit
		if (fileCount > FILE_LIMIT) {
			// try to recover
			logger.warn("Unscheduled cleaning of temp files, current count: "+fileCount);
			cleaner.cleanFiles();
			
			// check again
			fileCount = targetFolder.list().length;
			if (fileCount > FILE_LIMIT) {
				// still to much
				throw new IOException("Max temporary file count reached, please try again later");
			}
		}
		
		return File.createTempFile(prefix, suffix, targetFolder);
	}

	private static final class CleanerThread extends Thread {

		private final long validityPeriod;
		private volatile boolean doStop = false;
		private final File targetFolder;

		private CleanerThread(long validityPeriod, File targetFolder) {
			this.validityPeriod = validityPeriod;
			this.targetFolder = targetFolder;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(validityPeriod);
					int count = cleanFiles();
					logger.info("Removed "+count+" files from: "+targetFolder);
				} catch (InterruptedException exception) {
					if (!doStop) {
						throw new RuntimeException(exception);
					}
					return;
				}
			}
		}
		
		private synchronized int cleanFiles() {
			File[] files = targetFolder.listFiles();
			long timeOut = new Date().getTime() - validityPeriod;
			int removedCount = 0;
			for (File file : files) {
				long lastModified = file.lastModified();
				if (lastModified < timeOut) {
					file.delete();
					removedCount += 1;
				}
			}
			return removedCount;
		}
	}

	void stopCleaner() {
		if (cleaner != null) {
			cleaner.doStop = true;
			cleaner.interrupt();
		}
	}

}
