package ru.iris.noolite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.ProtocolServiceLayer;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.noolite.protocol.events.*;
import ru.iris.noolite.protocol.model.NooliteDevice;
import ru.iris.noolite.protocol.model.NooliteDeviceValue;
import ru.iris.noolite4j.receiver.RX2164;
import ru.iris.noolite4j.watchers.BatteryState;
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.SensorType;
import ru.iris.noolite4j.watchers.Watcher;

@Component
@Profile("noolite")
@Qualifier("nooliterx")
@Scope("singleton")
public class NooliteRXController extends AbstractProtocolService<NooliteDevice> {

	private final EventBus r;
	private final ConfigLoader config;
	private final ProtocolServiceLayer<NooliteDevice, NooliteDeviceValue> service;

	private RX2164 rx;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public NooliteRXController(@Qualifier("nooliteDeviceService") ProtocolServiceLayer service, EventBus r, ConfigLoader config) {
		this.service = service;
		this.r = r;
		this.config = config;
	}

	@Override
	public void onStartup() {
		logger.info("NooliteRXController started");
		if(!config.loadPropertiesFormCfgDirectory("noolite"))
			logger.error("Cant load noolite-specific configs. Check noolite.property if exists");

		for(NooliteDevice device : service.getDevices()) {
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
		addSubscription("command.device.noolite.rx");
		addSubscription("event.device.noolite.rx");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> {
			if (event.getData() instanceof NooliteBindRXChannel) {
				NooliteBindRXChannel n = (NooliteBindRXChannel) event.getData();
				logger.debug("Get BindRXChannel advertisement (channel {})", n.getChannel());
				logger.info("Binding device to RX channel {}", n.getChannel());
				rx.bindChannel(n.getChannel());
			} else if (event.getData() instanceof NooliteUnbindRXChannel) {
				NooliteBindRXChannel n = (NooliteBindRXChannel) event.getData();
				logger.debug("Get UnbindRXChannel advertisement (channel {})",n.getChannel());
				logger.info("Unbinding device from RX channel {}", n.getChannel());
				rx.unbindChannel(n.getChannel());
			} else if (event.getData() instanceof NooliteUnbindAllRXChannels) {
				logger.debug("Get UnbindAllRXChannel advertisement");
				logger.info("Unbinding all RX channels");
				rx.unbindAllChannels();
			} else if (event.getData() instanceof NooliteValueChanged) {

				NooliteValueChanged n = (NooliteValueChanged) event.getData();
				logger.debug("Get ValueChanged advertisement (channel {})", n.getChannel());
				logger.info("Change device value event from TX, channel {}", n.getChannel());

				NooliteDevice device = devices.get(n.getChannel());

				if (device != null && device.getDeviceValues().get("level") != null) {
					device.getDeviceValues().get("level").setCurrentValue(n.getLevel());
					devices.replace(n.getChannel(), devices.get(n.getChannel()), device);
				}
			} else {
				// We received unknown request message. Lets make generic log entry.
				logger.info("Received unknown request for nooliterx service! Class: {}", event.getData().getClass());
			}
		};
	}

	@Override
	@Async
	public void run() {

		try {
			rx = new RX2164();
			rx.open();

			Watcher watcher = this::doWork;

			rx.addWatcher(watcher);
			rx.start();
		} catch (Throwable t) {
			logger.error("Noolite RX error!");
			t.printStackTrace();
		}
	}

	private void doWork(Notification notification) {

		boolean isNew = false;
		byte channel = notification.getChannel();
		SensorType sensor = (SensorType) notification.getValue("sensortype");

		logger.debug("Message to RX from channel " + channel);

		NooliteDevice device = devices.get(channel);

		if (device == null) {
			device = new NooliteDevice();
			device.setSource(SourceProtocol.NOOLITE);
			device.setHumanReadable("noolite/channel/" + channel);
			device.setState(State.ACTIVE);
			device.setType(DeviceType.UNKNOWN);
			device.setManufacturer("Nootechnika");
			device.setNode(channel);

			// device is sensor
			if (sensor != null) {
				switch (sensor) {
					case PT111:
						device.setType(DeviceType.TEMP_HUMI_SENSOR);
						device.setProductName("PT111");
						break;
					case PT112:
						device.setType(DeviceType.TEMP_SENSOR);
						device.setProductName("PT112");
						break;
					default:
						device.setType(DeviceType.UNKNOWN_SENSOR);
				}
			}

			isNew = true;
		}

		// turn off
		switch (notification.getType()) {
			case TURN_OFF:
				logger.info("Channel {}: Got OFF command", channel);
				updateValue(device, "level", 0, ValueType.BYTE);

				// device product name unkown
				if (device.getProductName().isEmpty()) {
					device.setProductName("Generic Switch");
					device.setType(DeviceType.BINARY_SWITCH);
				}

				broadcast("event.device.noolite.off", new NooliteDeviceOff(channel));
				break;

			case SLOW_TURN_OFF:
				logger.info("Channel {}: Got DIM command", channel);
				// we only know, that the user hold OFF button
				updateValue(device, "level", 0, ValueType.BYTE);

				broadcast("event.device.noolite.dim", new NooliteDeviceOn(channel));
				break;

			case TURN_ON:
				logger.info("Channel {}: Got ON command", channel);
				updateValue(device, "level", 255, ValueType.BYTE);

				// device product name unkown
				if (device.getType().equals(DeviceType.UNKNOWN)) {
					device.setProductName("Generic Switch");
					device.setType(DeviceType.BINARY_SWITCH);
				}

				broadcast("event.device.noolite.on", new NooliteDeviceOn(channel));
				break;

			case SLOW_TURN_ON:
				logger.info("Channel {}: Got BRIGHT command", channel);
				// we only know, that the user hold ON button
				updateValue(device, "level", 255, ValueType.BYTE);

				broadcast("event.device.noolite.bright", new NooliteDeviceOn(channel));
				break;

			case SET_LEVEL:
				logger.info("Channel {}: Got SETLEVEL command.", channel);
				updateValue(device, "level", notification.getValue("level"), ValueType.BYTE);

				// device product name unkown
				if (device.getProductName().isEmpty() || device.getType().equals(DeviceType.BINARY_SWITCH)) {
					device.setProductName("Generic Dimmer");
					device.setType(DeviceType.MULTILEVEL_SWITCH);
				}

				broadcast("event.device.noolite.setlevel", new NooliteDeviceSetLevel(channel, (byte) notification.getValue("level")));
				break;

			case STOP_DIM_BRIGHT:
				logger.info("Channel {}: Got STOPDIMBRIGHT command.", channel);

				broadcast("event.device.noolite.stopdimbright", new NooliteDeviceStopDimBright(channel));
				break;

			case TEMP_HUMI:
				BatteryState battery = (BatteryState) notification.getValue("battery");
				logger.info("Channel {}: Got TEMP_HUMI command.", channel);

				ru.iris.commons.protocol.enums.BatteryState batteryState;

				switch (battery) {
					case OK:
						batteryState = ru.iris.commons.protocol.enums.BatteryState.OK;
						break;
					case REPLACE:
						batteryState = ru.iris.commons.protocol.enums.BatteryState.LOW;
						break;
					default:
						batteryState = ru.iris.commons.protocol.enums.BatteryState.UNKNOWN;
				}

				updateValue(device, "temperature", notification.getValue("temp"), ValueType.DOUBLE);
				updateValue(device, "humidity", notification.getValue("humi"), ValueType.BYTE);
				updateValue(device, "battery", batteryState, ValueType.STRING);

				broadcast("event.device.noolite.temphumi", new NooliteDeviceTempHumi(
						channel,
						(double) notification.getValue("temp"),
						(int) notification.getValue("humi"),
						batteryState)
				);
				break;

			case BATTERY_LOW:
				logger.info("Channel {}: Got BATTERYLOW command.", channel);

				if (device.getType().equals(DeviceType.BINARY_SWITCH)) {
					device.setType(DeviceType.MOTION_SENSOR);
					device.setProductName("PM111");
				}

				broadcast("event.device.noolite.batterylow", new NooliteDeviceBatteryLow(channel));
				break;

			default:
				logger.info("Unknown command: {}", notification.getType().name());
		}

		// save/replace device in devices pool
		if (isNew) {
			devices.put(channel, service.saveIntoDatabase(device));
			broadcast("event.device.noolite.added", new NooliteDeviceAdded(notification));
		} else
			devices.replace(channel, device);
	}

	private void saveIntoDB() {
		for(NooliteDevice device : devices.values()) {
			devices.replace(device.getNode(), device, service.saveIntoDatabase(device));
		}
	}

	private void updateValue(NooliteDevice device, String label, Object value, ValueType type) {
		NooliteDeviceValue deviceValue = device.getDeviceValues().get(label);

		if (deviceValue == null) {
			deviceValue = new NooliteDeviceValue();
			deviceValue.setName(label);
			deviceValue.setCurrentValue(value);
			deviceValue.setType(type);
			deviceValue.setReadOnly(false);

			device.getDeviceValues().put(label, deviceValue);
		}
		else {
			deviceValue.setCurrentValue(value);
			device.getDeviceValues().replace(label, device.getDeviceValues().get(label), deviceValue);
		}

		service.addChange(deviceValue);
	}
}
