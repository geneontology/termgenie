package org.bbop.termgenie.services;

import java.util.Random;

import org.apache.log4j.Logger;


public class SessionHandlerImpl implements SessionHandler {

	private static final Logger logger = Logger.getLogger(SessionHandlerImpl.class);
	
	// TODO replace this with a proper session key generation
	private static final Random random = new Random(-1);
	
	@Override
	public String createSession() {
		long long1 = random.nextLong();
		long long2 = random.nextLong();
		String sessionId = Long.toString(long1, Character.MAX_RADIX)+
			Long.toString(long2, Character.MAX_RADIX);
		logger.info("Creating new session: "+sessionId);
		return sessionId;
	}

	@Override
	public boolean login(String sessionId, String username, String password) {
		// TODO add a proper user management to this.
		logger.info("Trying to login.");
		if ("test".equals(username) && "123456".equals(password)) {
			return true;
		}
		return false;
	}

	@Override
	public void logout(String sessionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keepSessionAlive(String sessionId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getValue(String sessionId, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(String sessionId, String key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getValues(String sessionId, String[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValues(String sessionId, String[] keys, String[] values) {
		// TODO Auto-generated method stub
		
	}


}
