package org.bbop.termgenie.ontology;

import java.io.File;

public interface OntologyCommitPipelineData {

	public File getSCMTargetFile();

	public File getTargetFile();

	public File getModifiedTargetFile();

	public File getModifiedSCMTargetFile();
}