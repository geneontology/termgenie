package org.bbop.termgenie.services.permissions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.services.permissions.PermissionsData.TermGeniePermissions;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class JsonFileUserPermissionsImpl implements UserPermissions {

	private static final String FLAG_SCREEN_NAME = "screenname";
	private static final String FLAG_ALLOW_WRITE = "allowWrite";
	private static final String FLAG_ALLOW_COMMIT_REVIEW = "allowCommitReview";
	private static final String FLAG_ALLOW_MANAGEMENT = "allowManagement";

	private final String applicationName;
	private final File jsonPermissionsFile;

	@Inject
	JsonFileUserPermissionsImpl(@Named("JsonUserPermissionsFileName") String jsonPermissionsFileName,
			@Named("JsonUserPermissionsApplicationName") String applicationName)
	{
		this.applicationName = applicationName;
		jsonPermissionsFile = new File(jsonPermissionsFileName);
		if (!jsonPermissionsFile.isFile() || !jsonPermissionsFile.canRead()) {
			throw new RuntimeException("Invalid permissions file: " + jsonPermissionsFile);
		}
		PermissionsData permissionsData = loadFile(jsonPermissionsFile);
		if (permissionsData == null) {
			throw new RuntimeException("Empty permissions: " + jsonPermissionsFile);
		}
	}

	static PermissionsData loadFile(File jsonPermissionsFile) {
		try {
			String configString = FileUtils.readFileToString(jsonPermissionsFile);
			PermissionsData permissionsData = PermissionsData.loadFromJson(configString);
			return permissionsData;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	static PermissionsData loadInputSteam(InputStream jsonPermissionsInputStream) {
		try {
			String configString = IOUtils.toString(jsonPermissionsInputStream);
			PermissionsData permissionsData = PermissionsData.loadFromJson(configString);
			return permissionsData;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(jsonPermissionsInputStream);
		}
	}

	@Override
	public boolean allowCommitReview(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getUniqueName(), FLAG_ALLOW_COMMIT_REVIEW);
	}

	@Override
	public boolean allowCommit(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getUniqueName(), FLAG_ALLOW_WRITE);
	}

	private boolean checkPermissions(UserData userData, String group, String flag) {
		PermissionsData permissions = loadFile(jsonPermissionsFile);
		if (permissions != null && userData != null) {
			String guid = userData.getGuid();
			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid,
					applicationName);
			if (termgeniePermissions != null) {
				Map<String, String> groupFlags = termgeniePermissions.getPermissionFlags(group);
				if (groupFlags != null) {
					String value = groupFlags.get(flag);
					if (value != null) {
						return "true".equals(value.toLowerCase());
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean allowManagementAccess(UserData userData) {
		return checkPermissions(userData, applicationName, FLAG_ALLOW_MANAGEMENT);
	}

	@Override
	public CommitUserData getCommitReviewUserData(UserData userData, Ontology ontology) {
		return retrieveCommitUserData(userData,
				ontology.getUniqueName(),
				ontology,
				FLAG_ALLOW_COMMIT_REVIEW);
	}

	@Override
	public CommitUserData getCommitUserData(UserData userData, Ontology ontology) {
		return retrieveCommitUserData(userData, ontology.getUniqueName(), ontology, FLAG_ALLOW_WRITE);
	}

	private CommitUserData retrieveCommitUserData(UserData userData,
			String group,
			Ontology ontology,
			String flag)
	{
		PermissionsData permissions = loadFile(jsonPermissionsFile);
		if (permissions != null && userData != null) {
			String guid = userData.getGuid();
			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid,
					applicationName);
			if (termgeniePermissions != null) {
				Map<String, String> groupFlags = termgeniePermissions.getPermissionFlags(group);
				if (groupFlags != null) {
					String groupValue = groupFlags.get(flag);
					if (groupValue != null && "true".equals(groupValue.toLowerCase())) {
						Map<String, String> ontologyFlags = termgeniePermissions.getPermissionFlags(ontology.getUniqueName());
						if (ontologyFlags != null) {
							String screenname = ontologyFlags.get(FLAG_SCREEN_NAME);
							return new CommitUserDataImpl(null, null, screenname);
						}
					}
				}
			}
		}
		return null;
	}

	private static class CommitUserDataImpl implements CommitUserData {

		private final String username;
		private final String password;
		private final String screenname;

		/**
		 * @param username
		 * @param password
		 * @param screenname
		 */
		CommitUserDataImpl(String username, String password, String screenname) {
			super();
			this.username = username;
			this.password = password;
			this.screenname = screenname;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public String getScreenname() {
			return screenname;
		}
	}
}
