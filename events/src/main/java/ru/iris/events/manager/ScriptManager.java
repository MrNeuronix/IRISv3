package ru.iris.events.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.events.types.*;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ScriptManager {

	private static final Logger logger = LoggerFactory.getLogger(ScriptManager.class);
	private final ConfigLoader config;

	private HashMap<String, Script> scripts = new HashMap<>();
	private HashMap<Rule, Script> ruleMap = new HashMap<>();

	private RuleTriggerManager triggerManager;
	private DeviceRegistry registry;

	private Thread scriptUpdateWatcher;

	private static ScriptManager instance;

	public ScriptManager(RuleTriggerManager triggerManager, ConfigLoader config, DeviceRegistry itemRegistry) {
		this.triggerManager = triggerManager;
		this.config = config;
		instance = this;
		logger.info("Available engines:");
		for (ScriptEngineFactory f : new ScriptEngineManager().getEngineFactories()) {
			logger.info(f.getEngineName());
		}

		this.setItemRegistry(itemRegistry);

		if(!config.loadPropertiesFormCfgDirectory("events"))
			logger.error("Cant load events-specific configs. Check events.properties if exists");

		File folder = getFolder(config.get("scriptsDirectory"));

		if (folder.exists() && folder.isDirectory()) {
			loadScripts(folder);

			scriptUpdateWatcher = new Thread(new ScriptUpdateWatcher(this, folder));
			scriptUpdateWatcher.start();
		} else {
			logger.warn("Script directory: scripts missing, no scripts will be added!");
		}
	}

	public void loadScripts(File folder) {
		for (File file : folder.listFiles()) {
			loadScript(file);
		}
	}

	private Script loadScript(File file) {
		Script script = null;
		try {
			//Filtering Directories and not usable Files
			if(!file.isFile() || file.getName().startsWith(".") || getFileExtension(file) == null){
				return null;
			}
			script = new Script(this, file, registry);
			if(script.getEngine() == null){
				logger.warn("No Engine found for File: {}", file.getName());
				return null;
			}else{
				logger.info("Engine found for File: {}", file.getName());
				scripts.put(file.getName(), script);
				List<Rule> newRules = script.getRules();
				for (Rule rule : newRules) {
					ruleMap.put(rule, script);
				}

				// add all rules to the needed triggers
				triggerManager.addRuleModel(newRules);
			}

		} catch(NoSuchMethodException e) {
			logger.error("Script file misses mandotary function: getRules()", e);
		} catch (FileNotFoundException e) {
			logger.error("script file not found", e);
		} catch (ScriptException e) {
			logger.error("script exception", e);
		} catch (Exception e) {
			logger.error("unknown exception", e);
		}

		return script;
	}

	public static ScriptManager getInstance() {
		return instance;
	}

	public Collection<Rule> getAllRules() {
		return ruleMap.keySet();
	}

	public DeviceRegistry getItemRegistry() {
		return registry;
	}

	public void setItemRegistry(DeviceRegistry itemRegistry) {
		this.registry = itemRegistry;
	}

	public synchronized void executeRules(Rule[] rules, Event event) {
		for (Rule rule : rules) {
			ruleMap.get(rule).executeRule(rule, event);
		}
	}

	public synchronized void executeRules(Iterable<Rule> rules, Event event) {
		for (Rule rule : rules) {
			ruleMap.get(rule).executeRule(rule, event);
		}
	}

	/**
	 * returns the {@link File} object for a given foldername
	 * 
	 * @param foldername
	 *            the foldername to get the {@link File} for
	 * @return the corresponding {@link File}
	 */
	private File getFolder(String foldername) {
		return new File("." + File.separator + foldername);
	}

	public Script getScript(Rule rule) {
		return ruleMap.get(rule);
	}

	private String getFileExtension(File file) {
		String extension = null;
		if (file.getName().contains(".")) {
			String name = file.getName();
			extension = name.substring(name.lastIndexOf('.') + 1, name.length());
		}
		return extension;
	}

	public void scriptsChanged(List<File> addedScripts, List<File> removedScripts, List<File> modifiedScripts) {

		for (File scriptFile : removedScripts) {
			removeScript(scriptFile.getName());
		}

		for (File scriptFile : addedScripts) {
			Script script = loadScript(scriptFile);
			runStartupRules(script);
		}

		for (File scriptFile : modifiedScripts) {
			removeScript(scriptFile.getName());
			Script script = loadScript(scriptFile);
			runStartupRules(script);
		}
	}

	private void runStartupRules(Script script) {
		if (script != null) {
			List<Rule> toTrigger = new ArrayList<>();
			for (Rule rule : script.getRules()) {
				for (EventTrigger trigger : rule.getEventTrigger()) {
					if (trigger instanceof StartupTrigger) {
						toTrigger.add(rule);
						break;
					}
				}
			}
			if (toTrigger.size() > 0)
				executeRules(toTrigger, new Event(TriggerType.STARTUP, null));
		}
	}

	private void removeScript(String scriptName) {
		if(scripts.containsKey(scriptName)) {
			Script script = scripts.remove(scriptName);

			List<Rule> allRules = script.getRules();

			triggerManager.removeRuleModel(allRules);
			for (Rule rule : allRules) {
				ruleMap.remove(rule);
			}
		}
	}

}
