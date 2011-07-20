package org.bbop.termgenie.rules;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSEngineManager {

	private final ScriptEngineManager manager;

	public JSEngineManager() {
		manager = new ScriptEngineManager();
	}
	
	public ScriptEngine getEngine() {
		return manager.getEngineByName("js"); 
	}
}
