package ru.iris.events.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import ru.iris.commons.helpers.DeviceHelper;
import ru.iris.commons.helpers.SpeakHelper;
import ru.iris.commons.registry.DeviceRegistry;
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

@Slf4j
public class Script {
    private ArrayList<Rule> rules = new ArrayList<>();
    private ScriptEngine engine = null;
    private DeviceRegistry registry;
    private String fileName;
    private SpeakHelper speakHelper;
    private DeviceHelper deviceHelper;

    public Script(File file, DeviceRegistry registry,
                  SpeakHelper speakHelper, DeviceHelper deviceHelper) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        this.fileName = file.getName();
        this.registry = registry;
        this.speakHelper = speakHelper;
        this.deviceHelper = deviceHelper;
        loadScript(file);
    }

    private void loadScript(File file) throws FileNotFoundException, ScriptException, NoSuchMethodException {
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
        if (engine.getFactory().getEngineName().toLowerCase().endsWith("nashorn")) {
            initializeNashornGlobals();
        } else {
            initializeGeneralGlobals();
        }
    }

    private void initializeNashornGlobals() {
        try {

            logger.info("initializeSciptGlobals for : " + engine.getFactory().getEngineName());

            engine.put("DeviceRegistry", registry);
            engine.put("DeviceHelper", deviceHelper);
            engine.put("SpeakHelper", speakHelper);

            engine.eval("RuleSet 				= Java.type('ru.iris.events.types.RuleSet'),\n"
                    + "Rule 					= Java.type('ru.iris.events.types.Rule'),\n"
                    + "ChangedEventTrigger 	= Java.type('ru.iris.events.types.ChangedEventTrigger'),\n"
                    + "CommandEventTrigger 	= Java.type('ru.iris.events.types.CommandEventTrigger'),\n"
                    + "Event 				= Java.type('ru.iris.events.types.Event'),\n"
                    + "EventTrigger			= Java.type('ru.iris.events.types.EventTrigger'),\n"
                    + "ShutdownTrigger 		= Java.type('ru.iris.events.types.ShutdownTrigger'),\n"
                    + "StartupTrigger 		= Java.type('ru.iris.events.types.StartupTrigger'),\n"
                    + "TimerTrigger 			= Java.type('ru.iris.events.types.TimerTrigger'),\n"
                    + "TriggerType 			= Java.type('ru.iris.events.types.TriggerType'),\n"
                    + "URLEncoder 			= Java.type('java.net.URLEncoder'),\n"

                    // Devices
                    + "Device 			= Java.type('ru.iris.commons.database.model.Device'),\n"
                    + "DeviceValue 			= Java.type('ru.iris.commons.database.model.DeviceValue'),\n"
                    + "DeviceValueChange 			= Java.type('ru.iris.commons.database.model.DeviceValueChange'),\n"

                    + "Zone			= Java.type('ru.iris.commons.protocol.Zone'),\n"

                    + "SourceProtocol			= Java.type('ru.iris.commons.protocol.enums.SourceProtocol'),\n"

                    // Helpers
                    //+"SpeakHelper			= Java.type('ru.iris.commons.helpers.SpeakHelper'),\n"
                    //+"DeviceHelper		= Java.type('ru.iris.commons.helpers.DeviceHelper'),\n"

                    //System
                    + "FileUtils 			= Java.type('org.apache.commons.io.FileUtils'),\n"
                    + "FilenameUtils			= Java.type('org.apache.commons.io.FilenameUtils'),\n"
                    + "File 					= Java.type('java.io.File'),\n"

                    + "engine				= 'javascript';\n"
            );
        } catch (ScriptException e) {
            logger.error("ScriptException in initializeSciptGlobals while importing default-classes: ", e);
        }
    }

    private void initializeGeneralGlobals() {
        engine.put("RuleSet", RuleSet.class);
        engine.put("Rule", Rule.class);
        engine.put("ChangedEventTrigger", ChangedEventTrigger.class);
        engine.put("CommandEventTrigger", CommandEventTrigger.class);
        engine.put("Event", Event.class);
        engine.put("EventTrigger", EventTrigger.class);
        engine.put("ShutdownTrigger", ShutdownTrigger.class);
        engine.put("StartupTrigger", StartupTrigger.class);
        engine.put("TimerTrigger", TimerTrigger.class);
        engine.put("TriggerType", TriggerType.class);
        engine.put("StringUtils", StringUtils.class);
        engine.put("URLEncoder", URLEncoder.class);
        engine.put("FileUtils", FileUtils.class);
        engine.put("FilenameUtils", FilenameUtils.class);
        engine.put("File", File.class);
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
