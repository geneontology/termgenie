package org.bbop.termgenie.user.simple;

import java.util.List;

import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataImpl;
import org.bbop.termgenie.user.UserDataProvider;

/**
 * Simple implementation to provide some user data, just by analyzing an email
 * address.
 */
public class SimpleUserDataProvider implements UserDataProvider {

	@Override
	public UserData getUserDataPerEMail(String email) {
		String screenname = getNameFromEMail(email);
		String guid = email;
		String xref = null;
		String scmAlias = screenname;
		return new UserDataImpl(screenname, guid, email, xref, scmAlias);
	}

	protected String getNameFromEMail(String email) {
		int pos = email.indexOf('@');
		if (pos > 0) {
			return email.substring(0, pos);
		}
		return email;
	}

	@Override
	public UserData getUserDataPerGuid(String guid, List<String> emails) {
		String email = emails.get(0);
		String screenname = getNameFromEMail(email);
		String xref = null;
		String scmAlias = screenname;
		return new UserDataImpl(screenname, guid, email, xref, scmAlias);
	}

}
