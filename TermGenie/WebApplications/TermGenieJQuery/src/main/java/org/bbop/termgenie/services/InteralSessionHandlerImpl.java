package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import com.google.inject.Singleton;

/**
 * Extend {@link SessionHandlerImpl} to implement the additional internal
 * methods of {@link InternalSessionHandler}.
 */
@Singleton
public class InteralSessionHandlerImpl extends SessionHandlerImpl implements InternalSessionHandler
{

	@Override
	public void setAuthenticated(String screenname, String guid, HttpSession session) {
		if (session != null) {
			SessionObject sessionObject = getSessionObject(session);
			if (sessionObject != null) {
				synchronized (sessionObject) {
					sessionObject.authenticated(screenname, guid);
				}
			}
		}

	}
}
