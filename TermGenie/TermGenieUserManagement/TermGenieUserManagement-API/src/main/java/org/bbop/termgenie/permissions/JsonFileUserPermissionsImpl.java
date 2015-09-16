package org.bbop.termgenie.permissions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.permissions.ComplexPermissionsData.TermGeniePermissions;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class JsonFileUserPermissionsImpl implements UserPermissions {

	private static final String FLAG_ALLOW_WRITE = "allowWrite";
	private static final String FLAG_ALLOW_COMMIT_REVIEW = "allowCommitReview";
	private static final String FLAG_ALLOW_MANAGEMENT = "allowManagement";
	private static final String FLAG_ALLOW_FREE_FORM = "allowFreeForm";
	private static final String FLAG_ALLOW_FREE_FORM_WRITE = "allowFreeFormWrite";
	private static final String FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL = "allowFreeFormLitXrefOptional";

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
		ComplexPermissionsData complexPermissionsData = loadFile(jsonPermissionsFile);
		if (complexPermissionsData == null) {
			throw new RuntimeException("Empty permissions: " + jsonPermissionsFile);
		}
	}

	static ComplexPermissionsData loadFile(File jsonPermissionsFile) {
		try {
			String configString = FileUtils.readFileToString(jsonPermissionsFile);
			ComplexPermissionsData complexPermissionsData = ComplexPermissionsData.loadFromJson(configString);
			return complexPermissionsData;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	static ComplexPermissionsData loadInputSteam(InputStream jsonPermissionsInputStream) {
		try {
			String configString = IOUtils.toString(jsonPermissionsInputStream);
			ComplexPermissionsData complexPermissionsData = ComplexPermissionsData.loadFromJson(configString);
			return complexPermissionsData;
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		finally {
			IOUtils.closeQuietly(jsonPermissionsInputStream);
		}
	}

	@Override
	public boolean allowCommitReview(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getName(), FLAG_ALLOW_COMMIT_REVIEW);
	}

	@Override
	public boolean allowCommit(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getName(), FLAG_ALLOW_WRITE);
	}

	@Override
	public boolean allowFreeForm(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getName(), FLAG_ALLOW_FREE_FORM);
	}

	@Override
	public boolean allowFreeFormCommit(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getName(), FLAG_ALLOW_FREE_FORM_WRITE);
	}

	@Override
	public boolean allowFreeFormLiteratureXrefOptional(UserData userData, Ontology ontology) {
		return checkPermissions(userData, ontology.getName(), FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL);
	}

	private boolean checkPermissions(UserData userData, String group, String...flags) {
		ComplexPermissionsData permissions = loadFile(jsonPermissionsFile);
		if (permissions != null && userData != null) {
			String guid = userData.getGuid();
			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid,
					applicationName);
			if (termgeniePermissions != null) {
				Map<String, String> groupFlags = termgeniePermissions.getPermissionFlags(group);
				if (groupFlags != null) {
					return hasAllFlags(groupFlags, flags);
				}
			}
		}
		return false;
	}
	
	boolean hasAllFlags(Map<String, String> groupFlags, String[] flags) {
		boolean[] results = new boolean[flags.length];
		Arrays.fill(results, false);
		for (int i = 0; i < flags.length; i++) {
			String flag = flags[i];
			String value = groupFlags.get(flag);
			if (value != null) {
				results[i] = "true".equals(value.toLowerCase());
			}
		}
		boolean result = results[0];
		if (results.length > 1) {
			for (int i = 1; i < results.length; i++) {
				result = result && results[i];
			}
		}
		return result;
	}

	@Override
	public boolean allowManagementAccess(UserData userData) {
		return checkPermissions(userData, applicationName, FLAG_ALLOW_MANAGEMENT);
	}

//	@Override
//	public CommitUserData getCommitReviewUserData(UserData userData, Ontology ontology) {
//		return retrieveCommitUserData(userData,
//				ontology.getName(),
//				ontology,
//				FLAG_ALLOW_COMMIT_REVIEW);
//	}
//
//	@Override
//	public CommitUserData getCommitUserData(UserData userData, Ontology ontology) {
//		return retrieveCommitUserData(userData, ontology.getName(), ontology, FLAG_ALLOW_WRITE);
//	}
//
//	private CommitUserData retrieveCommitUserData(UserData userData,
//			String group,
//			Ontology ontology,
//			String flag)
//	{
//		ComplexPermissionsData permissions = loadFile(jsonPermissionsFile);
//		if (permissions != null && userData != null) {
//			String guid = userData.getGuid();
//			TermGeniePermissions termgeniePermissions = permissions.getPermissions(guid,
//					applicationName);
//			if (termgeniePermissions != null) {
//				Map<String, String> groupFlags = termgeniePermissions.getPermissionFlags(group);
//				if (groupFlags != null) {
//					String groupValue = groupFlags.get(flag);
//					if (groupValue != null && "true".equals(groupValue.toLowerCase())) {
//						Map<String, String> ontologyFlags = termgeniePermissions.getPermissionFlags(ontology.getName());
//						if (ontologyFlags != null) {
//							String screenname = ontologyFlags.get(FLAG_SCREEN_NAME);
//							return new CommitUserDataImpl(null, null, screenname);
//						}
//					}
//				}
//			}
//		}
//		return null;
//	}

//	private static class CommitUserDataImpl implements CommitUserData {
//
//		private final String username;
//		private final String password;
//		private final String screenname;
//
//		/**
//		 * @param username
//		 * @param password
//		 * @param screenname
//		 */
//		CommitUserDataImpl(String username, String password, String screenname) {
//			super();
//			this.username = username;
//			this.password = password;
//			this.screenname = screenname;
//		}
//
//		@Override
//		public String getUsername() {
//			return username;
//		}
//
//		@Override
//		public String getPassword() {
//			return password;
//		}
//
//		@Override
//		public String getScreenname() {
//			return screenname;
//		}
//	}
}
