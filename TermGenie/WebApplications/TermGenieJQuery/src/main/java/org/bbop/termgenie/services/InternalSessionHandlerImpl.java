package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import com.google.inject.Singleton;

/**
 * Extend {@link SessionHandlerImpl} to implement the additional internal
 * methods of {@link InternalSessionHandler}.
 */
@Singleton
public class InternalSessionHandlerImpl extends SessionHandlerImpl implements InternalSessionHandler
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

	@Override
	public String getGUID(HttpSession session) {
		if (session != null) {
			SessionObject sessionObject = getSessionObject(session);
			if (sessionObject != null) {
				synchronized (sessionObject) {
					return sessionObject.getGUID();
				}
			}
		}
		return null;
	}
}
