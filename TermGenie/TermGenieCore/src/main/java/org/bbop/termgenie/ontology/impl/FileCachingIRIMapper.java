package org.bbop.termgenie.ontology.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Cache remote IRIs as a local file. Allow periodic reloads of the original IRI
 * for updates.
 */
@Singleton
class FileCachingIRIMapper implements OWLOntologyIRIMapper {

	private static final Logger logger = Logger.getLogger(FileCachingIRIMapper.class);

	private final FileValidity validityHelper;
	private final File cacheDirectory;
	private FileCachingFilter filter = null; 

	public static interface FileCachingFilter {
		
		public boolean allowCaching(IRI iri);
	}
	
	@Inject
	FileCachingIRIMapper(@Named("FileCachingIRIMapperLocalCache") String localCache,
			@Named("FileCachingIRIMapperPeriod") long period,
			@Named("FileCachingIRIMapperTimeUnit") TimeUnit unit) throws IOException
	{
		super();
		cacheDirectory = new File(localCache);
		FileUtils.forceMkdir(cacheDirectory);
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
	
	@Inject(optional=true)
	void setFilter(FileCachingFilter filter) {
		this.filter = filter;
	}

	protected void reloadIRIs() {
		validityHelper.setInvalidRecursive(cacheDirectory);
	}

	protected InputStream getInputStream(IRI iri) throws IOException {
		DefaultHttpClient client = new ContentEncodingHttpClient();
		final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		client.setRedirectStrategy(redirectStrategy);
		final DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler();
		client.setHttpRequestRetryHandler(retryHandler);
		final HttpGet request = new HttpGet(iri.toURI());
		return tryHttpRequest(client, request, iri, 3);
	}
	
	private InputStream tryHttpRequest(DefaultHttpClient client, HttpGet request, IRI iri, int count) throws IOException {
		// try the load
		final HttpResponse response;
		try {
			response = client.execute(request);
		}
		catch (IOException e) {
			if (count <= 0) {
				// no more retry, handle the final error.
				return handleError(iri, e);
			}
			logger.warn("Retry request for IRI: "+iri+" after exception: "+e.getMessage());
			defaultRandomWait();
			return tryHttpRequest(client, request, iri, count - 1);
		}
		final HttpEntity entity = response.getEntity();
		final StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
			StringBuilder message = new StringBuilder();
			message.append("Web request for IRI '");
			message.append(iri);
			message.append("' failed with status code: ");
			message.append(statusLine.getStatusCode());
			String reasonPhrase = statusLine.getReasonPhrase();
			if (reasonPhrase != null) {
				message.append(" reason: ");
				message.append(reasonPhrase);
			}
			EntityUtils.consume(entity);
			if (count <= 0) {
				// no more retry, handle the final error.
				return handleError(iri, new IOException(message.toString()));
			}
			message.append("\n Retry request.");
			logger.warn(message);

			defaultRandomWait();

			// try again
			return tryHttpRequest(client, request, iri, count - 1);
		}
		return entity.getContent();
	}
	
	private void defaultRandomWait() {
		// wait a random interval between 400 and 1500 ms
		randomWait(400, 1500);
	}
	
	private void randomWait(int min, int max) {
		Random random = new Random(System.currentTimeMillis());
		long wait = min + random.nextInt((max - min));
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Waiting "+wait+" ms for retry.");
			}
			Thread.sleep(wait);
		} catch (InterruptedException exception) {
			logger.warn("Interrupted sleep: Incomplete wait for retry.", exception);
		}
	}

	/**
	 * Overwrite this method to implement more sophisticated methods. E.g.,
	 * fall-back on local copies, if the URL is not reachable.
	 * 
	 * @param iri
	 * @param exception
	 * @return inputStream
	 * @throws IOException
	 */
	protected InputStream handleError(IRI iri, IOException exception) throws IOException {
		logger.error("IOException during fetch of IRI: "+iri, exception);
		throw exception;
	}
	
	@Override
	public IRI getDocumentIRI(IRI ontologyIRI) {
		if (filter != null && filter.allowCaching(ontologyIRI) == false) {
			return null;
		}
		return mapIRI(ontologyIRI);
	}

	private synchronized IRI mapIRI(IRI originalIRI) {
		boolean success = false;
		File localFile = localCacheFile(originalIRI);
		if (isValid(localFile)) {
			success = true;
		}
		else{
			createFile(localFile);
			success = download(originalIRI, localFile);
		}
		if (success) {
			return IRI.create(localFile);
		}
		return null;
	}

	protected boolean createFile(File localFile) {
		File folder = localFile.getParentFile();
		try {
			FileUtils.forceMkdir(folder);
			localFile.createNewFile();
			return true;
		} catch (IOException exception) {
			logger.warn("Could not create local file: "+localFile.getAbsolutePath(), exception);
			return false;
		}
	}

	private boolean download(IRI originalIRI, File localFile) {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			logger.info("Downloading: "+originalIRI+" to file: "+localFile.getAbsolutePath());
			inputStream = getInputStream(originalIRI);
			if (inputStream == null) {
				return false;
			}
			outputStream = new FileOutputStream(localFile);
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
			setValid(localFile);
			return true;
		} catch (IOException exception) {
			logger.warn("Could not download IRI: "+originalIRI, exception);
			return false;
		}
		finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
		
	}

	protected void setValid(File localFile) throws IOException {
		validityHelper.setValid(localFile);
	}

	protected boolean isValid(File localFile) {
		return localFile.exists() && validityHelper.isValid(localFile);
	}

	private File localCacheFile(IRI iri) {
		return new File(cacheDirectory, localCacheFilename(iri));
	}

	static String localCacheFilename(IRI iri) {
		URI uri = iri.toURI();
		StringBuilder sb = new StringBuilder();
		escapeToBuffer(sb, uri.getHost());
		escapeToBuffer(sb, uri.getPath());
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
					FileUtils.deleteQuietly(validityFile);
				} catch (ParseException exception) {
					validityFile.delete();
				}
			}
			return null;
		}

		private void setDate(Date date, File validityFile) throws IOException {
			FileUtils.write(validityFile, df.get().format(date));
		}

		private void deleteValidityFile(File file) {
			File validityFile = getValidityFile(file);
			if (validityFile.exists()) {
				validityFile.delete();
			}
		}

		void setValid(File file) throws IOException {
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
