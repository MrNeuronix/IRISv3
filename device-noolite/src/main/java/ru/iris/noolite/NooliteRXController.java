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
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.service.AbstractService;
import ru.iris.noolite.protocol.events.*;
import ru.iris.noolite.protocol.model.NooliteDevice;
import ru.iris.noolite.protocol.model.NooliteDeviceValue;
import ru.iris.noolite.protocol.service.NooliteProtoService;
import ru.iris.noolite4j.receiver.RX2164;
import ru.iris.noolite4j.watchers.BatteryState;
import ru.iris.noolite4j.watchers.SensorType;
import ru.iris.noolite4j.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("noolite-rx")
@Qualifier("noolite-rx")
public class NooliteRXController extends AbstractService implements Protocol {

	private final EventBus r;
	private final ConfigLoader config;
	private final NooliteProtoService service;
	private Map<Byte, NooliteDevice> devices = new HashMap<>();

	private RX2164 rx;

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
			devices.put(Byte.valueOf(String.valueOf(device.getNode())), device);
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
		addSubscription("command.device.noolite.*");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> {
			if (event.getData() instanceof NooliteBindRXChannel) {
				NooliteBindRXChannel n = (NooliteBindRXChannel) event.getData();
				logger.debug("Get BindRXChannel advertisement (channel {})", n.getChannel());
				logger.info("Binding device to RX channel {}", n.getChannel());
				rx.bindChannel(n.getChannel());
			}
			else if (event.getData() instanceof NooliteUnbindRXChannel) {
				NooliteBindRXChannel n = (NooliteBindRXChannel) event.getData();
				logger.debug("Get UnbindRXChannel advertisement (channel {})",n.getChannel());
				logger.info("Unbinding device from RX channel {}", n.getChannel());
				rx.unbindChannel(n.getChannel());
			}
			else if (event.getData() instanceof NooliteUnbindAllRXChannels) {
				logger.debug("Get UnbindAllRXChannel advertisement");
				logger.info("Unbinding all RX channels");
				rx.unbindAllChannels();
			}
			else {
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

			Watcher watcher = notification -> {

				byte channel = notification.getChannel();
				SensorType sensor = (SensorType) notification.getValue("sensortype");

				NooliteDevice device = devices.get(channel);

				if (device == null) {
					device = new NooliteDevice();
					device.setSource(SourceProtocol.NOOLITE);
					device.setHumanReadable("noolite/channel/" + channel);
					device.setState(State.ACTIVE);
					device.setType(DeviceType.UNKNOWN);
					device.setManufacturer("Nootechnika");
					device.setNode((short) (1000 + channel));

					// device is not sensor
					if (sensor == null) {
						device.setType(DeviceType.BINARY_SWITCH);
						device.getDeviceValues().put("channel", new NooliteDeviceValue("channel", channel));
					}
					else {
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
						device.getDeviceValues().put("channel", new NooliteDeviceValue("channel", channel));
					}
				}

				Map<String, Object> params = new HashMap<>();

				// turn off
				switch (notification.getType()) {
					case TURN_OFF:
						logger.info("Channel {}: Got OFF command", channel);
						updateValue(device, "level", 0);

						// device product name unkown
						if (device.getProductName().isEmpty()) {
							device.setProductName("Generic Switch");
							device.setType(DeviceType.BINARY_SWITCH);
						}

						broadcast("event.device.noolite", new NooliteDeviceOff(channel));
						break;

					case SLOW_TURN_OFF:
						logger.info("Channel {}: Got DIM command", channel);
						// we only know, that the user hold OFF button
						updateValue(device, "level", 0);

						broadcast("event.device.noolite", new NooliteDeviceOn(channel));
						break;

					case TURN_ON:
						logger.info("Channel {}: Got ON command", channel);
						updateValue(device, "level", 255);

						// device product name unkown
						if (device.getType().equals(DeviceType.UNKNOWN)) {
							device.setProductName("Generic Switch");
							device.setType(DeviceType.BINARY_SWITCH);
						}

						broadcast("event.device.noolite", new NooliteDeviceOn(channel));
						break;

					case SLOW_TURN_ON:
						logger.info("Channel {}: Got BRIGHT command", channel);
						// we only know, that the user hold ON button
						updateValue(device, "level", 255);

						broadcast("event.device.noolite", new NooliteDeviceOn(channel));
						break;

					case SET_LEVEL:
						logger.info("Channel {}: Got SETLEVEL command.", channel);
						updateValue(device, "level", notification.getValue("level"));

						// device product name unkown
						if (device.getProductName().isEmpty() || device.getType().equals(DeviceType.BINARY_SWITCH)) {
							device.setProductName("Generic Dimmer");
							device.setType(DeviceType.MULTILEVEL_SWITCH);
						}

						broadcast("event.device.noolite", new NooliteDeviceSetLevel(channel, (byte) notification.getValue("level")));
						break;

					case STOP_DIM_BRIGHT:
						logger.info("Channel {}: Got STOPDIMBRIGHT command.", channel);

						broadcast("event.device.noolite", new NooliteDeviceStopDimBright(channel));
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

						updateValue(device, "temperature", notification.getValue("temp"));
						updateValue(device, "humidity", notification.getValue("humi"));
						updateValue(device, "battery", batteryState);

						broadcast("event.device.noolite", new NooliteDeviceTempHumi(
								channel,
								(double) notification.getValue("temp"),
								(double) notification.getValue("humi"),
								batteryState)
						);
						break;

					case BATTERY_LOW:
						logger.info("Channel {}: Got BATTERYLOW command.", channel);

						if (device.getType().equals(DeviceType.BINARY_SWITCH)) {
							device.setType(DeviceType.MOTION_SENSOR);
							device.setProductName("PM111");
						}

						broadcast("event.device.noolite", new NooliteDeviceBatteryLow(channel));
						break;

					default:
						logger.info("Unknown command: {}", notification.getType().name());
				}
			};

			rx.addWatcher(watcher);
			rx.start();
		} catch (Throwable t) {
			logger.error("Noolite RX error!");
			t.printStackTrace();
		}
	}

	private void saveIntoDB() {
		for(NooliteDevice device : devices.values()) {
			devices.replace(Byte.valueOf(String.valueOf(device.getNode())), device, service.saveIntoDatabase(device));
		}
	}

	private void updateValue(NooliteDevice device, String label, Object value) {
		NooliteDeviceValue deviceValue = device.getDeviceValues().get(label);

		if (deviceValue == null) {
			deviceValue = new NooliteDeviceValue();
			deviceValue.setName(label);
			deviceValue.setValue(value);
			deviceValue.setReadOnly(false);

			device.getDeviceValues().put(label, deviceValue);
		}
		else {
			deviceValue.setValue(value);
			device.getDeviceValues().replace(label, device.getDeviceValues().get(label), deviceValue);
		}
	}
}
