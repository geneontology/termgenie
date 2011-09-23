package org.bbop.termgenie.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;

/**
 * Manage sessions and session data using the servlet API.
 */
@Singleton
public class SessionHandlerImpl implements SessionHandler {

	private static final String TERM_GENIE_SESSION_OBJECT = "TERM_GENIE_SESSION_OBJECT";
	private static final Logger logger = Logger.getLogger(SessionHandlerImpl.class);

	private static class SessionObject {

		private boolean authenticated = false;
		private String username = null;
		private final Map<String, String> values = new ConcurrentHashMap<String, String>();

		String put(String key, String value) {
			return values.put(key, value);
		}

		String get(String key) {
			return values.get(key);
		}
	}

	@Override
	public String createSession(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		String id = session.getId();
		if (session.isNew()) {
			session.setAttribute(TERM_GENIE_SESSION_OBJECT, new SessionObject());
			logger.info("Created new session with id: " + id);
		}
		else {
			logger.info("Re-using session with id: " + id);
		}
		return id;
	}

	@Override
	public boolean isValidSession(String sessionId, HttpSession session) {
		if (session != null) {
			return sessionId.equals(session.getId()) && session.getAttribute(TERM_GENIE_SESSION_OBJECT) != null;
		}
		return false;
	}

	@Override
	public boolean login(String sessionId, String username, String password, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			// TODO add a proper authentication check
			logger.info("Trying to login.");
			if ("test".equals(username) && "123456".equals(password)) {
				SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
				synchronized (sessionObject) {
					sessionObject.authenticated = true;
					sessionObject.username = username;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void logout(String sessionId, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				if (sessionObject.authenticated) {
					// TODO add a proper logout to user management.
				}
				sessionObject.authenticated = false;
				sessionObject.username = null;
			}
		}
	}

	@Override
	public String isAuthenticated(String sessionId, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				if (sessionObject.authenticated) {
					return sessionObject.username;
				}
			}
		}
		return null;
	}

	@Override
	public boolean keepSessionAlive(String sessionId, HttpSession session) {
		return isValidSession(sessionId, session);
	}

	@Override
	public String getValue(String sessionId, String key, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				String value = sessionObject.get(key);
				return value;
			}
		}
		return null;
	}

	@Override
	public String[] getValues(String sessionId, String[] keys, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				String[] results = new String[keys.length];
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					results[i] = sessionObject.get(key);
				}
				return results;
			}
		}
		return null;
	}

	@Override
	public boolean setValue(String sessionId, String key, String value, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				sessionObject.put(key, value);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setValues(String sessionId, String[] keys, String[] values, HttpSession session)
	{
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = (SessionObject) session.getAttribute(TERM_GENIE_SESSION_OBJECT);
			synchronized (sessionObject) {
				for (int i = 0; i < keys.length; i++) {
					sessionObject.put(keys[i], values[i]);
				}
				return true;
			}
		}
		return false;
	}

}
