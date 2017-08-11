package ru.iris.protocol.xiaomi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.springframework.scheduling.annotation.Async;
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
public class XiaomiController extends AbstractProtocolService {
    private final EventBus r;
    private final ConfigLoader config;
    private final DeviceRegistry registry;
    private final Gson gson = new GsonBuilder().create();
    private List<GatewayController> gateways;

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
            logger.error("Cant load xiaomi-specific configs. Check zwave.property if exists");

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

                switch (x.getLabel()) {
                    case "TurnOn":
                        logger.info("Turn ON device on channel {}", x.getChannel());
                        broadcast("event.device.on", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "Level", 255, ValueType.INT));
                        break;
                    case "TurnOff":
                        logger.info("Turn OFF device on channel {}", x.getChannel());
                        broadcast("event.device.off", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "Level", 0, ValueType.INT));
                        break;
                    case "SetLevel":
                        logger.info("Set level {} on channel {}", x.getTo(), x.getChannel());
                        broadcast("event.device.level", new DeviceChangeEvent(x.getChannel(), SourceProtocol.XIAOMI, "Level", x.getTo(), ValueType.INT));
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
        logger.info("Gateways: {}", gateways);
    }

    @Getter
    @Setter
    @ToString
    private class GatewayController {
        private Inet4Address host;
        private Integer port;
        private Long serial;
        private String encryptionKey;
    }
}
