package org.bbop.termgenie.services;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bbop.termgenie.user.UserData;

import com.google.inject.Singleton;

/**
 * Manage sessions and session data using the servlet API.
 */
@Singleton
public class SessionHandlerImpl implements SessionHandler {

	private static final String TERM_GENIE_SESSION_OBJECT = "TERM_GENIE_SESSION_OBJECT";
	private static final Logger logger = Logger.getLogger(SessionHandlerImpl.class);

	protected static class SessionObject implements Serializable {

		// generated
		private static final long serialVersionUID = -3200919422080908002L;

		private boolean authenticated = false;
		private UserData userData = null;
		private final Map<String, String> values = new ConcurrentHashMap<String, String>();

		String put(String key, String value) {
			return values.put(key, value);
		}

		String get(String key) {
			return values.get(key);
		}
		
		void authenticated(UserData userData) {
			authenticated = true;
			this.userData = userData;
		}
		
		UserData getUserData() {
			return userData;
		}
	}

	@Override
	public String createSession(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		String ip = getClientIpAddr(request);
		String id = session.getId();
		if (session.isNew()) {
			session.setAttribute(TERM_GENIE_SESSION_OBJECT, createSessionObject());
			logger.info("Created new session with id: " + id+" from ip: "+ip);
		}
		else {
			logger.info("Re-using session with id: " + id+" from ip: "+ip);
		}
		return id;
	}

	private static String[] IP_HEADERS = {"VIA", "X-Forwarded-For", "Proxy-Client-IP",
		"WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
	
	private static String getClientIpAddr(HttpServletRequest request) {
		for (String ipHeader : IP_HEADERS) {
			String ip = request.getHeader(ipHeader);  
	        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            return ip;  
	        }
		}
        String ip = request.getRemoteAddr();  
        return ip;  
    }  
	
	protected SessionObject createSessionObject() {
		return new SessionObject();
	}

	protected SessionObject getSessionObject(HttpSession session) {
		Object object = session.getAttribute(TERM_GENIE_SESSION_OBJECT);
		if (object == null) {
			return null;
		}
		return (SessionObject) object;
	}

	@Override
	public boolean isValidSession(String sessionId, HttpSession session) {
		if (session != null) {
			return sessionId.equals(session.getId()) && getSessionObject(session) != null;
		}
		return false;
	}

	@Override
	public void logout(String sessionId, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = getSessionObject(session);
			synchronized (sessionObject) {
				if (sessionObject.authenticated) {
					processLogout(sessionObject);
				}
				sessionObject.authenticated = false;
				sessionObject.userData = null;
			}
		}
	}

	/**
	 * @param sessionObject
	 */
	protected void processLogout(SessionObject sessionObject) {
		// Do nothing
	}

	@Override
	public String isAuthenticated(String sessionId, HttpSession session) {
		if (isValidSession(sessionId, session)) {
			SessionObject sessionObject = getSessionObject(session);
			synchronized (sessionObject) {
				if (sessionObject.authenticated && sessionObject.userData != null) {
					return sessionObject.userData.getScreenname();
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
			SessionObject sessionObject = getSessionObject(session);
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
			SessionObject sessionObject = getSessionObject(session);
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
			SessionObject sessionObject = getSessionObject(session);
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
			SessionObject sessionObject = getSessionObject(session);
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
