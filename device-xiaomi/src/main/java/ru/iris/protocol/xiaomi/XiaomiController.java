package ru.iris.protocol.xiaomi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.bus.devices.DeviceProtocolEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValue;
import ru.iris.commons.protocol.data.DataLevel;
import ru.iris.commons.protocol.data.DataSubChannelLevel;
import ru.iris.commons.protocol.enums.*;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.xiaomi4j.Discovery;
import ru.iris.xiaomi4j.Gateway;
import ru.iris.xiaomi4j.model.GatewayModel;
import ru.iris.xiaomi4j.watchers.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.iris.commons.protocol.enums.EventLabel.BATTERY_LOW;

@Component
@Profile("xiaomi")
@Qualifier("xiaomi")
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XiaomiController extends AbstractProtocolService {
    private final EventBus r;
    private final ConfigLoader config;
    private final DeviceRegistry registry;
    private final Gson gson = new GsonBuilder().create();
    private static final JsonParser PARSER = new JsonParser();
    private List<GatewayController> gateways;
    private Gateway gateway = null;

    private static final int VOLTAGE_MAX_MILLIVOLTS = 3100;
    private static final int VOLTAGE_MIN_MILLIVOLTS = 2700;
    private static final int BATT_LEVEL_LOW = 20;

    @Autowired
    public XiaomiController(EventBus r,
                            ConfigLoader config,
                            DeviceRegistry registry) {
        this.r = r;
        this.config = config;
        this.registry = registry;
    }

    @Override
    public void onStartup() {
        logger.info("XiaomiController started");
        if (!config.loadPropertiesFormCfgDirectory("xiaomi"))
            logger.error("Cant load xiaomi-specific configs. Check xiaomi.property if exists");

        String gatewayConfig = config.get("gateways");

        if (StringUtils.isEmpty(gatewayConfig)) {
            logger.error("No gateways specified!");
        } else {
            Type listType = new TypeToken<ArrayList<GatewayController>>() {
            }.getType();
            gateways = gson.fromJson(gatewayConfig, listType);
        }
    }

    @Override
    public void onShutdown() {
        logger.info("XiaomiController stopping");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("command.device");
    }

    @Override
    public Consumer<Event<?>> handleMessage() {
        return event -> {
            if (event.getData() instanceof DeviceCommandEvent) {
                DeviceCommandEvent x = (DeviceCommandEvent) event.getData();

                if (!x.getProtocol().equals(SourceProtocol.XIAOMI)) {
                    return;
                }

                if (gateway == null) {
                    logger.error("No gateways configured!");
                    return;
                }

                switch (EventLabel.parse(x.getEventLabel())) {
                    case TURN_ON:
                        if (x.getClazz().equals(DataLevel.class)) {
                            logger.info("Turn ON device on channel {}", x.getChannel());
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"on"});
                            broadcast(
                                    "event.device.on",
                                    DeviceCommandEvent.builder()
                                            .channel(x.getChannel())
                                            .protocol(SourceProtocol.XIAOMI)
                                            .eventLabel(StandartDeviceValueLabel.LEVEL.getName())
                                            .data(new DataLevel(StandartDeviceValue.FULL_ON.getValue(), ValueType.BYTE))
                                            .clazz(DataLevel.class)
                                            .build()
                            );
                        } else if (x.getClazz().equals(DataSubChannelLevel.class)) {
                            DataSubChannelLevel data = (DataSubChannelLevel) x.getData();
                            logger.info("Turn ON device on channel {}, subchannel: {}", x.getChannel(), data.getSubChannel());
                            int subchannel = data.getSubChannel() - 1;
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_" + subchannel}, new String[]{"on"});
                            broadcast(
                                    "event.device.on",
                                    DeviceCommandEvent.builder()
                                            .channel(x.getChannel())
                                            .protocol(SourceProtocol.XIAOMI)
                                            .eventLabel(StandartDeviceValueLabel.LEVEL.getName())
                                            .data(new DataLevel(String.valueOf(subchannel), StandartDeviceValue.FULL_ON.getValue(), ValueType.BYTE))
                                            .clazz(DataLevel.class)
                                            .build()
                            );
                        } else {
                            logger.error("Unknown data class!");
                        }
                        break;
                    case TURN_OFF:
                        if (x.getClazz().equals(DataLevel.class)) {
                            logger.info("Turn OFF device on channel {}", x.getChannel());
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"off"});
                            broadcast(
                                    "event.device.off",
                                    new DeviceChangeEvent(
                                            x.getChannel(),
                                            SourceProtocol.XIAOMI,
                                            StandartDeviceValueLabel.LEVEL.getName(),
                                            StandartDeviceValue.FULL_OFF.getValue(),
                                            ValueType.BYTE));
                        } else if (x.getClazz().equals(DataSubChannelLevel.class)) {
                            DataSubChannelLevel data = (DataSubChannelLevel) x.getData();
                            logger.info("Turn OFF device on channel {}, subchannel: {}", x.getChannel(), data.getSubChannel());
                            int subchannel = data.getSubChannel() - 1;
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_" + subchannel}, new String[]{"off"});
                            broadcast(
                                    "event.device.off",
                                    new DeviceChangeEvent(
                                            x.getChannel(),
                                            SourceProtocol.XIAOMI,
                                            StandartDeviceValueLabel.LEVEL.getName(),
                                            String.valueOf(subchannel),
                                            StandartDeviceValue.FULL_OFF.getValue(),
                                            ValueType.BYTE));
                        } else {
                            logger.error("Unknown data class!");
                        }
                        break;
                    default:
                        logger.info("Received unknown request for Xiaomi service! Class: {}", event.getData().getClass());
                        break;
                }
            }
        };
    }

    @Override
    @Async
    public void run() throws InterruptedException {
        logger.info("Gateways: {}", gateways);

        Discovery discovery = new Discovery();
        discovery.startScan();

        Thread.sleep(3000L);

        List<GatewayModel> discoveredGateways = discovery.getGatewayModels();
        logger.info("Gateways found: " + discoveredGateways.size());

        discovery.stopScan();

        if (discoveredGateways.size() == 0) {
            logger.error("No Xiaomi gateways found!");
            return;
        }

        for (GatewayModel model : discoveredGateways) {
            for (GatewayController controller : gateways) {
                if (model.getSid().equals(controller.getSerial())) {
                    gateway = new Gateway(
                            model.getHost(),
                            model.getPort(),
                            model.getSid(),
                            controller.getEncryptionKey(),
                            this::doWork);
                }
            }
        }

        if (gateway == null) {
            logger.error("No gateways found, specified in xiaomi.properties");
        }
    }

    @SuppressWarnings("Duplicates")
    private void doWork(Notification notification) {

        String sid = notification.getSid();
        Device device = registry.getDevice(SourceProtocol.XIAOMI, sid);

        if (device == null) {
            device = new Device();
            device.setSource(SourceProtocol.XIAOMI);
            device.setHumanReadable("xiaomi/channel/" + sid);
            device.setState(State.ACTIVE);
            device.setManufacturer("Xiaomi");
            device.setChannel(sid);

            device = registry.addOrUpdateDevice(device);
            broadcast("event.device.added", new DeviceProtocolEvent(sid, SourceProtocol.XIAOMI, "DeviceAdded"));
        }

        if (device.getType() == null || device.getType().equals(DeviceType.UNKNOWN)) {
            switch (notification.getType()) {
                case GATEWAY:
                case BRIDGE:
                    device.setType(DeviceType.CONTROLLER);
                    device.setProductName("Mi Gateway");
                    break;
                case SENSOR_HT:
                    device.setType(DeviceType.TEMP_HUMI_SENSOR);
                    device.setProductName("Aqara Temperature & Humidity Sensor");
                    break;
                case SENSOR_AQARA_FLOOD:
                    device.setType(DeviceType.FLOOD_SENSOR);
                    device.setProductName("Aqara Flood Sensor");
                    break;
                case SWITCH:
                    device.setType(DeviceType.BUTTON);
                    device.setProductName("Aqara Wireless Switch (button)");
                    break;
                case SENSOR_AQARA_MAGNET:
                    device.setType(DeviceType.DOOR_SENSOR);
                    device.setProductName("Aqara Door Sensor");
                    break;
                case SWITCH_AQARA_1BUTTON:
                    device.setType(DeviceType.BINARY_SWITCH);
                    device.setProductName("Aqara Switch 1 Button");
                    break;
                case SWITCH_AQARA_2BUTTONS:
                    device.setType(DeviceType.BINARY_SWITCH);
                    device.setProductName("Aqara Switch 2 Buttons");
                    break;
                case SWITCH_AQARA_ZERO_1BUTTON:
                    device.setType(DeviceType.BINARY_SWITCH);
                    device.setProductName("Aqara Wireless Switch 1 Button");
                    break;
                case SWITCH_AQARA_ZERO_2BUTTONS:
                    device.setType(DeviceType.BINARY_SWITCH);
                    device.setProductName("Aqara Wireless Switch 2 Buttons");
                    break;
	              case SENSOR_MOTION:
			              device.setType(DeviceType.MOTION_SENSOR);
			              device.setProductName("Generic Motion Sensor");
	              case SENSOR_AQUARA_MOTION:
	              	  device.setType(DeviceType.MOTION_SENSOR);
	              	  device.setProductName("Aqara Motion Sensor");
                    break;

	              default:
                    device.setProductName("Unknown device");
                    device.setType(DeviceType.UNKNOWN);
            }

            if (!device.getType().equals(DeviceType.UNKNOWN)) {
                device = registry.addOrUpdateDevice(device);
                broadcast("event.device.updated", new DeviceProtocolEvent(sid, SourceProtocol.XIAOMI, "DeviceUpdated"));
            }
        }

        JsonObject message;

        switch (notification.getType()) {
            case SENSOR_HT:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();
                    Double temp, humi;

                    if (data.has("temperature")) {
                        temp = data.get("temperature").getAsInt() / 100D;
                        DeviceValue tempDb = device.getValues().get(StandartDeviceValueLabel.TEMPERATURE.getName());

                        if ((tempDb != null && tempDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(tempDb.getCurrentValue()), temp))
                                || tempDb == null || tempDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.TEMPERATURE.getName(), temp.toString(), ValueType.DOUBLE);

                            broadcast("event.device.temperature", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.TEMPERATURE.getName(),
                                    temp.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Temperature: {}C", notification.getSid(), temp);
                        }
                    }

                    if (data.has("humidity")) {
                        humi = data.get("humidity").getAsInt() / 100D;
                        DeviceValue humiDb = device.getValues().get(StandartDeviceValueLabel.HUMIDITY.getName());

                        if ((humiDb != null && humiDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(humiDb.getCurrentValue()), humi))
                                || humiDb == null || humiDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.HUMIDITY.getName(), humi.toString(), ValueType.DOUBLE);

                            broadcast("event.device.humidity", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.HUMIDITY.getName(),
                                    humi.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Humidity: {}%", notification.getSid(), humi);
                        }
                    }

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.VOLTAGE.getName(),
                                    voltage.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }
                }

                break;
            case SENSOR_AQARA_MAGNET:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("status")) {
                        Boolean status = data.get("status").getAsString().equals("open");
                        DeviceValue statusDb = device.getValues().get(StandartDeviceValueLabel.OPENED.getName());

                        if ((statusDb != null && statusDb.getCurrentValue() != null && !Boolean.valueOf(statusDb.getCurrentValue()) == status)
                                || statusDb == null || statusDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.OPENED.getName(), status.toString(), ValueType.BOOL);

                            broadcast("event.device.doorsensor." + data.get("status").getAsString(), new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.OPENED.getName(),
                                    status.toString(),
                                    ValueType.BOOL)
                            );

                            logger.info("Channel: {} Door sensor state is {}", notification.getSid(),
                                        status ? "open" : "closed");
                        }
                    }

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.VOLTAGE.getName(),
                                    voltage.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }
                }
                break;
            case SWITCH:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage",
                                    DeviceCommandEvent.builder()
                                            .channel(device.getChannel())
                                            .protocol(SourceProtocol.XIAOMI)
                                            .eventLabel(StandartDeviceValueLabel.VOLTAGE.getName())
                                            .data(new DataLevel(voltage.toString(), ValueType.DOUBLE))
                                            .build()
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }

                    if (data.has("status")) {
                        String status = data.get("status").getAsString();
                        DeviceValue statusDb = device.getValues().get(StandartDeviceValueLabel.STATUS.getName());

                        if ((statusDb != null && statusDb.getCurrentValue() != null && !statusDb.getCurrentValue().equals(status))
                                || statusDb == null || statusDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.STATUS.getName(), status, ValueType.STRING);

                            broadcast("event.device.button", new DeviceChangeEvent(
                                        device.getChannel(),
                                        SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.STATUS.getName(),
                                    status,
                                    ValueType.STRING)
                                );

                            logger.info("Channel: {} Button status: {}", notification.getSid(), status);
                            }
                    }
                }
                break;
            case SENSOR_AQARA_FLOOD:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.VOLTAGE.getName(),
                                    voltage.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }

                    if (data.has("status")) {
                        DeviceValue leakDb = device.getValues().get(StandartDeviceValueLabel.LEAK.getName());
                        Boolean leak;

                        if (data.get("status").getAsString().equals("leak")) {
                            leak = true;
                            if ((leakDb != null && leakDb.getCurrentValue() != null && !Objects.equals(Boolean.valueOf(leakDb.getCurrentValue()), true))
                                    || leakDb == null || leakDb.getCurrentValue() == null) {
                                registry.addChange(device, StandartDeviceValueLabel.LEAK.getName(), leak.toString(), ValueType.BOOL);

                                broadcast("event.device.leak", new DeviceChangeEvent(
                                        device.getChannel(),
                                        SourceProtocol.XIAOMI,
                                        StandartDeviceValueLabel.LEAK.getName(),
                                        leak.toString(),
                                        ValueType.BOOL)
                                );

                                logger.info("Channel: {} Leak detected", notification.getSid());
                            }
                        } else {
                            leak = false;
                            if ((leakDb != null && leakDb.getCurrentValue() != null && !Objects.equals(Boolean.valueOf(leakDb.getCurrentValue()), false))
                                    || leakDb == null || leakDb.getCurrentValue() == null) {
                                registry.addChange(device, StandartDeviceValueLabel.LEAK.getName(), leak.toString(), ValueType.BOOL);

                                broadcast("event.device.leak", new DeviceChangeEvent(
                                        device.getChannel(),
                                        SourceProtocol.XIAOMI,
                                        StandartDeviceValueLabel.LEAK.getName(),
                                        leak.toString(),
                                        ValueType.BOOL)
                                );
                                logger.info("Channel: {} Leak gone", notification.getSid());
                            }
                        }
                    }
                }
                break;
            case SWITCH_AQARA_ZERO_1BUTTON:
            case SWITCH_AQARA_1BUTTON:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get(StandartDeviceValueLabel.LEVEL.getName()).getAsString()).getAsJsonObject();

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.VOLTAGE.getName(),
                                    voltage.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch0Db = device.getValues().get(StandartDeviceValueLabel.LEVEL.getName());

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || ch0Db == null || ch0Db.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL.getName(), ch0, ValueType.BYTE);

                            broadcast("event.device." + data.get("channel_0").getAsString(), new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.LEVEL.getName(),
                                    ch0,
                                    ValueType.BYTE)
                            );

	                          logger.info("Channel: {} Light is {}", notification.getSid(),
	                                      data.get("channel_0").getAsString());
                        }
                    }
                }

                break;
            case SWITCH_AQARA_ZERO_2BUTTONS:
            case SWITCH_AQARA_2BUTTONS:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("voltage")) {
                        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
                        Double voltage = data.get("voltage").getAsDouble() / 1000D;
                        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

                        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
                                || voltageDb == null || voltageDb.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

                            broadcast("event.device.voltage", new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.VOLTAGE.getName(),
                                    voltage.toString(),
                                    ValueType.DOUBLE)
                            );

                            logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
                        }
                    }

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch0Db = device.getValues().get(StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_1.getName());

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || ch0Db == null || ch0Db.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_1.getName(), ch0, ValueType.BYTE);

                            broadcast("event.device." + data.get("channel_0").getAsString(), new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.LEVEL.getName(),
                                    ch0,
                                    ValueType.BYTE)
                            );

	                          logger.info("Channel: {}, subchannel 1: Light is {}", notification.getSid(),
	                                    data.get("channel_0").getAsString());
                        }
                    }

                    if (data.has("channel_1")) {
                        String ch1 = data.get("channel_1").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch1Db = device.getValues().get(StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_2.getName());

                        if ((ch1Db != null && ch1Db.getCurrentValue() != null && !ch1Db.getCurrentValue().equals(ch1))
                                || ch1Db == null || ch1Db.getCurrentValue() == null) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_2.getName(), ch1, ValueType.BYTE);

                            broadcast("event.device." + data.get("channel_1").getAsString(), new DeviceChangeEvent(
                                    device.getChannel(),
                                    SourceProtocol.XIAOMI,
                                    StandartDeviceValueLabel.LEVEL.getName(),
                                    ch1,
                                    ValueType.BYTE)
                            );

                            logger.info("Channel: {}, subchannel 2: Light is {}", notification.getSid(),
                                    data.get("channel_1").getAsString());
                        }
                    }
                }
                break;
	        case SENSOR_MOTION:
	        case SENSOR_AQUARA_MOTION:
		        message = notification.getRawMessage();

		        if (message.has("data")) {
			        JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

			        if (data.has("lux")) {
				        Integer lux = data.get("lux").getAsInt();
				        DeviceValue luxDb = device.getValues().get(StandartDeviceValueLabel.ILLUMINANCE.getName());

				        if ((luxDb != null && luxDb.getCurrentValue() != null && !Objects.equals(Integer.valueOf(luxDb.getCurrentValue()), lux))
				            || luxDb == null || luxDb.getCurrentValue() == null) {
					        registry.addChange(device, StandartDeviceValueLabel.ILLUMINANCE.getName(), lux.toString(), ValueType.INT);

					        broadcast("event.device.illuminance", new DeviceChangeEvent(
							        device.getChannel(),
							        SourceProtocol.XIAOMI,
							        StandartDeviceValueLabel.ILLUMINANCE.getName(),
							        lux.toString(),
							        ValueType.INT)
					        );

					        logger.info("Channel: {} Illuminance: {} lux", notification.getSid(), lux);
				        }
			        }

			        if (data.has("status")) {
				        Boolean motion = data.get("status").getAsString().equals("motion");
				        DeviceValue motionDb = device.getValues().get(StandartDeviceValueLabel.MOTION.getName());

				        if ((motionDb != null && motionDb.getCurrentValue() != null && !Objects.equals(Boolean.valueOf(motionDb.getCurrentValue()), motion))
				            || motionDb == null || motionDb.getCurrentValue() == null) {
					        registry.addChange(device, StandartDeviceValueLabel.MOTION.getName(), motion.toString(), ValueType.BOOL);

					        broadcast("event.device.motion", new DeviceChangeEvent(
							        device.getChannel(),
							        SourceProtocol.XIAOMI,
							        StandartDeviceValueLabel.MOTION.getName(),
							        motion.toString(),
							        ValueType.BOOL)
					        );

					        logger.info("Channel: {} Motion detected", notification.getSid());
				        }
			        }

			        if (data.has("voltage")) {
				        checkBatteryLevelFromVoltage(device.getChannel(), data.get("voltage").getAsInt());
				        Double voltage = data.get("voltage").getAsDouble() / 1000D;
				        DeviceValue voltageDb = device.getValues().get(StandartDeviceValueLabel.VOLTAGE.getName());

				        if ((voltageDb != null && voltageDb.getCurrentValue() != null && !Objects.equals(Double.valueOf(voltageDb.getCurrentValue()), voltage))
				            || voltageDb == null || voltageDb.getCurrentValue() == null) {
					        registry.addChange(device, StandartDeviceValueLabel.VOLTAGE.getName(), voltage.toString(), ValueType.DOUBLE);

					        broadcast("event.device.voltage", new DeviceChangeEvent(
							        device.getChannel(),
							        SourceProtocol.XIAOMI,
							        StandartDeviceValueLabel.VOLTAGE.getName(),
							        voltage.toString(),
							        ValueType.DOUBLE)
					        );

					        logger.info("Channel: {} Voltage {}V", notification.getSid(), voltage);
				        }
			        }
		        }

		        break;
            default:
                //skip
        }
    }


    @Scheduled(fixedDelay = 3600000, initialDelay = 10000)
    void searchDevices() {
        gateway.discoverItems();
    }

    private void checkBatteryLevelFromVoltage(String channel, Integer voltage) {
        voltage = Math.min(VOLTAGE_MAX_MILLIVOLTS, voltage);
        voltage = Math.max(VOLTAGE_MIN_MILLIVOLTS, voltage);
        Integer battLevel = (int) ((float) (voltage - VOLTAGE_MIN_MILLIVOLTS)
                / (float) (VOLTAGE_MAX_MILLIVOLTS - VOLTAGE_MIN_MILLIVOLTS) * 100);

        if (battLevel <= BATT_LEVEL_LOW) {
            broadcast("event.device.battery", new DeviceProtocolEvent(channel, SourceProtocol.XIAOMI, BATTERY_LOW.getName()));
            logger.info("Channel: {} Battery low", channel);
        }
    }

    @Getter
    @Setter
    @ToString
    private class GatewayController {
        private String serial;
        private String encryptionKey;
    }
}
