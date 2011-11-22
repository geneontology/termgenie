package org.bbop.termgenie.user;

import java.util.List;


public interface UserDataProvider {

	public UserData getUserDataPerEMail(String email);
	
	public UserData getUserDataPerGuid(String guid, List<String> emails);
}
