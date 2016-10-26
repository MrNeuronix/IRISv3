package ru.iris.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.zwave4j.*;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.Protocol;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.service.AbstractService;
import ru.iris.zwave.protocol.events.*;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;
import ru.iris.zwave.protocol.service.ZWaveProtoService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Profile("zwave")
@Qualifier("zwave")
@Scope("singleton")
public class ZWaveController extends AbstractService implements Protocol {

	private final EventBus r;
	private final ConfigLoader config;
	private final ZWaveProtoService service;
	private Map<Short, ZWaveDevice> devices = new HashMap<>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private long homeId;
	private boolean ready = false;

	@Autowired
	public ZWaveController(ZWaveProtoService service, EventBus r, ConfigLoader config) {
		this.service = service;
		this.r = r;
		this.config = config;
	}

	@Override
	public void onStartup() {
		logger.info("ZWaveController started");
		if(!config.loadPropertiesFormCfgDirectory("zwave"))
			logger.error("Cant load zwave-specific configs. Check zwave.property if exists");

		for(ZWaveDevice device : service.getZWaveDevices()) {
			devices.put(device.getNode(), device);
		}

		logger.debug("Load {} ZWave devices from database", devices.size());
	}

	@Override
	public void onShutdown() {
		logger.info("ZWaveController stopping");
		logger.info("Saving ZWave devices state into database");
		saveIntoDB();
		logger.info("Saved");
	}

	@Override
	public void subscribe() throws Exception  {
		addSubscription("command.device.zwave.*");
	}

	@Override
	public Consumer<Event<?>> handleMessage() {
		return event -> {
			if (event.getData() instanceof ZWaveDeviceOn) {
				ZWaveDeviceOn z = (ZWaveDeviceOn) event.getData();
				deviceSetLevel(z.getNode(), 255);
			} else if (event.getData() instanceof ZWaveDeviceOff) {
				ZWaveDeviceOff z = (ZWaveDeviceOff) event.getData();
				deviceSetLevel(z.getNode(), 0);
			} else if (event.getData() instanceof ZWaveSetValue) {
				ZWaveSetValue z = (ZWaveSetValue) event.getData();
				deviceSetLevel(z.getNode(), z.getLabel(), z.getValue());
			} else if (event.getData() instanceof ZWaveDeviceAddRequest) {
				logger.info("Set controller into AddDevice mode");
				Manager.get().beginControllerCommand(homeId, ControllerCommand.ADD_DEVICE, new CallbackListener(ControllerCommand.ADD_DEVICE), null, true);
			} else if (event.getData() instanceof ZWaveDeviceRemoveRequest) {
				ZWaveDeviceRemoveRequest z = (ZWaveDeviceRemoveRequest) event.getData();
				logger.info("Set controller into RemoveDevice mode for node {}", z.getNode());
				Manager.get().beginControllerCommand(homeId, ControllerCommand.REMOVE_DEVICE, new CallbackListener(ControllerCommand.REMOVE_DEVICE), null, true, z.getNode());
			} else if (event.getData() instanceof ZWaveDeviceCancelRequest) {
				logger.info("Canceling controller command");
				Manager.get().cancelControllerCommand(homeId);
			} else {
				// We received unknown request message. Lets make generic log entry.
				logger.info("Received unknown request for zwave service! Class: {}", event.getData().getClass());
			}
		};
	}

	@Override
	@Async
	public void run() {

		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

		final Options options = Options.create(config.get("openzwaveCfgPath"), "", "");
		options.addOptionBool("ConsoleOutput", Boolean.valueOf(config.get("zwaveDebug")));
		options.addOptionString("UserPath", config.get("openzwaveCfgPath"), true);
		options.lock();

		Manager manager = Manager.create();

		NotificationWatcher watcher = (notification, context) -> {

			short node = notification.getNodeId();
			ZWaveDevice device = null;

			switch (notification.getType()) {
				case DRIVER_READY:
					homeId = notification.getHomeId();
					logger.info("Driver ready. Home ID: {}", homeId);
					broadcast("event.device.zwave", new ZWaveDriverReady(homeId));
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
				case VALUE_REFRESHED:
					broadcast("event.device.zwave", new ZWaveValueRefreshed(notification.getNodeId()));
					logger.info("Node " + node + ": Value refreshed (" +
							" command class: " + notification.getValueId().getCommandClassId() + ", " +
							" instance: " + notification.getValueId().getInstance() + ", " +
							" index: " + notification.getValueId().getIndex() + ", " +
							" value: " + notification.getValueId());
					break;
				case GROUP:
					broadcast("event.device.zwave", new ZWaveGroup(notification.getNodeId()));
					break;
				case SCENE_EVENT:
					broadcast("event.device.zwave", new ZWaveScene(notification.getNodeId()));
					break;
				case CREATE_BUTTON:
					broadcast("event.device.zwave", new ZWaveCreateButton(notification.getNodeId()));
					break;
				case DELETE_BUTTON:
					broadcast("event.device.zwave", new ZWaveDeleteButton(notification.getNodeId()));
					break;
				case BUTTON_ON:
					broadcast("event.device.zwave", new ZWaveButtonOn(notification.getNodeId()));
					break;
				case BUTTON_OFF:
					broadcast("event.device.zwave", new ZWaveButtonOff(notification.getNodeId()));
					break;
				case NOTIFICATION:
					broadcast("event.device.zwave", new ZWaveNotification(notification.getNodeId()));
					break;
				case VALUE_ADDED:

					// check empty label
					if (Manager.get().getValueLabel(notification.getValueId()).isEmpty())
						break;

					String nodeType = manager.getNodeType(homeId, node);

					switch (nodeType) {

						case "Portable Remote Controller":
							addZWaveDeviceOrValue(DeviceType.CONTROLLER, notification);
							break;
						case "Multilevel Power Switch":
							addZWaveDeviceOrValue(DeviceType.MULTILEVEL_SWITCH, notification);
							break;
						case "Routing Alarm Sensor":
							addZWaveDeviceOrValue(DeviceType.ALARM_SENSOR, notification);
							break;
						case "Binary Power Switch":
							addZWaveDeviceOrValue(DeviceType.BINARY_SWITCH, notification);
							break;
						case "Routing Binary Sensor":
							addZWaveDeviceOrValue(DeviceType.BINARY_SENSOR, notification);
							break;
						case "Routing Multilevel Sensor":
							addZWaveDeviceOrValue(DeviceType.MULTILEVEL_SENSOR, notification);
							break;
						case "Simple Meter":
							addZWaveDeviceOrValue(DeviceType.SIMPLE_METER, notification);
							break;
						case "Simple Window Covering":
							addZWaveDeviceOrValue(DeviceType.DRAPES, notification);
							break;
						case "Setpoint Thermostat":
							addZWaveDeviceOrValue(DeviceType.THERMOSTAT, notification);
							break;

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
									getValue(notification.getValueId()) +
									" index " +
									notification.getValueId().getIndex() +
									" instance " +
									notification.getValueId().getInstance()
							);

							// finnaly, create message and sent
							ZWaveValueAdded created = new ZWaveValueAdded(
									node,
									Manager.get().getValueLabel(notification.getValueId()),
									getValue(notification.getValueId())
							);

							broadcast("event.device.zwave", created);
					}

					// enable value polling TODO
					//Manager.get().enablePoll(notification.getValueId());

					break;
				case VALUE_REMOVED:

					device = devices.get(node);

					if (device == null) {
						logger.info("Remove ZWave value requested, but node {} not found", node);
						break;
					}

					// remove value from device
					device.getDeviceValues().remove(Manager.get().getValueLabel(notification.getValueId()));

					ZWaveValueRemoved removed = new ZWaveValueRemoved(
							node,
							Manager.get().getValueLabel(notification.getValueId()),
							getValue(notification.getValueId())
					);

					broadcast("event.device.zwave", removed);

					if (!manager.getValueLabel(notification.getValueId()).isEmpty()) {
						logger.info("Node {}: Value \"{}\" removed", device.getNode(), manager.getValueLabel(notification.getValueId()));
					}

					break;
				case VALUE_CHANGED:

					device = devices.get(node);

					if (device == null) {
						logger.info("Change ZWave value requested, but node {} not found", node);
						break;
					}

					// Check for awaked after sleeping nodes
					if (manager.isNodeAwake(homeId, device.getNode()) && device.getState().equals(State.SLEEPING)) {
						logger.info("Setting node {}  to LISTEN state", device.getNode());
						device.setState(State.ACTIVE);
					}

					logger.info("Node " +
							device.getNode() + ": " +
							"Value for label \"" + manager.getValueLabel(notification.getValueId()) + "\" changed --> " +
							"\"" + getValue(notification.getValueId()) + "\"");

					String label = manager.getValueLabel(notification.getValueId());
					ValueId valueId = notification.getValueId();
					ZWaveDeviceValue value = device.getDeviceValues().get(label);

					if(value == null)
						value = new ZWaveDeviceValue();

					value.setName(label);
					value.setType(getValueType(valueId));
					value.setUnits(Manager.get().getValueUnits(valueId));
					value.setReadOnly(Manager.get().isValueReadOnly(valueId));
					value.setCurrentValue(getValue(valueId));
					value.setValueId(valueId);

					service.addChange(value);

					device.getDeviceValues().remove(label);
					device.getDeviceValues().put(label, value);
					devices.replace(node, device);

					ZWaveValueChanged changed = new ZWaveValueChanged(
							node,
							Manager.get().getValueLabel(notification.getValueId()),
							getValue(notification.getValueId())
					);

					broadcast("event.device.zwave", changed);

					break;
				default:
					logger.info(notification.getType().name());
					break;
			}
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

		saveIntoDB();
	}

	private void saveIntoDB() {
		for(ZWaveDevice device : devices.values()) {
			devices.replace(device.getNode(), device, service.saveIntoDatabase(device));
		}
	}

	private ZWaveDevice addZWaveDeviceOrValue(DeviceType type, Notification notification) {

		String label = Manager.get().getValueLabel(notification.getValueId());
		String productName = Manager.get().getNodeProductName(notification.getHomeId(), notification.getNodeId());
		String manufName = Manager.get().getNodeManufacturerName(notification.getHomeId(), notification.getNodeId());
		ZWaveDevice device = devices.get(notification.getNodeId());
		boolean listen = false;
		ValueId valueId = notification.getValueId();
		Short node = notification.getNodeId();

		if (Manager.get().requestNodeState(homeId, node))
		{
			listen = true;
		}

		// device doesnt exists
		if(device == null)
		{
			device = new ZWaveDevice();

			device.setType(type);
			device.setNode(node);
			device.setManufacturer(manufName);
			device.setProductName(productName);
			device.setHumanReadable("zwave/node/"+node);

			if(listen)
				device.setState(State.ACTIVE);
			else
				device.setState(State.UNKNOWN);

			Map<String, ZWaveDeviceValue> values = new HashMap<>();

			ZWaveDeviceValue value = new ZWaveDeviceValue();
			value.setName(label);
			value.setType(getValueType(valueId));
			value.setUnits(Manager.get().getValueUnits(valueId));
			value.setReadOnly(Manager.get().isValueReadOnly(valueId));
			value.setCurrentValue(getValue(valueId));
			value.setValueId(valueId);

			// Check if it is beaming device
			ZWaveDeviceValue beaming = new ZWaveDeviceValue();
			beaming.setName("beaming");
			beaming.setType(ValueType.NONE);
			beaming.setCurrentValue(String.valueOf(Manager.get().isNodeBeamingDevice(homeId, node)));
			beaming.setReadOnly(true);

			service.addChange(beaming);
			service.addChange(value);

			values.put(label, value);
			values.put("beaming", beaming);
			device.setDeviceValues(values);

			// add new device to pool
			devices.put(node, device);

			logger.info("Adding device " + type + " (node: " + node + ") to system");
		}
		//device exists, add/change value
		else
		{
			device.setManufacturer(manufName);
			device.setProductName(productName);

			if(listen)
				device.setState(State.ACTIVE);
			else
				device.setState(State.UNKNOWN);

			// check empty label
			if (label.isEmpty())
				return device;

			logger.info("Node {}: Add \"{}\" value \"{}\"", node, label, getValue(valueId));

			ZWaveDeviceValue value = device.getDeviceValues().get(label);

			if(value == null)
				value = new ZWaveDeviceValue();

			value.setName(label);
			value.setType(getValueType(valueId));
			value.setUnits(Manager.get().getValueUnits(valueId));
			value.setReadOnly(Manager.get().isValueReadOnly(valueId));
			value.setCurrentValue(getValue(valueId));
			value.setValueId(valueId);

			service.addChange(value);

			device.getDeviceValues().remove(label);
			device.getDeviceValues().put(label, value);

			// replace
			devices.replace(node, device);
		}

		return device;
	}

	private void setTypedValue(ValueId valueId, String value) {

		logger.debug("Set type {} to label {}", valueId.getType(), Manager.get().getValueLabel(valueId));

		switch (valueId.getType()) {
			case BOOL:
				logger.debug("Set value type BOOL to " + value);
				Manager.get().setValueAsBool(valueId, Boolean.valueOf(value));
				break;
			case BYTE:
				logger.debug("Set value type BYTE to " + value);
				Manager.get().setValueAsByte(valueId, Short.valueOf(value));
				break;
			case DECIMAL:
				logger.debug("Set value type FLOAT to " + value);
				Manager.get().setValueAsFloat(valueId, Float.valueOf(value));
				break;
			case INT:
				logger.debug("Set value type INT to " + value);
				Manager.get().setValueAsInt(valueId, Integer.valueOf(value));
				break;
			case LIST:
				logger.debug("Set value type LIST to " + value);
				break;
			case SCHEDULE:
				logger.debug("Set value type SCHEDULE to " + value);
				break;
			case SHORT:
				logger.debug("Set value type SHORT to " + value);
				Manager.get().setValueAsShort(valueId, Short.valueOf(value));
				break;
			case STRING:
				logger.debug("Set value type STRING to " + value);
				Manager.get().setValueAsString(valueId, value);
				break;
			case BUTTON:
				logger.debug("Set value type BUTTON to " + value);
				break;
			case RAW:
				logger.debug("Set value RAW to " + value);
				break;
			default:
				break;
		}
	}

	private Object getValue(ValueId valueId)
	{
		switch (valueId.getType())
		{
			case BOOL:
				AtomicReference<Boolean> b = new AtomicReference<>();
				Manager.get().getValueAsBool(valueId, b);
				return b.get();
			case BYTE:
				AtomicReference<Short> bb = new AtomicReference<>();
				Manager.get().getValueAsByte(valueId, bb);
				return bb.get();
			case DECIMAL:
				AtomicReference<Float> f = new AtomicReference<>();
				Manager.get().getValueAsFloat(valueId, f);
				return f.get();
			case INT:
				AtomicReference<Integer> i = new AtomicReference<>();
				Manager.get().getValueAsInt(valueId, i);
				return i.get();
			case LIST:
				return null;
			case SCHEDULE:
				return null;
			case SHORT:
				AtomicReference<Short> s = new AtomicReference<>();
				Manager.get().getValueAsShort(valueId, s);
				return s.get();
			case STRING:
				AtomicReference<String> ss = new AtomicReference<>();
				Manager.get().getValueAsString(valueId, ss);
				return ss.get();
			case BUTTON:
				return null;
			case RAW:
				AtomicReference<short[]> sss = new AtomicReference<>();
				Manager.get().getValueAsRaw(valueId, sss);
				return sss.get();
			default:
				return null;
		}
	}

	private ru.iris.commons.protocol.enums.ValueType getValueType(ValueId valueId)
	{
		switch (valueId.getType())
		{
			case BOOL:
				return ValueType.BOOL;
			case BYTE:
				return ValueType.BYTE;
			case DECIMAL:
				return ValueType.DECIMAL;
			case INT:
				return ValueType.INT;
			case LIST:
				return ValueType.LIST;
			case SCHEDULE:
				return ValueType.SCHEDULE;
			case SHORT:
				return ValueType.SHORT;
			case STRING:
				return ValueType.STRING;
			case BUTTON:
				return ValueType.BUTTON;
			case RAW:
				return ValueType.RAW;
			default:
				return ValueType.RAW;
		}
	}

	private void deviceSetLevel(short node, int level) {
		deviceSetLevel(node, "Level", String.valueOf(level));
	}

	private void deviceSetLevel(short node, String label, String level)
	{
		ZWaveDevice device = devices.get(node);

		if(device == null)
		{
			logger.error("Cant find device node {} for set level request", node);
			return;
		}

		if (!device.getState().equals(State.DEAD) && device.getDeviceValues().get(label) != null)
		{
			logger.info("Node {}: Setting value: {} for label \"{}\"", node, level, label);

			if (!Manager.get().isValueReadOnly(device.getDeviceValues().get(label).getValueId()))
			{
				setTypedValue(device.getDeviceValues().get(label).getValueId(), level);
			}
			else
			{
				logger.info("Node {}: Value \"{}\" is read-only! Skip.", node, label);
			}
		}
		else
		{
			logger.info("Node {}: Cant set empty value or node dead", node);
		}
	}

	private class CallbackListener implements ControllerCallback {
		private ControllerCommand ctl;

		CallbackListener(ControllerCommand ctl) {
			this.ctl = ctl;
		}

		@Override
		public void onCallback(ControllerState state, ControllerError err, Object context) {
			logger.debug("ZWave Command Callback: {} , {}", state, err);

			if (ctl == ControllerCommand.REMOVE_DEVICE && state == ControllerState.COMPLETED) {
				logger.info("Remove ZWave device from network");
				Manager.get().softReset(homeId);
				Manager.get().testNetwork(homeId, 5);
				Manager.get().healNetwork(homeId, true);
			}

			if (ctl == ControllerCommand.ADD_DEVICE && state == ControllerState.COMPLETED) {
				logger.info("Add ZWave device to network");
				Manager.get().testNetwork(homeId, 5);
				Manager.get().healNetwork(homeId, true);
			}
		}
	}
}
