package ru.iris.events.manager;

import lombok.extern.slf4j.Slf4j;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.helpers.DeviceHelper;
import ru.iris.commons.helpers.SpeakHelper;
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

@Slf4j
public class ScriptManager {

    private static ScriptManager instance;
    private HashMap<String, Script> scripts = new HashMap<>();
    private HashMap<Rule, Script> ruleMap = new HashMap<>();
    private RuleTriggerManager triggerManager;
    private DeviceRegistry registry;
    private SpeakHelper speakHelper;
    private DeviceHelper deviceHelper;

    public ScriptManager(RuleTriggerManager triggerManager, ConfigLoader config, DeviceRegistry itemRegistry,
                         SpeakHelper speakHelper, DeviceHelper deviceHelper) {
        this.triggerManager = triggerManager;
        this.speakHelper = speakHelper;
        this.deviceHelper = deviceHelper;
        instance = this;
        logger.info("Available engines:");
        for (ScriptEngineFactory f : new ScriptEngineManager().getEngineFactories()) {
            logger.info(f.getEngineName());
        }

        this.setItemRegistry(itemRegistry);
        File folder = getFolder(config.get("scriptsDirectory"));

        if (folder.exists() && folder.isDirectory()) {
            loadScripts(folder);

            Thread scriptUpdateWatcher = new Thread(new ScriptUpdateWatcher(this, folder));
            scriptUpdateWatcher.start();
        } else {
            logger.warn("Script directory: scripts missing, no scripts will be added!");
        }
    }

    public static ScriptManager getInstance() {
        return instance;
    }

    private void loadScripts(File folder) {
        if (folder != null)
            for (File file : folder.listFiles()) {
                loadScript(file);
            }
    }

    private Script loadScript(File file) {
        Script script = null;
        try {
            //Filtering Directories and not usable Files
            if (!file.isFile() || file.getName().startsWith(".") || getFileExtension(file) == null) {
                return null;
            }
            script = new Script(file, registry, speakHelper, deviceHelper);
            if (script.getEngine() == null) {
                logger.warn("No Engine found for File: {}", file.getName());
                return null;
            } else {
                logger.info("Engine found for File: {}", file.getName());
                scripts.put(file.getName(), script);
                List<Rule> newRules = script.getRules();
                for (Rule rule : newRules) {
                    ruleMap.put(rule, script);
                }

                // add all rules to the needed triggers
                triggerManager.addRuleModel(newRules);
            }

        } catch (NoSuchMethodException e) {
            logger.error("Script file misses mandatory function: getRules()", e);
        } catch (FileNotFoundException e) {
            logger.error("script file not found", e);
        } catch (ScriptException e) {
            logger.error("script exception", e);
        } catch (Exception e) {
            logger.error("unknown exception", e);
        }

        return script;
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
     * @param foldername the foldername to get the {@link File} for
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
                executeRules(toTrigger, new Event(TriggerType.STARTUP, null, null));
        }
    }

    private void removeScript(String scriptName) {
        if (scripts.containsKey(scriptName)) {
            Script script = scripts.remove(scriptName);

            List<Rule> allRules = script.getRules();

            triggerManager.removeRuleModel(allRules);
            for (Rule rule : allRules) {
                ruleMap.remove(rule);
            }
        }
    }

}
