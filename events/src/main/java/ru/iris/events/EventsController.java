package ru.iris.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.bus.devices.DeviceProtocolEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.helpers.DeviceHelper;
import ru.iris.commons.helpers.SpeakHelper;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractService;
import ru.iris.events.manager.RuleTriggerManager;
import ru.iris.events.manager.ScriptManager;
import ru.iris.events.types.Rule;
import ru.iris.events.types.TriggerType;

@Component
@Qualifier("events")
@Profile("events")
@Slf4j
public class EventsController extends AbstractService {

    private final ConfigLoader config;
    private final DeviceRegistry registry;
    private final RuleTriggerManager triggerManager;
    private ScriptManager scriptManager;
    private SpeakHelper speakHelper;
    private DeviceHelper deviceHelper;

    @Autowired
    public EventsController(ConfigLoader config, DeviceRegistry registry, RuleTriggerManager triggerManager,
                            SpeakHelper speakHelper, DeviceHelper deviceHelper) {
        this.config = config;
        this.registry = registry;
        this.triggerManager = triggerManager;
        this.speakHelper = speakHelper;
        this.deviceHelper = deviceHelper;
    }

    @Override
    public void onStartup() throws InterruptedException {
        logger.info("EventsController starting");
        scriptManager = new ScriptManager(triggerManager, config, registry, speakHelper, deviceHelper);
        logger.info("EventsController started");

        runStartupRules();
    }

    @Override
    public void onShutdown() {
        logger.info("EventsController stopping");
    }

    @Override
    public Consumer<Event<?>> handleMessage() throws Exception {
        return event -> {
            if (event.getData() instanceof DeviceProtocolEvent) {

                DeviceProtocolEvent e = (DeviceProtocolEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.CHANGE, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.CHANGE, device));

            } else if (event.getData() instanceof DeviceChangeEvent) {

                DeviceChangeEvent e = (DeviceChangeEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.CHANGE, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.CHANGE, device));

            } else if (event.getData() instanceof DeviceCommandEvent) {

                DeviceCommandEvent e = (DeviceCommandEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.COMMAND, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.COMMAND, device));

            } else {
                // We received unknown request message. Lets make generic log entry.
                logger.info("Received unknown request for events service! Class: {}", event.getData().getClass());
            }
        };
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("event.*");
        addSubscription("command.*");
    }

    @Override
    public void run() {
    }

    private void runStartupRules() {
        if (triggerManager != null) {
            Iterable<Rule> startupRules = triggerManager.getRules(TriggerType.STARTUP);
            scriptManager.executeRules(startupRules, new ru.iris.events.types.Event(TriggerType.STARTUP, null));
        }
    }
}
