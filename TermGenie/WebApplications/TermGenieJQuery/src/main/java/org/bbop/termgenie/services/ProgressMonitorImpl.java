package org.bbop.termgenie.services;

import java.util.List;

import org.bbop.termgenie.core.process.ProcessState;
import org.bbop.termgenie.core.process.ProcessStateMessage;


public class ProgressMonitorImpl implements ProgressMonitor {

	@Override
	public List<ProcessStateMessage> getProgress(String uuid) {
		ProcessState state = ProcessState.getProcessState(uuid);
		if (state != null) {
			List<ProcessStateMessage> messages = state.getMessagesAndClear();
			if (!messages.isEmpty()) {
				return messages;
			}
		}
		return null;
	}

}
