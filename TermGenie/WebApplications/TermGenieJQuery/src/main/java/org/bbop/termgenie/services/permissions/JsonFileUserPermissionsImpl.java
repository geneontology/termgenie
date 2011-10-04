package org.bbop.termgenie.services.permissions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.services.permissions.PermissionsData.TermGeniePermissions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class JsonFileUserPermissionsImpl implements UserPermissions {

	private static final String FLAG_SCREEN_NAME = "screenname";
	private static final String FLAG_ALLOW_WRITE = "allowWrite";
	private static final String APPLICATION_NAME = "termgenie";
	
	private final File jsonPermissionsFile;

	@Inject
	JsonFileUserPermissionsImpl(@Named("JsonUserPermissionsFileName") String jsonPermissionsFileName) {
		jsonPermissionsFile = new File(jsonPermissionsFileName);
		if (!jsonPermissionsFile.isFile() || !jsonPermissionsFile.canRead()) {
			throw new RuntimeException("Invalid permissions file: "+jsonPermissionsFile);
		}
		PermissionsData permissionsData = loadFile(jsonPermissionsFile);
		if (permissionsData == null) {
			throw new RuntimeException("Empty permissions: "+jsonPermissionsFile);
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

	@Override
	public boolean allowCommit(String guid, Ontology ontology) {
		PermissionsData permissions = loadFile(jsonPermissionsFile);
		if (permissions != null) {
			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid, APPLICATION_NAME);
			if (termgeniePermissions != null) {
				Map<String, String> flags = termgeniePermissions.getPermissionFlags(ontology.getUniqueName());
				if (flags != null) {
					String value = flags.get(FLAG_ALLOW_WRITE);
					if (value != null) {
						return "true".equals(value.toLowerCase());
					}
				}
			}
		}
		return false;
	}

	@Override
	public CommitUserData getCommitUserData(String guid, Ontology ontology) {
		PermissionsData permissions = loadFile(jsonPermissionsFile);
		if (permissions != null) {
			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid, APPLICATION_NAME);
			if (termgeniePermissions != null) {
				Map<String, String> flags = termgeniePermissions.getPermissionFlags(ontology.getUniqueName());
				if (flags != null) {
					String value = flags.get(FLAG_ALLOW_WRITE);
					if (value != null && "true".equals(value.toLowerCase())) {
						String screenname = flags.get(FLAG_SCREEN_NAME);
						return new CommitUserDataImpl(null, null, screenname);
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
