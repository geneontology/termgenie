package org.bbop.termgenie.user.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.termgenie.tools.ResourceLoader;
import org.bbop.termgenie.user.OrcidUserData;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.XrefUserData;
import org.bbop.termgenie.user.simple.SimpleUserDataProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GeneOntologyJsonUserDataProvider extends SimpleUserDataProvider {

	private static final Logger logger = Logger.getLogger(GeneOntologyJsonUserDataProvider.class);
	
	static final String ConfigResourceName = "GeneOntologyJsonUserDataProviderConfigResource";
	static final String AdditionalXrefResourcesName = "GeneOntologyAdditionalXrefResources";
	
	private static final ResourceProvider resourceProvider = new ResourceProvider();

	private final String gocConfigResource;
	private List<String> additionalXrefResources = null;

	@Inject
	GeneOntologyJsonUserDataProvider(@Named(ConfigResourceName) String gocConfigResource)
	{
		super();
		this.gocConfigResource = gocConfigResource;
	}
	
	/**
	 * @param additionalXrefResources the additionalXrefResources to set
	 */
	@Inject(optional=true)
	public void setAdditionalXrefResources(@Nullable @Named(AdditionalXrefResourcesName) List<String> additionalXrefResources) {
		this.additionalXrefResources = additionalXrefResources;
	}

	static List<UserData> loadUserData(String resource) {
		List<UserData> userDatalist;
		Gson gson = new Gson();
		Reader jsonReader = null;
		try {
			jsonReader = resourceProvider.getResourceReader(resource);
			Type typeOfT = new TypeToken<List<UserData>>(){ /* empty */}.getType();
			userDatalist = gson.fromJson(jsonReader, typeOfT);
		}
		finally {
			IOUtils.closeQuietly(jsonReader);
		}
		if (userDatalist != null && !userDatalist.isEmpty()) {
			for (UserData userData : userDatalist) {
				SimpleUserDataProvider.normalize(userData);
			}
			return userDatalist;
		}
		return Collections.emptyList();
	}
	
	@Override
	public UserData getUserDataPerEMail(String email) {
		List<UserData> data = loadUserData(gocConfigResource);
		for (UserData userData : data) {
			if (email.equals(userData.getEmail())) {
				return userData;
			}
		}
		logger.warn("Could not retrieve user data for email: "+email);
		return super.getUserDataPerEMail(email);
	}
	
	@Override
	public UserData getUserDataPerGuid(String guid, List<String> emails) {
		List<UserData> data = loadUserData(gocConfigResource);
		for (UserData userData : data) {
			if (guid.equals(userData.getGuid())) {
				return userData;
			}
		}
		logger.warn("Could not retrieve an xref for emails: "+emails);
		return super.getUserDataPerGuid(guid, emails);
	}

	
	@Override
	public List<XrefUserData> getXrefUserData() {
		List<UserData> data = loadUserData(gocConfigResource);
		List<XrefUserData> filtered = new ArrayList<XrefUserData>(data.size());
		for(UserData elem : data) {
			if (elem.getXref() != null) {
				filtered.add(elem);
			}
		}
		return filtered;
	}

	@Override
	public Set<String> getAdditionalXrefs() {
		Set<String> xrefs = null;
		for(String resource : additionalXrefResources) {
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(resourceProvider.getResourceReader(resource));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					line = StringUtils.trimToNull(line);
					if (line != null && line.charAt(0) != '#') {
						String[] split = StringUtils.split(line, '\t');
						if (split.length > 0) {
							if (xrefs == null) {
								xrefs = new HashSet<String>();
							}
							xrefs.add(split[0]);
						}
					}
				}
			} catch (IOException exception) {
				logger.warn("Could not retrieve an xref for additional resource: "+resource, exception);
			}
			finally {
				IOUtils.closeQuietly(bufferedReader);
			}
		}
		return xrefs;
	}

	@Override
	public List<OrcidUserData> getOrcIdUserData() {
		List<UserData> data = loadUserData(gocConfigResource);
		List<OrcidUserData> filtered = new ArrayList<OrcidUserData>(data.size());
		for(UserData elem : data) {
			if (elem.getOrcid() != null) {
				filtered.add(elem);
			}
		}
		return filtered;
	}



	static class ResourceProvider extends ResourceLoader {

		ResourceProvider() {
			super(true);
		}

		Reader getResourceReader(String resource) {
			InputStream inputStream = loadResource(resource);
			InputStreamReader reader = new InputStreamReader(inputStream);
			return reader;
		}
	}
}
