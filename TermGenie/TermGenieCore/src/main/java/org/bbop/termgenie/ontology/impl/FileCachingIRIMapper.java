package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.ontology.IRIMapper;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Cache remote IRIs as a local file. Allow periodic reloads of the original IRI
 * for updates.
 */
@Singleton
public class FileCachingIRIMapper implements IRIMapper {

	private static final Logger logger = Logger.getLogger(FileCachingIRIMapper.class);

	private final FileValidity validityHelper;
	private final File cacheDirectory;

	@Inject
	FileCachingIRIMapper(@Named("FileCachingIRIMapperLocalCache") String localCache,
			@Named("FileCachingIRIMapperPeriod") long period,
			@Named("FileCachingIRIMapperTimeUnit") TimeUnit unit)
	{
		super();
		cacheDirectory = new File(localCache);
		createFolder(cacheDirectory);
		validityHelper = new FileValidity(TimeUnit.MILLISECONDS.convert(period, unit));

		// use java.concurrent to schedule a periodic task of reloading the IRI
		// content.
		Runnable command = new Runnable() {

			@Override
			public void run() {
				reloadIRIs();
			}
		};
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(command, period, period, unit);
	}

	protected void reloadIRIs() {
		validityHelper.setInvalidRecursive(cacheDirectory);
	}

	/**
	 * Overwrite this method to implement more sophisticated methods. E.g.,
	 * fall-back on local copies, if the URL is not reachable.
	 * 
	 * @param url
	 * @return inputStream
	 * @throws IOException
	 */
	protected InputStream getInputStream(URL url) throws IOException {
		return url.openStream();
	}

	@Override
	public URL mapUrl(String url) {
		try {
			final URL originalURL = new URL(url);
			String protocol = originalURL.getProtocol().toLowerCase();
			if (protocol.equals("file")) {
				return originalURL;
			}
			else if (protocol.startsWith("http")) {
				return mapUrl(originalURL);
			}
			else {
				throw new RuntimeException("Unknown protocol: " + protocol);
			}
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private synchronized URL mapUrl(URL originalURL) {
		File localFile = localCacheFile(originalURL);
		if (!isValid(localFile)) {
			createFile(localFile);
			download(originalURL, localFile);
		}
		try {
			return localFile.toURI().toURL();
		} catch (MalformedURLException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void createFile(File localFile) {
		File folder = localFile.getParentFile();
		createFolder(folder);
		try {
			localFile.createNewFile();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void download(URL originalURL, File localFile) {
		IOException prevException = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = getInputStream(originalURL);
			outputStream = new FileOutputStream(localFile);
			IOUtils.copy(inputStream, outputStream);

		} catch (IOException exception) {
			prevException = exception;
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException exception) {
					if (prevException == null) {
						prevException = exception;
					}
					else {
						logger.error("Problem closing inputStream.", exception);
					}
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException exception) {
					if (prevException == null) {
						prevException = exception;
					}
					else {
						logger.error("Problem closing outputStream.", exception);
					}
				}
			}
			if (prevException != null) {
				throw new RuntimeException(prevException);
			}
			/*
			 * only set the file valid, if there were no exceptions and the
			 * output stream is closed.
			 */
			validityHelper.setValid(localFile);
		}
	}

	private boolean isValid(File localFile) {
		return localFile.exists() && validityHelper.isValid(localFile);
	}

	private File localCacheFile(URL url) {
		return new File(cacheDirectory, localCacheFilename(url));
	}

	static String localCacheFilename(URL url) {
		StringBuilder sb = new StringBuilder();
		escapeToBuffer(sb, url.getHost());
		escapeToBuffer(sb, url.getPath());
		return sb.toString();
	}

	static void escapeToBuffer(StringBuilder sb, String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '.' || c == '-') {
				sb.append(c);
			}
			else if (c == '/') {
				sb.append(File.separatorChar);
			}
			else {
				sb.append('_');
			}
		}
	}

	private static void createFolder(File folder) throws RuntimeException {
		if (!folder.exists()) {
			boolean success = folder.mkdirs();
			if (!success) {
				throw new RuntimeException("Could not create folder: " + folder.getAbsolutePath());
			}
		}
		if (!folder.isDirectory()) {
			throw new RuntimeException("The indicated location is not a folder: " + folder.getAbsolutePath());
		}
		if (!folder.canWrite()) {
			throw new RuntimeException("Cannot write into the specified folder: " + folder.getAbsolutePath());
		}
	}

	/**
	 * Helper for checking, whether a file is still valid
	 */
	static class FileValidity {

		private static final String VALIDITY_FILE_SUFFIX = ".validity";

		private static final class ValidityFileFilter implements FileFilter {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getName().endsWith(VALIDITY_FILE_SUFFIX);
			}
		}

		private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {

			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
		};

		private final long validPeriod;

		/**
		 * @param validPeriod
		 */
		FileValidity(long validPeriod) {
			super();
			this.validPeriod = validPeriod;
		}

		private File getValidityFile(File file) {
			return new File(file.getParentFile(), file.getName() + VALIDITY_FILE_SUFFIX);
		}

		private Date getDate(File validityFile) {
			if (validityFile.exists()) {
				try {
					String dateString = FileUtils.readFileToString(validityFile);
					return df.get().parse(dateString);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				} catch (ParseException exception) {
					validityFile.delete();
				}
			}
			return null;
		}

		private void setDate(Date date, File validityFile) {
			try {
				FileUtils.write(validityFile, df.get().format(date));
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}

		private void deleteValidityFile(File file) {
			File validityFile = getValidityFile(file);
			if (validityFile.exists()) {
				validityFile.delete();
			}
		}

		void setValid(File file) {
			setDate(new Date(), getValidityFile(file));
		}

		void setInvalid(File file) {
			deleteValidityFile(file);
		}

		void setInvalidRecursive(File directory) {
			File[] files = directory.listFiles(new ValidityFileFilter());
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						setInvalidRecursive(file);
					}
					else {
						file.delete();
					}
				}
			}
		}

		boolean isValid(File file) {
			Date date = getDate(getValidityFile(file));
			if (date != null) {
				Date earliest = new Date(System.currentTimeMillis() - validPeriod);
				return date.after(earliest);
			}
			return false;
		}
	}

}
