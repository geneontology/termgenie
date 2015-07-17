package org.bbop.termgenie.services.info;

import org.bbop.termgenie.core.rules.ReasonerFactory;

import com.google.inject.Inject;

public class InfoServicesImpl implements InfoServices {
	
	private final String termgenieBuildDate;
	private final String owlapiVersion;
	private final String reasonerName;
	private final String reasonerVersion;
	
	@Inject
	public InfoServicesImpl(ReasonerFactory rf) {
		super();
		String termgenieTimestamp = owltools.version.VersionInfo.getManifestVersion("termgenie-build-timestamp");
		termgenieBuildDate = termgenieTimestamp == null ? "unknown" : termgenieTimestamp;
		owlapiVersion = org.semanticweb.owlapi.util.VersionInfo.getVersionInfo().getVersion();
		this.reasonerName = rf.getReasonerName();
		this.reasonerVersion = rf.getReasonerVersion();
	}

	@Override
	public JsonInfoDetails getInfoDetails() {
		JsonInfoDetails details = new JsonInfoDetails();
		
		// termgenie
		details.setTermgenieVersion(termgenieBuildDate);
		
		// owlapi
		details.setOwlapiVersion(owlapiVersion);
		
		// reasoner
		details.setReasonerName(reasonerName);
		details.setReasonerVersion(reasonerVersion);
		
		return details;
	}

}
