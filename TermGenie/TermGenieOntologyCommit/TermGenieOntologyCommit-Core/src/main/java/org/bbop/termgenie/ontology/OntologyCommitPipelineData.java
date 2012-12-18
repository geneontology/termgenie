package org.bbop.termgenie.ontology;

import java.io.File;
import java.util.List;

public interface OntologyCommitPipelineData {

	public List<File> getTargetFiles();

	public List<File> getModifiedTargetFiles();

}