package org.bbop.termgenie.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class LoggingCallback<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable caught) {
		Logger logger = Logger.getLogger("TermGenieLogger");
		logger.log(Level.SEVERE, "An Internal error occured.", caught);
	}
}
