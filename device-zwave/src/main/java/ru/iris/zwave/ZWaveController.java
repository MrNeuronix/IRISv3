package ru.iris.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.zwave4j.*;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.bus.models.protocol.zwave.*;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.Protocol;
import ru.iris.commons.service.AbstractService;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.service.ZWaveProtoService;

@Component
@Profile("zwave")
@Qualifier("zwave")
public class ZWaveController extends AbstractService implements Protocol {

	@Autowired
	private EventBus r;

	@Autowired
	private ConfigLoader config;

	@Autowired
	private ZWaveProtoService service;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private long homeId;
	private boolean ready = false;

	@Override
	public void onStartup() {
		logger.info("ZWaveController started");
	}

	@Override
	public void onShutdown() {
		logger.info("ZWaveController stopping");
	}

	@Override
	public void subscribe() throws Exception  {
		addSubscription("command.device.*");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> logger.info("ZWAVE MSG: " + event.getData().getClass());
	}

	@Override
	@Async
	public void run() {

		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

		final Options options = Options.create(config.get("openzwaveCfgPath"), "", "");
		options.addOptionBool("ConsoleOutput", Boolean.valueOf(config.get("zwaveDebug")));
		options.addOptionString("UserPath", "conf/", true);
		options.lock();

		Manager manager = Manager.create();

		NotificationWatcher watcher = (notification, context) -> {

			short node = notification.getNodeId();
			ZWaveDevice device = null;

			switch (notification.getType()) {
				case DRIVER_READY:
					homeId = notification.getHomeId();
					logger.info("Driver ready. Home ID: {}", homeId);
					broadcast("event.device.zwave.driver.ready", new ZWaveDriverReady(homeId));
					break;
				case DRIVER_FAILED:
					logger.info("Driver failed");
					broadcast("event.device.zwave", new ZWaveDriverFailed());
					break;
				case DRIVER_RESET:
					logger.info("Driver reset");
					broadcast("event.device.zwave", new ZWaveDriverReset());
					break;
				case AWAKE_NODES_QUERIED:
					logger.info("Awake nodes queried");
					ready = true;
					broadcast("event.device.zwave", new ZWaveAwakeNodesQueried());
					break;
				case ALL_NODES_QUERIED:
					logger.info("All node queried");
					manager.writeConfig(homeId);
					ready = true;
					broadcast("event.device.zwave", new ZWaveAllNodesQueried());
					break;
				case ALL_NODES_QUERIED_SOME_DEAD:
					logger.info("All node queried, some dead");
					manager.writeConfig(homeId);
					broadcast("event.device.zwave", new ZWaveAllNodesQueriedSomeDead());
					break;
				case POLLING_ENABLED:
					logger.info("Polling enabled");
					broadcast("event.device.zwave", new ZWavePollingEnabled(notification.getNodeId()));
					break;
				case POLLING_DISABLED:
					logger.info("Polling disabled");
					broadcast("event.device.zwave", new ZWavePollingDisabled(notification.getNodeId()));
					break;
				case NODE_NEW:
					broadcast("event.device.zwave", new ZWaveNodeNew(notification.getNodeId()));
					break;
				case NODE_ADDED:
					broadcast("event.device.zwave", new ZWaveNodeAdded(notification.getNodeId()));
					break;
				case NODE_REMOVED:
					broadcast("event.device.zwave", new ZWaveNodeRemoved(notification.getNodeId()));
					break;
				case ESSENTIAL_NODE_QUERIES_COMPLETE:
					broadcast("event.device.zwave", new ZWaveEssentialsNodeQueriesComplete());
					break;
				case NODE_QUERIES_COMPLETE:
					broadcast("event.device.zwave", new ZWaveNodeQueriesComplete());
					break;
				case NODE_EVENT:
					logger.info("Update info for node " + node);
					manager.refreshNodeInfo(homeId, node);
					broadcast("event.device.zwave", new ZWaveNodeEvent(notification.getNodeId()));
					break;
				case NODE_NAMING:
					broadcast("event.devices.zwave", new ZWaveNodeNaming(notification.getNodeId()));
					break;
				case NODE_PROTOCOL_INFO:
					broadcast("event.devices.zwave", new ZWaveNodeProtocolInfo(notification.getNodeId()));
					break;
			}
				/*case VALUE_ADDED:

					// check empty label
					if (Manager.get().getValueLabel(notification.getValueId()).isEmpty())
						break;

					String nodeType = manager.getNodeType(homeId, node);

					switch (nodeType) {
						case "Portable Remote Controller":
							device = addZWaveDeviceOrValue("controller", notification);
							break;

						//////////////////////////////////

						case "Multilevel Power Switch":
							device = addZWaveDeviceOrValue("dimmer", notification);
							break;

						//////////////////////////////////

						case "Routing Alarm Sensor":
							device = addZWaveDeviceOrValue("alarmsensor", notification);
							break;

						case "Binary Power Switch":
							device = addZWaveDeviceOrValue("switch", notification);
							break;

						case "Routing Binary Sensor":
							device = addZWaveDeviceOrValue("binarysensor", notification);
							break;

						//////////////////////////////////

						case "Routing Multilevel Sensor":
							device = addZWaveDeviceOrValue("multilevelsensor", notification);
							break;

						//////////////////////////////////

						case "Simple Meter":
							device = addZWaveDeviceOrValue("metersensor", notification);
							break;

						//////////////////////////////////

						case "Simple Window Covering":
							device = addZWaveDeviceOrValue("drapes", notification);
							break;

						//////////////////////////////////

						case "Setpoint Thermostat":
							device = addZWaveDeviceOrValue("thermostat", notification);
							break;

						//////////////////////////////////
						//////////////////////////////////

						default:
							logger.info("Unassigned value for node" +
									node +
									" type " +
									manager.getNodeType(notification.getHomeId(), notification.getNodeId()) +
									" class " +
									notification.getValueId().getCommandClassId() +
									" genre " +
									notification.getValueId().getGenre() +
									" label " +
									manager.getValueLabel(notification.getValueId()) +
									" value " +
									Utils.getValue(notification.getValueId()) +
									" index " +
									notification.getValueId().getIndex() +
									" instance " +
									notification.getValueId().getInstance()
							);

							data.put("uuid", device.getUuid());
							data.put("label", Manager.get().getValueLabel(notification.getValueId()));
							data.put("data", String.valueOf(Utils.getValue(notification.getValueId())));

							broadcast("event.devices.zwave.value.added", new GenericAdvertisement("ZWaveValueAdded", data));
					}

					// enable value polling TODO
					//Manager.get().enablePoll(notification.getValueId());

					break;
				case VALUE_REMOVED:

					device = Device.getDeviceByNode(node);

					if (device == null) {
						logger.info("While save remove value, node " + node + " not found");
						break;
					}

					device.removeValue(manager.getValueLabel(notification.getValueId()));

					data.put("uuid", device.getUuid());
					data.put("label", Manager.get().getValueLabel(notification.getValueId()));
					data.put("data", String.valueOf(Utils.getValue(notification.getValueId())));

					broadcast("event.devices.zwave.value.removed", new GenericAdvertisement("ZWaveValueRemoved", data));

					if (!manager.getValueLabel(notification.getValueId()).isEmpty()) {
						logger.info("Node " + device.getNode() + ": Value " + manager.getValueLabel(notification.getValueId()) + " removed");
					}

					break;
				case VALUE_CHANGED:

					device = Device.getDeviceByNode(node);

					if (device == null) {
						break;
					}

					// Check for awaked after sleeping nodes
					if (manager.isNodeAwake(homeId, device.getNode()) && device.getStatus().equals("Sleeping")) {
						logger.info("Setting node " + device.getNode() + " to LISTEN state");
						device.setStatus("Listening");
					}

					logger.info("Node " +
							device.getNode() + ": " +
							"Value for label \"" + manager.getValueLabel(notification.getValueId()) + "\" changed --> " +
							"\"" + Utils.getValue(notification.getValueId()) + "\"");

					DeviceValue udvChg = device.getValue(manager.getValueLabel(notification.getValueId()));

					if (udvChg != null)
						device.removeValue(udvChg);
					else
						udvChg = new DeviceValue();

					udvChg.setLabel(manager.getValueLabel(notification.getValueId()));
					udvChg.setValueType(Utils.getValueType(notification.getValueId()));
					udvChg.setValueId(notification.getValueId());
					udvChg.setValueUnits(Manager.get().getValueUnits(notification.getValueId()));
					udvChg.setValue(String.valueOf(Utils.getValue(notification.getValueId())));
					udvChg.setReadonly(Manager.get().isValueReadOnly(notification.getValueId()));

					device.addValue(udvChg);

					DBlogger.info("Value " + manager.getValueLabel(notification.getValueId()) + " changed: " + Utils.getValue(notification.getValueId()), device.getUuid());
					SensorData.log(device.getUuid(), Manager.get().getValueLabel(notification.getValueId()), String.valueOf(Utils.getValue(notification.getValueId())));

					data.put("uuid", device.getUuid());
					data.put("label", Manager.get().getValueLabel(notification.getValueId()));
					data.put("data", String.valueOf(Utils.getValue(notification.getValueId())));

					broadcast("event.devices.zwave.value.changed", new GenericAdvertisement("ZWaveValueChanged", data));

					break;
				case VALUE_REFRESHED:
					logger.info("Node " + node + ": Value refreshed (" +
							" command class: " + notification.getValueId().getCommandClassId() + ", " +
							" instance: " + notification.getValueId().getInstance() + ", " +
							" index: " + notification.getValueId().getIndex() + ", " +
							" value: " + Utils.getValue(notification.getValueId()));
					break;
				case GROUP:
					break;
				case SCENE_EVENT:
					break;
				case CREATE_BUTTON:
					break;
				case DELETE_BUTTON:
					break;
				case BUTTON_ON:
					break;
				case BUTTON_OFF:
					break;
				case NOTIFICATION:
					break;
				default:
					logger.info(notification.getType().name());
					break;
			}

			// save device and values
			if (device != null)
				device.save();*/
		};

		manager.addWatcher(watcher, null);
		manager.addDriver(config.get("zwavePort"));

		logger.info("Waiting while ZWave finish initialization");

		// Waiting for initialization ends

		while (!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Error: {}", e.getLocalizedMessage());
			}
			logger.info("Still waiting");
		}
	}
}
