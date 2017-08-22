package ru.iris.protocol.xiaomi;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.xiaomi4j.Discovery;
import ru.iris.xiaomi4j.Gateway;
import ru.iris.xiaomi4j.model.GatewayModel;
import ru.iris.xiaomi4j.watchers.Notification;
import ru.iris.xiaomi4j.watchers.Watcher;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

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

                switch (x.getLabel()) {
                    case "TurnOn":
                        logger.info("Turn ON device on channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"on"});
                        broadcast("event.device.on", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level", 255, ValueType.INT));
                        break;
                    case "TurnOff":
                        logger.info("Turn OFF device on channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"off"});
                        broadcast("event.device.off", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level", 0, ValueType.INT));
                        break;
                    case "TurnChannel1On":
                        logger.info("Turn ON device on first channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"on"});
                        broadcast("event.device.on", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level1", 255, ValueType.INT));
                        break;
                    case "TurnChannel1Off":
                        logger.info("Turn OFF device on first channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_0"}, new String[]{"off"});
                        broadcast("event.device.off", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level1", 0, ValueType.INT));
                        break;
                    case "TurnChannel2On":
                        logger.info("Turn ON device on second channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_1"}, new String[]{"on"});
                        broadcast("event.device.on", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level2", 255, ValueType.INT));
                        break;
                    case "TurnChannel2Off":
                        logger.info("Turn OFF device on second channel {}", x.getChannel());
                        gateway.writeToDevice(x.getChannel(), new String[]{"channel_1"}, new String[]{"off"});
                        broadcast("event.device.off", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "level2", 0, ValueType.INT));
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
                        String status = data.get("status").getAsString();
                        DeviceValue statusDb = device.getValues().get("status");

                        if ((statusDb != null && statusDb.getCurrentValue() != null && !statusDb.getCurrentValue().equals(status))
                                || (statusDb == null || statusDb.getCurrentValue() == null)) {
                            registry.addChange(device, "status",
                                    status.equals("open") ? "true" : "false",
                                    ValueType.BOOL);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_1BUTTON:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString();
                        DeviceValue ch0Db = device.getValues().get("level");

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level",
                                    data.get("channel_0").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_2BUTTONS:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString();
                        DeviceValue ch0Db = device.getValues().get("level1");

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level1",
                                    data.get("channel_0").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
                        }
                    }

                    if (data.has("channel_1")) {
                        String ch1 = data.get("channel_1").getAsString();
                        DeviceValue ch1Db = device.getValues().get("level2");

                        if ((ch1Db != null && ch1Db.getCurrentValue() != null && !ch1Db.getCurrentValue().equals(ch1))
                                || (ch1Db == null || ch1Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level2",
                                    data.get("channel_1").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_ZERO_1BUTTON:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString();
                        DeviceValue ch0Db = device.getValues().get("level");

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level",
                                    data.get("channel_0").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
                        }
                    }
                }

                break;
            case SWITCH_AQARA_ZERO_2BUTTONS:
                message = notification.getRawMessage();

                if (message.has("data")) {
                    JsonObject data = PARSER.parse(message.get("data").getAsString()).getAsJsonObject();

                    if (data.has("channel_0")) {
                        String ch0 = data.get("channel_0").getAsString();
                        DeviceValue ch0Db = device.getValues().get("level1");

                        if ((ch0Db != null && ch0Db.getCurrentValue() != null && !ch0Db.getCurrentValue().equals(ch0))
                                || (ch0Db == null || ch0Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level1",
                                    data.get("channel_0").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
                        }
                    }

                    if (data.has("channel_1")) {
                        String ch1 = data.get("channel_1").getAsString();
                        DeviceValue ch1Db = device.getValues().get("level2");

                        if ((ch1Db != null && ch1Db.getCurrentValue() != null && !ch1Db.getCurrentValue().equals(ch1))
                                || (ch1Db == null || ch1Db.getCurrentValue() == null)) {
                            registry.addChange(device, "level2",
                                    data.get("channel_1").getAsString().equals("on") ? "255" : "0",
                                    ValueType.BYTE);
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
