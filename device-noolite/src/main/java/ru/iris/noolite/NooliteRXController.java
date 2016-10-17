package ru.iris.noolite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.Protocol;
import ru.iris.commons.service.AbstractService;
import ru.iris.noolite.protocol.events.NooliteValueChanged;
import ru.iris.noolite.protocol.model.NooliteDevice;
import ru.iris.noolite.protocol.service.NooliteProtoService;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("noolite-rx")
@Qualifier("noolite-rx")
public class NooliteRXController extends AbstractService implements Protocol {

	private final EventBus r;
	private final ConfigLoader config;
	private final NooliteProtoService service;
	private Map<Short, NooliteDevice> devices = new HashMap<>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public NooliteRXController(NooliteProtoService service, EventBus r, ConfigLoader config) {
		this.service = service;
		this.r = r;
		this.config = config;
	}

	@Override
	public void onStartup() {
		logger.info("NooliteRXController started");
		if(!config.loadPropertiesFormCfgDirectory("noolite"))
			logger.error("Cant load noolite-specific configs. Check noolite.property if exists");

		for(NooliteDevice device : service.getNooliteDevices()) {
			devices.put(device.getNode(), device);
		}

		logger.debug("Load {} Noolite devices from database", devices.size());
	}

	@Override
	public void onShutdown() {
		logger.info("NooliteRXController stopping");
		logger.info("Saving Noolite devices state into database");
		saveIntoDB();
		logger.info("Saved");
	}

	@Override
	public void subscribe() throws Exception  {
		addSubscription("command.device.*");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> {
			if (event.getData() instanceof NooliteValueChanged) {

			} else {
				// We received unknown request message. Lets make generic log entry.
				logger.info("Received unknown request for nooliterx service! Class: {}", event.getData().getClass());
			}
		};
	}

	@Override
	@Async
	public void run() {
		// impl
	}

	private void saveIntoDB() {
		for(NooliteDevice device : devices.values()) {
			devices.replace(device.getNode(), device, service.saveIntoDatabase(device));
		}
	}

}
