package org.bbop.termgenie.services;

import java.util.List;

import org.bbop.termgenie.core.process.ProcessStateMessage;


public interface ProgressMonitor {

	public List<ProcessStateMessage> getProgress(String uuid);
}
