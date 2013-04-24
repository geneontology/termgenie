package org.bbop.termgenie.user.go;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.bbop.termgenie.tools.ResourceLoader;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.simple.SimpleUserDataProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
@Deprecated
public class GeneOntologyUserDataProvider extends SimpleUserDataProvider {

	private static final Logger logger = Logger.getLogger(GeneOntologyUserDataProvider.class);
	
	static final String ConfigResourceName = "GeneOntologyUserDataProviderConfigResource";
	static final String ConfigResourceSeparatorName = "GeneOntologyUserDataProviderConfigResourceSeparator";
	static final String MappingResourceName = "GeneOntologyUserDataProviderMappingResource";
	static final String MappingResourceSeparatorName = "GeneOntologyUserDataProviderMappingResourceSeparator";
	
	private static final ResourceProvider resourceProvider = new ResourceProvider();

	private final String gocConfigResource;
	private final char gocConfigResourceSeparator;
	private final String gocMappingResource;
	private final char gocMappingResourceSeparator;

	@Inject
	GeneOntologyUserDataProvider(@Named(ConfigResourceName) String gocConfigResource,
			@Named(ConfigResourceSeparatorName) char gocConfigResourceSeparator,
			@Named(MappingResourceName) String gocMappingResource,
			@Named(MappingResourceSeparatorName) char gocMappingResourceSeparator)
	{
		super();
		this.gocConfigResource = gocConfigResource;
		this.gocConfigResourceSeparator = gocConfigResourceSeparator;
		this.gocMappingResource = gocMappingResource;
		this.gocMappingResourceSeparator = gocMappingResourceSeparator;
	}

	@Override
	public UserData getUserDataPerEMail(String email) {
		String xref = getXref(email);
		if (xref != null) {
			UserData userData = getGOCUserData(email, email, xref);
			if (userData != null) {
				return userData;
			}
			logger.warn("Could not retrieve user data for xref: "+xref);
			String screenname = getNameFromEMail(email);
			return new UserData(screenname, email, email, xref, screenname, null);
		}
		logger.warn("Could not retrieve an xref for email: "+email);
		return super.getUserDataPerEMail(email);
	}
	
	
	static List<String> splitLine(String line, char separatorChar) {
		List<String> result = new ArrayList<String>();
		if (line != null) {
			int prev = 0;
			int pos;
			while (prev < line.length() && (pos = line.indexOf(separatorChar, prev)) >= 0) {
				result.add(line.substring(prev, pos));
				prev = pos + 1;
			}
			if (prev == 0) {
				result.add(line);
			} else if (prev < line.length()) {
				result.add(line.substring(prev));
			} else if (line.endsWith(String.valueOf(separatorChar))) {
				result.add("");
			}
		}
		return result;
	}

	private String getXref(String email) {
		LineIterator iterator = resourceProvider.getResourcIterator(gocMappingResource);
		try {
			while (iterator.hasNext()) {
				String line = iterator.next();
				if (line.length() > 0 && line.charAt(0) != '#') {
					List<String> fields = splitLine(line, gocMappingResourceSeparator);
					if (fields.size() == 2) {
						if (email.equals(fields.get(0))) {
							String xref = fields.get(1);
							return xref.trim();
						}
					}
				}
			}
		} finally {
			LineIterator.closeQuietly(iterator);
		}
		return null;
	}
	
	@Override
	public UserData getUserDataPerGuid(String guid, List<String> emails) {
		for (String email : emails) {
			String xref = getXref(email);
			if (xref != null) {
				UserData userData = getGOCUserData(guid, email, xref);
				if (userData != null) {
					return userData;
				}
				logger.warn("Could not retrieve user data for xref: "+xref);
				String screenname = getNameFromEMail(email);
				return new UserData(screenname, email, email, xref, screenname, null);
			}
		}
		logger.warn("Could not retrieve an xref for emails: "+emails);
		return super.getUserDataPerGuid(guid, emails);
	}

	private UserData getGOCUserData(String guid, String email, String xref) {
		LineIterator iterator = resourceProvider.getResourcIterator(gocConfigResource);
		try {
			while (iterator.hasNext()) {
				String line = iterator.next();
				List<String> fields = splitLine(line, gocConfigResourceSeparator);
				if (fields.size() >= 3) {
					String lineXref = fields.get(0).trim();
					if (xref.equals(lineXref)) {
						String screenname = fields.get(2);
						if (screenname.length() < 2) {
							screenname = getNameFromEMail(email);
						}
						String scmAlias = extractSCMAlias(xref, email);
						return new UserData(screenname, guid, email, xref, scmAlias, null);
					}
				}
			}
		} finally {
			LineIterator.closeQuietly(iterator);
		}
		return null;
	}
	
	String extractSCMAlias(String xref, String email) {
		int colonPos = xref.indexOf(':');
		if (colonPos > 0 && colonPos < (xref.length() - 2)) {
			return xref.substring(colonPos + 1);
		}
		return getNameFromEMail(email);
	}

	static class ResourceProvider extends ResourceLoader {

		ResourceProvider() {
			super(true);
		}

		LineIterator getResourcIterator(String resource) {
			InputStream inputStream = loadResource(resource);
			InputStreamReader reader = new InputStreamReader(inputStream);
			return IOUtils.lineIterator(reader);
		}
	}
}
