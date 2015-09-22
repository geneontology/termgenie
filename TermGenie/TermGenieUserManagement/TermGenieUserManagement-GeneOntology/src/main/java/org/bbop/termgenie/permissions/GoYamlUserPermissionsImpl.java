package org.bbop.termgenie.permissions;

import java.io.File;
import java.util.Map;

import org.bbop.termgenie.core.Ontology;
import org.bbop.termgenie.tools.GitYaml;
import org.bbop.termgenie.tools.Md5Tool;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class GoYamlUserPermissionsImpl implements UserPermissions {

	static final String FLAG_ALLOW_WRITE = "allow-write";
	static final String FLAG_ALLOW_COMMIT_REVIEW = "allow-review";
	static final String FLAG_ALLOW_MANAGEMENT = "allow-management";
	static final String FLAG_ALLOW_FREE_FORM = "allow-freeform";
	static final String FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL = "allow-freeform-litxref-optional";

	private final String applicationName;
	private final GitYaml gitYamlFile;

	@Inject
	GoYamlUserPermissionsImpl(GitYaml gitYamlFile,
			@Named("YamlUserPermissionsApplicationName") String applicationName)
	{
		this.gitYamlFile = gitYamlFile;
		this.applicationName = applicationName;
		File yamlFile = gitYamlFile.getYamlFile();
		if (!yamlFile.isFile() || !yamlFile.canRead()) {
			throw new RuntimeException("Invalid permissions file: " + yamlFile);
		}
		GoYamlPermissionsTool.checkYaml(yamlFile, applicationName);
	}

	@Override
	public boolean allowCommitReview(UserData userData, Ontology ontology) {
		return checkPermissions(userData, FLAG_ALLOW_COMMIT_REVIEW);
	}

	@Override
	public boolean allowCommit(UserData userData, Ontology ontology) {
		return checkPermissions(userData, FLAG_ALLOW_WRITE);
	}

	@Override
	public boolean allowFreeForm(UserData userData, Ontology ontology) {
		return checkPermissions(userData, FLAG_ALLOW_FREE_FORM);
	}

	@Override
	public boolean allowFreeFormCommit(UserData userData, Ontology ontology) {
		return checkPermissions(userData, FLAG_ALLOW_FREE_FORM);
	}

	@Override
	public boolean allowFreeFormLiteratureXrefOptional(UserData userData, Ontology ontology) {
		return checkPermissions(userData, FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL);
	}

	private boolean checkPermissions(UserData userData, String flag) {
		if (userData != null) {
			return checkPermissions(userData.getGuid(), flag);
		}
		return false;
	}
	
	boolean checkPermissions(String guid, String flag) {
		String md5Guid = Md5Tool.md5(guid);
		Map<String, Boolean> userPermissions = 
				GoYamlPermissionsTool.loadFromYaml(gitYamlFile.getYamlFile(), applicationName, md5Guid);
		if (userPermissions != null) {
			return hasFlag(userPermissions, flag);
		}
		return false;
	}
	
	boolean hasFlag(Map<String, Boolean> groupFlags, String flag) {
		boolean result = false;
		Boolean value = groupFlags.get(flag);
		if (value != null) {
			result = value.booleanValue();
		}
		return result;
	}

	@Override
	public boolean allowManagementAccess(UserData userData) {
		return checkPermissions(userData, FLAG_ALLOW_MANAGEMENT);
	}
}
