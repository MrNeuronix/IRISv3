package ru.iris.noolite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.*;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.noolite.protocol.events.NooliteValueChanged;
import ru.iris.noolite.protocol.model.NooliteDevice;
import ru.iris.noolite4j.sender.PC1132;

@Component
@Profile("noolite")
@Qualifier("noolitetx")
@Scope("singleton")
public class NooliteTXController extends AbstractProtocolService<NooliteDevice> {

	private final ConfigLoader config;
	private final DeviceRegistry registry;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private PC1132 pc;

	@Autowired
	public NooliteTXController(ConfigLoader config,
	                           DeviceRegistry registry) {
		this.config = config;
		this.registry = registry;
	}

	@Override
	public void onStartup() {
		logger.info("NooliteRXController started");
		if(!config.loadPropertiesFormCfgDirectory("noolite"))
			logger.error("Cant load noolite-specific configs. Check noolite.property if exists");

		pc = new PC1132();
		pc.open();
	}

	@Override
	public void onShutdown() {
		logger.info("NooliteTXController stopping");
		logger.info("Closing Noolite PC device");
		pc.close();
		logger.info("Closed");
	}

	@Override
	public void subscribe() throws Exception  {
		addSubscription("command.device.noolite");
	}

	@Override
	public void run() {

	}

	@Override
	public NooliteDevice getDeviceByChannel(Short channel) {
		return (NooliteDevice) registry.getDevice(SourceProtocol.NOOLITE, channel);
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> {
			if (event.getData() instanceof DeviceOn) {
				DeviceOn n = (DeviceOn) event.getData();
				logger.info("Turn ON device on channel {}", n.getChannel());
				pc.turnOn(n.getChannel().byteValue());
				broadcast("event.device.noolite.rx", new NooliteValueChanged(n.getChannel(), (short) 255));
			} else if (event.getData() instanceof DeviceOff) {
				DeviceOff n = (DeviceOff) event.getData();
				logger.info("Turn OFF device on channel {}", n.getChannel());
				pc.turnOff(n.getChannel().byteValue());
				broadcast("event.device.noolite.rx", new NooliteValueChanged(n.getChannel(), (short) 0));
			} else if (event.getData() instanceof DeviceSetValue) {
				DeviceSetValue n = (DeviceSetValue) event.getData();
				if (n.getName().equals("level")) {
					logger.info("Set level {} on channel {}", n.getValue(), n.getChannel());
					pc.setLevel(n.getChannel().byteValue(), ((Short) n.getValue()).byteValue());
					broadcast("event.device.noolite.rx", new NooliteValueChanged(n.getChannel(), (Short) n.getValue()));
				} else {
					logger.info("Unknown value passed for NooliteTX: {} -> {}", n.getName(), n.getValue());
				}
			} else if (event.getData() instanceof DeviceAdd) {
				DeviceAdd n = (DeviceAdd) event.getData();
				logger.info("Incoming bind TX to channel {} request", n.getNode());
				pc.bindChannel(n.getNode().byteValue());
			} else if (event.getData() instanceof DeviceRemove) {
				DeviceRemove n = (DeviceRemove) event.getData();
				logger.info("Incoming unbind TX from channel {} request", n.getNode());
				pc.unbindChannel(n.getNode().byteValue());
			} else if (event.getData() instanceof DeviceRemoveAll) {
				logger.info("Incoming unbind all TX channels request");
			} else {
				// We received unknown request message. Lets make generic log entry.
				logger.info("Received unknown request for noolitetx service! Class: {}", event.getData().getClass());
			}
		};
	}
}
