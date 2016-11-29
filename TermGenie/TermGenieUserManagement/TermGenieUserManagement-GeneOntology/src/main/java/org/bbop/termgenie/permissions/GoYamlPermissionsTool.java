package org.bbop.termgenie.permissions;

import static org.bbop.termgenie.permissions.GoYamlUserPermissionsImpl.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bbop.termgenie.tools.YamlTool;

class GoYamlPermissionsTool {

	private GoYamlPermissionsTool(String md5Guid) {
		// private no instances!
	}

	@SuppressWarnings("rawtypes")
	static String checkYaml(File yamlFile, String application) {
		Object data = YamlTool.load(yamlFile);
		if (data == null) {
			return "The yaml file contains no data: "+yamlFile;
		}
		if (data instanceof List == false) {
			return "The top level element should be a list in the yaml: "+yamlFile;
		}
		List list = (List) data;
		if (list.isEmpty()) {
			return "The yaml file contains no data: "+yamlFile;
		}
		// check that the file contains content and at least one permission for the given application
		boolean found = false;
		for (Object object : list) {
			if (object instanceof Map) {
				Map map = (Map) object;
				Object permissions = map.get("authorizations");
				if (permissions != null) {
					if (permissions instanceof Map) {
						Object appPermissions = ((Map) permissions).get(application);
						if (appPermissions != null) {
							found = true;
							break;
						}
					}
				}
			}
		}
		if (found == false) {
			return "The yaml file contains no 'authorizations' for the app '"+application+"' in file: "+yamlFile;
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Map<String, Boolean> loadFromYaml(File yamlFile, String application, String guid) {
		List<Map> userEntries = (List<Map>) YamlTool.load(yamlFile);
		Map<String, Boolean> data = new HashMap<String, Boolean>();
		for(Map userEntry : userEntries) {
			boolean isUser = hasGithubUser(userEntry,guid);
			System.out.println("GoYamlPermissionsTool userEntry isUser: "+isUser + " for " + guid + " and has accounts: " + userEntry.containsKey("accounts"));
			if (isUser) {
				Map permissions = (Map) userEntry.get("authorizations");
				System.out.println("GoYamlPermissionsTool "+ guid + " has permissions: "+ permissions + " and has applications " + permissions.get(application) + " for "+application);
				if (permissions != null) {
					Map applicationPermissions = (Map) permissions.get(application);
					if (applicationPermissions != null) {
						addValue(data, FLAG_ALLOW_WRITE, applicationPermissions);
						addValue(data, FLAG_ALLOW_COMMIT_REVIEW, applicationPermissions);
						addValue(data, FLAG_ALLOW_MANAGEMENT, applicationPermissions);
						addValue(data, FLAG_ALLOW_FREE_FORM, applicationPermissions);
						addValue(data, FLAG_ALLOW_FREE_FORM_LIT_XREF_OPTIONAL, applicationPermissions);
					}
				}
			}
		}
		System.out.println("GoYamlPermissionsTool returning data:  " + data.toString());
		return data;
	}

	private static boolean hasGithubUser(Map userEntry, String guid) {
		Map accountsMap = (Map) userEntry.get("accounts");
		if(accountsMap!=null){
			String githubUsername = (String) accountsMap.get("github");
			if(githubUsername!=null && githubUsername.equals(guid)){
				return true ;
			}
		}
		return false ;
	}

	@SuppressWarnings("rawtypes")
	static void addValue(Map<String,Boolean> target, String key, Map source) {
		Object value = source.get(key);
		if (value != null) {
			if (value instanceof Boolean) {
				target.put(key, (Boolean) value);
			}
			else if (value instanceof CharSequence) {
				String s = value.toString();
				if ("true".equalsIgnoreCase(s)) {
					target.put(key, Boolean.TRUE);
				}
			}
		}
	}
}
