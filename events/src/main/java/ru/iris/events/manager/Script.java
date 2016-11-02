package ru.iris.events.manager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.iris.events.types.*;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A Script holds information about a script-file.
 * 
 * @author Simon Merschjohann
 * @author Helmut Lehmeyer
 * @author Nikolay Viguro
 */

public class Script {

	private static final Logger logger = LoggerFactory.getLogger(Script.class);
	private ArrayList<Rule> rules = new ArrayList<>();
	private ScriptManager scriptManager;
	private ScriptEngine engine = null;
	private String fileName;

	public Script(ScriptManager scriptManager, File file) throws FileNotFoundException, ScriptException, NoSuchMethodException {
		this.scriptManager = scriptManager;
		this.fileName = file.getName();
		loadScript(file);
	}

	public void loadScript(File file) throws FileNotFoundException, ScriptException, NoSuchMethodException {
		logger.info("Loading Script " + file.getName());
		String extension = getFileExtension(file);
		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByExtension(extension);
		if (engine != null) {
			logger.info("EngineName: " + engine.getFactory().getEngineName());
			initializeSciptGlobals();
			engine.eval(new FileReader(file));
			Invocable inv = (Invocable) engine;
			RuleSet ruleSet = (RuleSet) inv.invokeFunction("getRules");
			rules.addAll(ruleSet.getRules());
		}
	}

	private void initializeSciptGlobals() {
		if(engine.getFactory().getEngineName().toLowerCase().endsWith("nashorn")){
			initializeNashornGlobals();
		}else{
			initializeGeneralGlobals();
		}
	}

	private void initializeNashornGlobals() {
		try {
			
			logger.info("initializeSciptGlobals for : " + engine.getFactory().getEngineName());
			engine.eval("RuleSet 				= Java.type('org.openhab.core.jsr223.internal.shared.RuleSet'),\n"
				+"Rule 					= Java.type('org.openhab.core.jsr223.internal.shared.Rule'),\n"
				+"ChangedEventTrigger 	= Java.type('org.openhab.core.jsr223.internal.shared.ChangedEventTrigger'),\n"
				+"CommandEventTrigger 	= Java.type('org.openhab.core.jsr223.internal.shared.CommandEventTrigger'),\n"
				+"Event 				= Java.type('org.openhab.core.jsr223.internal.shared.Event'),\n"
				+"EventTrigger			= Java.type('org.openhab.core.jsr223.internal.shared.EventTrigger'),\n"
				+"ShutdownTrigger 		= Java.type('org.openhab.core.jsr223.internal.shared.ShutdownTrigger'),\n"
				+"StartupTrigger 		= Java.type('org.openhab.core.jsr223.internal.shared.StartupTrigger'),\n"
				+"TimerTrigger 			= Java.type('org.openhab.core.jsr223.internal.shared.TimerTrigger'),\n"
				+"TriggerType 			= Java.type('org.openhab.core.jsr223.internal.shared.TriggerType'),\n"
				+"URLEncoder 			= Java.type('java.net.URLEncoder'),\n"
				
				//System
				+"FileUtils 			= Java.type('org.apache.commons.io.FileUtils'),\n"
				+"FilenameUtils			= Java.type('org.apache.commons.io.FilenameUtils'),\n"
				+"File 					= Java.type('java.io.File'),\n"
				
				+"engine				= 'javascript';\n"
			);
		} catch (ScriptException e) {
			logger.error("ScriptException in initializeSciptGlobals while importing default-classes: ", e);
		}
	}
	
	private void initializeGeneralGlobals() {
		engine.put("RuleSet", 				RuleSet.class);
		engine.put("Rule", 					Rule.class);
		engine.put("ChangedEventTrigger", 	ChangedEventTrigger.class);
		engine.put("UpdatedEventTrigger", 	UpdatedEventTrigger.class);
		engine.put("CommandEventTrigger", 	CommandEventTrigger.class);
		engine.put("Event", 				Event.class);
		engine.put("EventTrigger", 			EventTrigger.class);
		engine.put("ShutdownTrigger", 		ShutdownTrigger.class);
		engine.put("StartupTrigger", 		StartupTrigger.class);
		engine.put("TimerTrigger", 			TimerTrigger.class);
		engine.put("TriggerType", 			TriggerType.class);
		engine.put("StringUtils", 			StringUtils.class);
		engine.put("URLEncoder", 			URLEncoder.class);	
		engine.put("FileUtils", 			FileUtils.class);	
		engine.put("FilenameUtils", 		FilenameUtils.class);	
		engine.put("File", 					File.class);
	}

	private String getFileExtension(File file) {
		String extension = null;
		if (file.getName().contains(".")) {
			String name = file.getName();
			extension = name.substring(name.lastIndexOf('.') + 1, name.length());
		}
		return extension;
	}

	public List<Rule> getRules() {
		return this.rules;
	}

	public void executeRule(Rule rule, Event event) {
		Thread t = new Thread(new RuleExecutionRunnable(rule, event));
		t.start();
	}

	public String getFileName() {
		return fileName;
	}

	public ScriptEngine getEngine() {
		return engine;
	}

}
