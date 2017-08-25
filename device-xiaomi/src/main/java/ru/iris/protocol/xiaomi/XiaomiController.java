package ru.iris.protocol.xiaomi;

import com.google.gson.*;
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

                switch (EventLabel.valueOf(x.getEventLabel())) {
                    case TURN_ON:
                        if (x.getClazz().equals(DataLevel.class)) {
                            logger.info("Turn ON device on channel {}", x.getChannel());
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"on"});
                            broadcast(
                                    "event.device.on",
                                    new DeviceChangeEvent(
                                            x.getChannel(),
                                            SourceProtocol.XIAOMI,
                                            StandartDeviceValueLabel.LEVEL.getName(),
                                            StandartDeviceValue.FULL_ON.getValue(),
                                            ValueType.BYTE));
                        } else if (x.getClazz().equals(DataSubChannelLevel.class)) {
                            DataSubChannelLevel data = (DataSubChannelLevel) x.getData();
                            logger.info("Turn ON device on channel {}, subchannel: {}", x.getChannel(), data.getSubChannel());
                            int subchannel = data.getSubChannel() - 1;
                            gateway.writeToDevice(x.getChannel(), new String[]{"channel_" + subchannel}, new String[]{"on"});
                            broadcast(
                                    "event.device.on",
                                    new DeviceChangeEvent(
                                            x.getChannel(),
                                            SourceProtocol.XIAOMI,
                                            StandartDeviceValueLabel.LEVEL.getName(),
                                            String.valueOf(subchannel),
                                            StandartDeviceValue.FULL_ON.getValue(),
                                            ValueType.BYTE));
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

            switch (notification.getType()) {
                case GATEWAY:
                case BRIDGE:
                    device.setType(DeviceType.CONTROLLER);
                    device.setProductName("Mi Gateway");
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
                default:
                    device.setProductName("Unknown device");
                    device.setType(DeviceType.UNKNOWN);
            }

            device = registry.addOrUpdateDevice(device);
        }

        JsonObject message;

        switch (notification.getType()) {
            case SENSOR_AQARA_MAGNET:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("status")) {
                        Boolean status = data.get("status").getAsString().equals("open");
                        DeviceValue statusDb = device.getValues().get("status");

                        if ((statusDb != null && statusDb.getCurrentValue() != null && !Boolean.valueOf(statusDb.getCurrentValue()) == status)
                                || (statusDb == null || statusDb.getCurrentValue() == null)) {
                            registry.addChange(device, StandartDeviceValueLabel.OPENED.getName(), status.toString(), ValueType.BOOL);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_ZERO_1BUTTON:
            case SWITCH_AQARA_1BUTTON:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get(StandartDeviceValueLabel.LEVEL.getName()).getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch0Db = device.getValues().get(StandartDeviceValueLabel.LEVEL.getName());

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL.getName(), ch0, ValueType.BYTE);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_ZERO_2BUTTONS:
            case SWITCH_AQARA_2BUTTONS:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch0Db = device.getValues().get(StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_1.getName());

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_1.getName(), ch0, ValueType.BYTE);
                        }
                    }

                    if (data.has("channel_1")) {
                        String ch1 = data.get("channel_1").getAsString().equals("on")
                                ? StandartDeviceValue.FULL_ON.getValue() : StandartDeviceValue.FULL_OFF.getValue();
                        DeviceValue ch1Db = device.getValues().get(StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_2.getName());

                        if ((ch1Db != null && ch1Db.getCurrentValue() != null && !ch1Db.getCurrentValue().equals(ch1))
                                || (ch1Db == null || ch1Db.getCurrentValue() == null)) {
                            registry.addChange(device, StandartDeviceValueLabel.LEVEL_ON_SUBCHANNEL_2.getName(), ch1, ValueType.BYTE);
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

    @Getter
    @Setter
    @ToString
    private class GatewayController {
        private String serial;
        private String encryptionKey;
    }
}
