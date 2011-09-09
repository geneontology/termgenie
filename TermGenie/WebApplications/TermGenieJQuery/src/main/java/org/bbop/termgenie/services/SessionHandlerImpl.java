package org.bbop.termgenie.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SessionHandlerImpl implements SessionHandler {

	private static final Logger logger = Logger.getLogger(SessionHandlerImpl.class);

	// TODO replace this with a proper session key generation
	private static final Random random = new Random(System.currentTimeMillis());

	private final Map<Long, SessionObject<String, String>> sessions;
	private final long timeoutMilliSeconds;
	private final long gracePeriodMilliSeconds;

	@Inject
	public SessionHandlerImpl(@Named("SessionHandlerImplTimeout") long timeout,
			@Named("SessionHandlerImplTimeUnit") TimeUnit unit)
	{
		super();
		timeoutMilliSeconds = TimeUnit.MILLISECONDS.convert(timeout, unit);
		gracePeriodMilliSeconds = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES);
		sessions = new ConcurrentHashMap<Long, SessionObject<String, String>>();
		Runnable command = new Runnable() {

			@Override
			public void run() {
				cleanSessions();
			}
		};
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		long period = timeout / 4; // do checks 4-times as often as the timeout.
		scheduler.scheduleWithFixedDelay(command, period, period, unit);
	}

	private static class SessionObject<K, V> {

		private Date lastUse;
		private boolean authenticated = false;
		private final Map<K, V> values = new ConcurrentHashMap<K, V>();

		SessionObject() {
			lastUse = new Date();
		}

		V put(K key, V value) {
			updateDate();
			return values.put(key, value);
		}

		V get(K key) {
			updateDate();
			return values.get(key);
		}
		
		V remove(K key) {
			updateDate();
			return values.remove(key);
		}

		synchronized void updateDate() {
			lastUse = new Date();
		}

		synchronized Date getLastUse() {
			return lastUse;
		}
	}

	private void cleanSessions() {
		synchronized (sessions) {
			if (!sessions.isEmpty()) {
				Set<Long> keys = new HashSet<Long>(sessions.keySet());
				Date timeout = new Date(System.currentTimeMillis() - (timeoutMilliSeconds + gracePeriodMilliSeconds));
				for (Long key : keys) {
					SessionObject<String, String> sessionObject = sessions.get(key);
					if (sessionObject == null) {
						sessions.remove(key);
					}
					else {
						Date lastUse = sessionObject.getLastUse();
						if (lastUse.before(timeout)) {
							logger.info("Removing session after timeout: " + toSessionId(key));
							sessions.remove(key);
						}
					}
				}
			}
		}
	}

	@Override
	public String createSession() {
		Long key = Long.valueOf(random.nextLong());

		synchronized (sessions) {
			while ((sessions.get(key)) != null) {
				logger.warn("Key collision for session creation.");
				key = Long.valueOf(random.nextLong());
			}
			String sessionId = createSession(key);
			return sessionId;
		}
	}

	private String createSession(Long key) {
		String sessionId = toSessionId(key);
		logger.info("Creating new session: " + sessionId);
		SessionObject<String, String> sessionObject = new SessionObject<String, String>();
		sessions.put(key, sessionObject);
		return sessionId;
	}

	private String toSessionId(Long key) {
		return Long.toString(key.longValue(), Character.MAX_RADIX);
	}

	private Long toKey(String sessionId) {
		try {
			return Long.parseLong(sessionId, Character.MAX_RADIX);
		} catch (NumberFormatException exception) {
			logger.info("Could not parse sessionId: " + sessionId, exception);
			return null;
		}
	}

	@Override
	public boolean isValidSession(String sessionId) {
		Long key = toKey(sessionId);
		return isVaidSession(key);
	}

	private boolean isVaidSession(Long key) {
		if (key != null) {
			SessionObject<String, String> sessionObject = sessions.get(key);
			return sessionObject != null;
		}
		return false;
	}

	private boolean updateSession(Long key) {
		if (key != null) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(key);
				if (sessionObject == null) {
					createSession(key);
				}
				else {
					sessionObject.updateDate();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean login(String sessionId, String username, String password) {
		Long key = toKey(sessionId);
		if (!updateSession(key)) {
			return false;
		}
		// TODO add a proper user management to this.
		logger.info("Trying to login.");
		if ("test".equals(username) && "123456".equals(password)) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(key);
				sessionObject.authenticated = true;
				sessionObject.put(USER_NAME, username);
			}
			return true;
		}
		return false;
	}

	@Override
	public void logout(String sessionId) {
		Long key = toKey(sessionId);
		if (!updateSession(key)) {
			return;
		}
		synchronized (sessions) {
			SessionObject<String, String> sessionObject = sessions.get(key);
			sessionObject.authenticated = false;
			sessionObject.remove(USER_NAME);
		}
	}

	@Override
	public boolean isAuthenticated(String sessionId) {
		Long key = toKey(sessionId);
		if (!updateSession(key)) {
			return false;
		}
		synchronized (sessions) {
			SessionObject<String, String> sessionObject = sessions.get(key);
			if(sessionObject.authenticated) {
				return sessionObject.get(USER_NAME) != null;
			}
		}
		return false;
	}

	@Override
	public boolean keepSessionAlive(String sessionId) {
		Long key = toKey(sessionId);
		return updateSession(key);
	}

	@Override
	public String getValue(String sessionId, String key) {
		Long sessionKey = toKey(sessionId);
		if (sessionKey != null) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(sessionKey);
				if (sessionObject != null) {
					String value = sessionObject.get(key);
					return value;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getValues(String sessionId, String[] keys) {
		Long sessionKey = toKey(sessionId);
		if (sessionKey != null) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(sessionKey);
				if (sessionObject != null) {
					String[] results = new String[keys.length];
					for (int i = 0; i < keys.length; i++) {
						String key = keys[i];
						results[i] = sessionObject.get(key);
					}
					return results;
				}
			}
		}
		return null;
	}

	@Override
	public boolean setValue(String sessionId, String key, String value) {
		Long sessionKey = toKey(sessionId);
		if (sessionKey != null) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(sessionKey);
				if (sessionObject != null) {
					sessionObject.put(key, value);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean setValues(String sessionId, String[] keys, String[] values) {
		Long sessionKey = toKey(sessionId);
		if (sessionKey != null) {
			synchronized (sessions) {
				SessionObject<String, String> sessionObject = sessions.get(sessionKey);
				if (sessionObject != null) {
					for (int i = 0; i < keys.length; i++) {
						sessionObject.put(keys[i], values[i]);
					}
					return true;
				}
			}
		}
		return false;
	}

}
