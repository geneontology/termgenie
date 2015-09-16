package org.bbop.termgenie.user.simple;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bbop.termgenie.user.OrcidUserData;
import org.bbop.termgenie.user.UserData;
import org.bbop.termgenie.user.UserDataProvider;
import org.bbop.termgenie.user.XrefUserData;

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
		String orcid = null;
		return new UserData(screenname, guid, email, xref, scmAlias, orcid);
	}

	public static String getNameFromEMail(String email) {
		int pos = email.indexOf('@');
		if (pos > 0) {
			return email.substring(0, pos);
		}
		return email;
	}

	protected static String extractSCMAlias(String xref, String email) {
		if (xref != null) {
			int colonPos = xref.indexOf(':');
			if (colonPos > 0 && colonPos < (xref.length() - 2)) {
				return xref.substring(colonPos + 1);
			}
		}
		return getNameFromEMail(email);
	}

	public static void normalize(UserData userData) {
		// normalize e-mail to lower case and trim white spaces
		if (userData.getEmail() != null) {
			userData.setEmail(userData.getEmail().toLowerCase().trim());
		}
		// set guid to e-mail as default
		if (userData.getGuid() == null) {
			userData.setGuid(userData.getEmail());
		}
		// create an default scm name, use either the xref or extract from e-mail
		if (userData.getScmAlias() == null) {
			userData.setScmAlias(extractSCMAlias(userData.getXref(), userData.getEmail()));
		}
	}

	@Override
	public List<XrefUserData> getXrefUserData() {
		return Collections.emptyList();
	}

	@Override
	public Set<String> getAdditionalXrefs() {
		return null;
	}

	@Override
	public List<OrcidUserData> getOrcIdUserData() {
		return Collections.emptyList();
	}

}
