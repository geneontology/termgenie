package org.bbop.termgenie.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bbop.termgenie.client.MessagePanel.FocusType;

import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class LoggingCallback<T> implements AsyncCallback<T> {
	
	static {
		Logger.getLogger("TermGenieLogger").addHandler(new HasWidgetsLogHandler(MessagePanel.getSystemErrorPanel()));
	}

	@Override
	public void onFailure(Throwable caught) {
		Logger logger = Logger.getLogger("TermGenieLogger");
		logger.log(Level.SEVERE, "An Internal error occured.", caught);
		MessagePanel.popup(FocusType.systemerror);
	}
}
