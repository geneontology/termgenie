package org.bbop.termgenie.services;

import javax.servlet.http.HttpSession;

import org.bbop.termgenie.user.UserData;

import com.google.inject.Singleton;

/**
 * Extend {@link SessionHandlerImpl} to implement the additional internal
 * methods of {@link InternalSessionHandler}.
 */
@Singleton
public class InternalSessionHandlerImpl extends SessionHandlerImpl implements InternalSessionHandler
{
	@Override
	public void setAuthenticated(UserData userData, HttpSession session) {
		if (session != null) {
			SessionObject sessionObject = getSessionObject(session);
			if (sessionObject != null) {
				synchronized (sessionObject) {
					sessionObject.authenticated(userData);
				}
			}
		}

	}
	
	@Override
	public UserData getUserData(HttpSession session) {
		System.out.println("InternalSessionHandlerImpl has session["+session+"]");
		if (session != null) {
			SessionObject sessionObject = getSessionObject(session);
			System.out.println("InternalSessionHandlerImpl has sessionObject["+sessionObject+"]");
			if (sessionObject != null) {
				synchronized (sessionObject) {
					System.out.println("InternalSessionHandlerImpl has userData["+sessionObject.getUserData()+"]");
					return sessionObject.getUserData();
				}
			}
		}
		return null;
	}

}
