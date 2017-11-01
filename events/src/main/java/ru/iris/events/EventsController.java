package ru.iris.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.bus.devices.DeviceProtocolEvent;
import ru.iris.commons.bus.service.ServiceEvent;
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

	      if (!config.loadPropertiesFormCfgDirectory("events"))
		      logger.error("Can't load events-specific configs. Check events.properties if exists");
    }

    @Override
    public void onShutdown() {
        logger.info("EventsController stopping");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("event.*");
        addSubscription("command.*");
        addSubscription("service.events");
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Consumer<Event<?>> handleMessage() throws Exception {
        return event -> {
            // skip events while manager not initialized yet
            if (scriptManager == null) {
                return;
            }

            if (event.getData() instanceof ServiceEvent) {
                ServiceEvent e = (ServiceEvent) event.getData();
                String label = e.getLabel() == null ? "" : e.getLabel();

                if (disabled && label.equals("ServiceOn")) {
                    disabled = false;
                }
                if (!disabled && label.equals("ServiceOff")) {
                    disabled = true;
                }
            }

            // if service disabled at this moment - do nothing
            if (disabled) {
                return;
            }

            if (event.getData() instanceof DeviceProtocolEvent) {
                DeviceProtocolEvent e = (DeviceProtocolEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.CHANGE, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.CHANGE, device, event.getKey().toString()));
            } else if (event.getData() instanceof DeviceChangeEvent) {
                DeviceChangeEvent e = (DeviceChangeEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.CHANGE, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.CHANGE, device, event.getKey().toString()));
            } else if (event.getData() instanceof DeviceCommandEvent) {
                DeviceCommandEvent e = (DeviceCommandEvent) event.getData();
                Device device = registry.getDevice(e.getProtocol(), e.getChannel());
                Iterable<Rule> rules = triggerManager.getRules(TriggerType.COMMAND, device);
                scriptManager.executeRules(rules, new ru.iris.events.types.Event(TriggerType.COMMAND, device, event.getKey().toString()));
            } else {
                // We received unknown request message. Lets make generic log entry.
                logger.info("Received unknown request for events service! Class: {}", event.getData().getClass());
            }
        };
    }

    @Override
    @Async
    public void run() {
		    int delay = Integer.parseInt(config.get("startupDelayInSeconds"));
		    logger.info("Startup delay is {} seconds", delay);

		    try {
			    Thread.sleep(delay * 1000L);
		    } catch (InterruptedException e) {
			    logger.error("", e);
		    }

	      scriptManager = new ScriptManager(triggerManager, config, registry, speakHelper, deviceHelper);
		    logger.info("EventsController started");

		    logger.info("EventsController running startup scripts");
		    runStartupRules();
		    logger.info("EventsController done running startup scripts");
    }

    private void runStartupRules() {
        if (triggerManager != null) {
            Iterable<Rule> startupRules = triggerManager.getRules(TriggerType.STARTUP);
            scriptManager.executeRules(startupRules, new ru.iris.events.types.Event(TriggerType.STARTUP, null, null));
        }
    }
}
