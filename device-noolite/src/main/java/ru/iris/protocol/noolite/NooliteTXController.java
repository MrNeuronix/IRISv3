package ru.iris.protocol.noolite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.noolite4j.sender.PC1132;

@Component
@Profile("noolite")
@Qualifier("noolitetx")
@Slf4j
public class NooliteTXController extends AbstractProtocolService {

    private final ConfigLoader config;
    private PC1132 pc;

    @Autowired
    public NooliteTXController(ConfigLoader config) {
        this.config = config;
    }

    @Override
    public void onStartup() {
        logger.info("NooliteRXController started");
        if (!config.loadPropertiesFormCfgDirectory("noolite"))
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
    public void subscribe() throws Exception {
        addSubscription("command.device");
    }

    @Override
    public void run() {

    }

    @Override
    public Consumer<Event<?>> handleMessage() {
        return event -> {
            if (event.getData() instanceof DeviceCommandEvent) {

                DeviceCommandEvent n = (DeviceCommandEvent) event.getData();

                if (!n.getProtocol().equals(SourceProtocol.NOOLITE)) {
                    return;
                }

                switch (n.getLabel()) {
                    case "TurnOn":
                        logger.info("Turn ON device on channel {}", n.getChannel());
                        pc.turnOn(n.getChannel().byteValue());
                        broadcast("command.device.on", new DeviceChangeEvent(n.getChannel(), SourceProtocol.NOOLITE, "level", 255, ValueType.INT));
                        break;
                    case "TurnOff":
                        logger.info("Turn OFF device on channel {}", n.getChannel());
                        pc.turnOff(n.getChannel().byteValue());
                        broadcast("command.device.off", new DeviceChangeEvent(n.getChannel(), SourceProtocol.NOOLITE, "level", 0, ValueType.INT));
                        break;
                    case "SetLevel":
                        logger.info("Set level {} on channel {}", n.getTo(), n.getChannel());
                        pc.setLevel(n.getChannel().byteValue(), ((Short) n.getTo()).byteValue());
                        broadcast("command.device.level", new DeviceChangeEvent(n.getChannel(), SourceProtocol.NOOLITE, "level", n.getTo(), ValueType.INT));
                        break;
                    case "BindTX":
                        logger.info("Incoming bind TX to channel {} request", n.getChannel());
                        pc.bindChannel(n.getChannel().byteValue());
                        break;
                    case "UnbindTX":
                        logger.info("Incoming unbind TX from channel {} request", n.getChannel());
                        pc.unbindChannel(n.getChannel().byteValue());
                        break;
                    default:
                        logger.info("Received unknown request for noolitetx service! Class: {}", event.getData().getClass());
                        break;
                }
            }
        };
    }
}