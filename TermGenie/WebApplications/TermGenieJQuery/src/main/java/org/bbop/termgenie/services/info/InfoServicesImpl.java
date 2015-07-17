package org.bbop.termgenie.services.info;

import org.bbop.termgenie.core.rules.ReasonerFactory;

import com.google.inject.Inject;

public class InfoServicesImpl implements InfoServices {
	
	private final String termgenieBuildTimestamp;
	private final String termgenieRevision;
	private final String termgenieRevisionUrl;
	private final String owlapiVersion;
	private final String reasonerName;
	private final String reasonerVersion;
	
	@Inject
	public InfoServicesImpl(ReasonerFactory rf) {
		super();
		termgenieBuildTimestamp = getManifestTag("termgenie-build-timestamp", "unknown");
		termgenieRevision = getManifestTag("git-revision-sha1", "unknown");
		termgenieRevisionUrl = getManifestTag("git-revision-url", "unknown");
		owlapiVersion = getOwlApiVersion();
		this.reasonerName = rf.getReasonerName();
		this.reasonerVersion = rf.getReasonerVersion();
	}
	
	private static String getManifestTag(String tag, String defaultValue) {
		String value = owltools.version.VersionInfo.getManifestVersion(tag);
		return value == null ? defaultValue : value;
	}
	
	private static String getOwlApiVersion() {
		return org.semanticweb.owlapi.util.VersionInfo.getVersionInfo().getVersion();
	}

	@Override
	public JsonInfoDetails getInfoDetails() {
		JsonInfoDetails details = new JsonInfoDetails();
		
		// termgenie
		details.setTermgenieBuildTimestamp(termgenieBuildTimestamp);
		details.setTermgenieRevision(termgenieRevision);
		details.setTermgenieRevisionUrl(termgenieRevisionUrl);
		
		// owlapi
		details.setOwlapiVersion(owlapiVersion);
		
		// reasoner
		details.setReasonerName(reasonerName);
		details.setReasonerVersion(reasonerVersion);
		
		return details;
	}

}
