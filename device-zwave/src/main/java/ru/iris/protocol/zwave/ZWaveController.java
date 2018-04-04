package ru.iris.protocol.zwave;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.zwave4j.*;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.*;
import ru.iris.models.protocol.enums.ValueType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Profile("zwave")
@Qualifier("zwave")
@Slf4j
public class ZWaveController extends AbstractProtocolService {

    private final EventBus r;
    private final ConfigLoader config;
    private final DeviceRegistry registry;
    private final Gson gson = new GsonBuilder().create();
    private Long homeId;
    private boolean ready = false;

    @Autowired
    public ZWaveController(EventBus r,
                           ConfigLoader config,
                           DeviceRegistry registry) {
        this.r = r;
        this.config = config;
        this.registry = registry;
    }

    @Override
    public void onStartup() {
        logger.info("ZWaveController started");
        if (!config.loadPropertiesFormCfgDirectory("zwave"))
            logger.error("Cant load zwave-specific configs. Check zwave.property if exists");
    }

    @Override
    public void onShutdown() {
        logger.info("ZWaveController stopping");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("command.device");
    }

    @Override
    public Consumer<Event<?>> handleMessage() {
        return event -> {
            if (event.getData() instanceof DeviceCommandEvent) {
                DeviceCommandEvent z = (DeviceCommandEvent) event.getData();

                if (!z.getProtocol().equals(SourceProtocol.ZWAVE)) {
                    return;
                }

                switch (EventLabel.parse(z.getEventLabel())) {
                    case TURN_ON:
                        logger.info("Turn ON device on channel {}", z.getChannel());
                        deviceSetLevel(z.getChannel(), 255);
                        broadcast("event.device.on", new DeviceChangeEvent(
		                        z.getChannel(),
		                        SourceProtocol.ZWAVE,
		                        StandartDeviceValueLabel.LEVEL.getName(),
		                        StandartDeviceValue.FULL_ON.getValue(),
		                        ValueType.BYTE)
                        );
                        break;
                    case TURN_OFF:
                        logger.info("Turn OFF device on channel {}", z.getChannel());
                        deviceSetLevel(z.getChannel(), 0);
                        broadcast("event.device.off", new DeviceChangeEvent(
                                z.getChannel(),
                                SourceProtocol.ZWAVE,
                                StandartDeviceValueLabel.LEVEL.getName(),
                                StandartDeviceValue.FULL_OFF.getValue(),
                                ValueType.BYTE)
                        );
                        break;
                    case SET_LEVEL:
                        if (z.getData() instanceof DataLevel) {
                            DataLevel data = (DataLevel) z.getData();
                            logger.info("Set level {} on channel {}", data.getTo(), z.getChannel());
                            deviceSetLevel(z.getChannel(), "Level", data.getTo());
                            broadcast("event.device.level", new DeviceChangeEvent(
                                    z.getChannel(),
                                    SourceProtocol.ZWAVE,
                                    StandartDeviceValueLabel.LEVEL.getName(),
                                    data.getTo(),
                                    ValueType.BYTE)
                            );
                        }
                        break;
                    case BIND:
                        logger.info("Set controller into AddDevice mode");
                        Manager.get().beginControllerCommand(homeId, ControllerCommand.ADD_DEVICE, new CallbackListener(ControllerCommand.ADD_DEVICE), null, true);
                        break;
                    case UNBIND:
                        logger.info("Set controller into RemoveDevice mode for node {}", z.getChannel());
                        Manager.get().beginControllerCommand(homeId, ControllerCommand.REMOVE_DEVICE, new CallbackListener(ControllerCommand.REMOVE_DEVICE), null, true, Short.valueOf(z.getChannel()));
                        break;
                    case CANCEL:
                        logger.info("Canceling controller command");
                        Manager.get().cancelControllerCommand(homeId);
                        break;
                    default:
                        logger.info("Received unknown request for ZWave service! Class: {}", event.getData().getClass());
                        break;
                }
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

            String node = String.valueOf(notification.getNodeId());
            Device device;

            switch (notification.getType()) {
                case DRIVER_READY:
                    homeId = notification.getHomeId();
                    logger.info("Driver ready. Home ID: {}", homeId);
                    broadcast("event.device.zwave.driver.ready", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "DriverReady", homeId.toString(), ValueType.LONG));
                    break;
                case DRIVER_FAILED:
                    logger.info("Driver failed");
                    broadcast("event.device.zwave.driver.failed", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "DriverFailed"));
                    break;
                case DRIVER_RESET:
                    logger.info("Driver reset");
                    broadcast("event.device.zwave.driver.reset", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "DriverReset"));
                    break;
                case AWAKE_NODES_QUERIED:
                    logger.info("Awake nodes queried");
                    ready = true;
                    broadcast("event.device.zwave.awake.nodes.queried", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "AwakeNodesQueried"));
                    break;
                case ALL_NODES_QUERIED:
                    logger.info("All node queried");
                    manager.writeConfig(homeId);
                    ready = true;
                    broadcast("event.device.zwave.all.nodes.queried", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "AllNodesQueried"));
                    break;
                case ALL_NODES_QUERIED_SOME_DEAD:
                    logger.info("All node queried, some dead");
                    manager.writeConfig(homeId);
                    broadcast("event.device.zwave.all.nodes.queried.some.dead", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "AllNodesQueriedSomeDead"));
                    break;
                case POLLING_ENABLED:
                    logger.info("Polling enabled");
                    broadcast("event.device.zwave.polling.enabled", new DeviceChangeEvent(node, SourceProtocol.ZWAVE, "polling", "true", ValueType.BOOL));
                    break;
                case POLLING_DISABLED:
                    logger.info("Polling disabled");
                    broadcast("event.device.zwave.polling.enabled", new DeviceChangeEvent(node, SourceProtocol.ZWAVE, "polling", "false", ValueType.BOOL));
                    break;
                case NODE_NEW:
                    broadcast("event.device.zwave.node.new", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "NewNode", node, ValueType.SHORT));
                    break;
                case NODE_ADDED:
                    broadcast("event.device.zwave.node.added", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "AddedNode", node, ValueType.SHORT));
                    break;
                case NODE_REMOVED:
                    broadcast("event.device.zwave.node.removed", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "RemovedNode", node, ValueType.SHORT));
                    break;
                case ESSENTIAL_NODE_QUERIES_COMPLETE:
                    broadcast("event.device.zwave.essential.node.queries.complete", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "EssentialNodeQueriesComplete"));
                    break;
                case NODE_QUERIES_COMPLETE:
                    broadcast("event.device.zwave.node.queries.complete", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "NodeQueriesComplete"));
                    break;
                case NODE_EVENT:
                    logger.info("Update info for node " + node);
                    manager.refreshNodeInfo(homeId, notification.getNodeId());
                    broadcast("event.device.zwave.node.event", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "EventNode", node, ValueType.SHORT));
                    break;
                case NODE_NAMING:
                    broadcast("event.device.zwave.node.naming", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "NamingNode", node, ValueType.SHORT));
                    break;
                case NODE_PROTOCOL_INFO:
                    broadcast("event.device.zwave.node.info", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "InfoNode", node, ValueType.SHORT));
                    break;
                case VALUE_REFRESHED:
                    broadcast("event.device.zwave.node.refreshed", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "RefreshedNode", node, ValueType.SHORT));
                    logger.info("Node {} refreshed", node);
                    break;
                case GROUP:
                    broadcast("event.device.zwave.group", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "Group", node, ValueType.SHORT));
                    break;
                case SCENE_EVENT:
                    broadcast("event.device.zwave.scene.event", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "SceneEvent", node, ValueType.SHORT));
                    break;
                case CREATE_BUTTON:
                    broadcast("event.device.zwave.button.create", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "ButtonCreate", node, ValueType.SHORT));
                    break;
                case DELETE_BUTTON:
                    broadcast("event.device.zwave.button.delete", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "ButtonDelete", node, ValueType.SHORT));
                    break;
                case BUTTON_ON:
                    broadcast("event.device.zwave.button.on", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "ButtonOn", node, ValueType.SHORT));
                    break;
                case BUTTON_OFF:
                    broadcast("event.device.zwave.button.off", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "ButtonOff", node, ValueType.SHORT));
                    break;
                case NOTIFICATION:
                    broadcast("event.device.zwave.notification", new DeviceProtocolEvent(SourceProtocol.ZWAVE, "Notification", node, ValueType.SHORT));
                    break;
                case VALUE_ADDED:

                    // check empty label
                    if (Manager.get().getValueLabel(notification.getValueId()).isEmpty())
                        break;

                    String nodeType = manager.getNodeType(homeId, notification.getNodeId());

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
                            broadcast("event.device.zwave.value.added",
                                    new DeviceChangeEvent(node,
                                            SourceProtocol.ZWAVE,
                                            Manager.get().getValueLabel(notification.getValueId()),
                                            getValue(notification.getValueId()) != null
                                                    ? getValue(notification.getValueId()).toString() : "",
                                            getValueType(notification.getValueId())
                                    )
                            );
                    }

                    // enable value polling TODO
                    //Manager.get().enablePoll(notification.getValueId());

                    break;
                case VALUE_REMOVED:
                    device = registry.getDevice(SourceProtocol.ZWAVE, node);

                    if (device == null) {
                        logger.info("Remove ZWave value requested, but node {} not found", node);
                        break;
                    }

                    // remove value from device
                    device.getValues().remove(Manager.get().getValueLabel(notification.getValueId()));

                    broadcast("event.device.zwave.value.removed",
                            new DeviceChangeEvent(node,
                                    SourceProtocol.ZWAVE,
                                    Manager.get().getValueLabel(notification.getValueId()),
                                    null,
                                    getValueType(notification.getValueId())
                            )
                    );

                    if (!manager.getValueLabel(notification.getValueId()).isEmpty()) {
                        logger.info("Node {}: Value \"{}\" removed", device.getChannel(), manager.getValueLabel(notification.getValueId()));
                    }

                    break;
                case VALUE_CHANGED:
                    device = registry.getDevice(SourceProtocol.ZWAVE, node);

                    if (device == null) {
                        logger.info("Change ZWave value requested, but node {} not found", node);
                        break;
                    }

                    // Check for awaked after sleeping nodes
                    if (manager.isNodeAwake(homeId, notification.getNodeId()) && device.getState().equals(State.SLEEPING)) {
                        logger.info("Setting node {}  to LISTEN state", device.getChannel());
                        device.setState(State.ACTIVE);
                    }

                    logger.info("Node " +
                            device.getChannel() + ": " +
                            "Value for label \"" + manager.getValueLabel(notification.getValueId()) + "\" changed --> " +
                            "\"" + getValue(notification.getValueId()) + "\"");

                    String label = manager.getValueLabel(notification.getValueId());
                    ValueId valueId = notification.getValueId();
                    DeviceValue value = device.getValues().get(label);

                    if (value == null)
                        value = new DeviceValue();

                    value.setName(label);
                    value.setType(getValueType(valueId));
                    value.setUnits(Manager.get().getValueUnits(valueId));
                    value.setReadOnly(Manager.get().isValueReadOnly(valueId));
                    value.setCurrentValue(getValue(valueId) != null ? getValue(valueId).toString() : "unknown");
                    value.setAdditionalData(gson.toJson(valueId));

                    registry.addChange(value);
                    device.getValues().put(label, value);

                    // update
                    registry.addOrUpdateDevice(device);

                    broadcast("event.device.zwave.value.changed",
                            new DeviceChangeEvent(node,
                                    SourceProtocol.ZWAVE,
                                    Manager.get().getValueLabel(notification.getValueId()),
                                    getValue(notification.getValueId()) != null
                                            ? getValue(notification.getValueId()).toString() : "",
                                    getValueType(notification.getValueId())
                            )
                    );

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
            logger.info("Still waiting for ZWave controller ready");
        }
    }

    @Override
    public String getServiceIdentifier() {
        return "zwave";
    }

    private Device addZWaveDeviceOrValue(DeviceType type, Notification notification) {

        String label = Manager.get().getValueLabel(notification.getValueId());
        String productName = Manager.get().getNodeProductName(notification.getHomeId(), notification.getNodeId());
        String manufName = Manager.get().getNodeManufacturerName(notification.getHomeId(), notification.getNodeId());
        String node = String.valueOf(notification.getNodeId());
        Device device = registry.getDevice(SourceProtocol.ZWAVE, node);
        boolean listen = false;
        ValueId valueId = notification.getValueId();

        if (Manager.get().requestNodeState(homeId, notification.getNodeId())) {
            listen = true;
        }

        // device doesnt exists
        if (device == null) {
            device = new Device();

            device.setType(type);
            device.setChannel(node);
            device.setSource(SourceProtocol.ZWAVE);
            device.setManufacturer(manufName);
            device.setProductName(productName);
            device.setHumanReadable("zwave/node/" + node);

            if (listen)
                device.setState(State.ACTIVE);
            else
                device.setState(State.UNKNOWN);

            device = registry.addOrUpdateDevice(device);

            Map<String, DeviceValue> values = new HashMap<>();

            DeviceValue value = new DeviceValue();
            value.setDevice(device);
            value.setName(label);
            value.setType(getValueType(valueId));
            value.setUnits(Manager.get().getValueUnits(valueId));
            value.setReadOnly(Manager.get().isValueReadOnly(valueId));
            value.setCurrentValue(getValue(valueId) != null ? getValue(valueId).toString() : "unknown");
            value.setAdditionalData(gson.toJson(valueId));

            // Check if it is beaming device
            DeviceValue beaming = new DeviceValue();
            beaming.setDevice(device);
            beaming.setName(StandartDeviceValueLabel.BEAMING.getName());
            beaming.setType(ValueType.BOOL);
            beaming.setCurrentValue((Manager.get().isNodeBeamingDevice(homeId, notification.getNodeId())) + "");
            beaming.setReadOnly(true);

            beaming = registry.addChange(beaming);
            value = registry.addChange(value);

            values.put(label, value);
            values.put(StandartDeviceValueLabel.BEAMING.getName(), beaming);
            device.setValues(values);

            // update
            registry.addOrUpdateDevice(device);

            logger.info("Adding device " + type + " (node: " + node + ") to system");
        }
        //device exists, add/change value
        else {
            device.setManufacturer(manufName);
            device.setProductName(productName);

            if (listen)
                device.setState(State.ACTIVE);
            else
                device.setState(State.UNKNOWN);

            // check empty label
            if (label.isEmpty())
                return device;

            logger.info("Node {}: Add \"{}\" value \"{}\"", node, label, getValue(valueId));

            DeviceValue value = device.getValues().get(label);

            if (value == null)
                value = new DeviceValue();

            value.setDevice(device);
            value.setName(label);
            value.setType(getValueType(valueId));
            value.setUnits(Manager.get().getValueUnits(valueId));
            value.setReadOnly(Manager.get().isValueReadOnly(valueId));
            value.setCurrentValue(getValue(valueId) != null ? getValue(valueId).toString() : "unknown");
            value.setAdditionalData(gson.toJson(valueId));

            value = registry.addChange(value);
            device.getValues().put(label, value);

            // update
            registry.addOrUpdateDevice(device);
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

    private Object getValue(ValueId valueId) {
        switch (valueId.getType()) {
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

    private ValueType getValueType(ValueId valueId) {
        switch (valueId.getType()) {
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

    private void deviceSetLevel(String node, int level) {
        deviceSetLevel(node, "Level", String.valueOf(level));
    }

    private void deviceSetLevel(String node, String label, String level) {
        Device device = registry.getDevice(SourceProtocol.ZWAVE, node);

        if (device == null) {
            logger.error("Cant find device node {} for set level request", node);
            return;
        }

        if (!device.getState().equals(State.DEAD) && device.getValues().get(label) != null) {
            logger.info("Node {}: Setting value: {} for label \"{}\"", node, level, label);

            if (!Manager.get().isValueReadOnly(gson.fromJson(device.getValues().get(label).getAdditionalData(), ValueId.class))) {
                setTypedValue(gson.fromJson(device.getValues().get(label).getAdditionalData(), ValueId.class), level);
            } else {
                logger.info("Node {}: Value \"{}\" is read-only! Skip.", node, label);
            }
        } else {
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
            logger.debug("ZWave EventLabel Callback: {} , {}", state, err);

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
