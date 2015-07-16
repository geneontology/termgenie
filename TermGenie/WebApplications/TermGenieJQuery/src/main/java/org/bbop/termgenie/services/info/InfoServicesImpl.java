package org.bbop.termgenie.services.info;

import org.semanticweb.owlapi.util.VersionInfo;

public class InfoServicesImpl implements InfoServices {

	@Override
	public JsonInfoDetails getInfoDetails() {
		JsonInfoDetails details = new JsonInfoDetails();
		details.setOwlapiVersion(VersionInfo.getVersionInfo().getVersion());
		return details;
	}

}
