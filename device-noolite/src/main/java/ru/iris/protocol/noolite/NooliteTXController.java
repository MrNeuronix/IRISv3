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
import ru.iris.commons.protocol.data.DataLevel;
import ru.iris.commons.protocol.enums.*;
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

                switch (EventLabel.parse(n.getEventLabel())) {
                    case TURN_ON:
                        logger.info("Turn ON device on channel {}", n.getChannel());
                        pc.turnOn(Byte.valueOf(n.getChannel()));
                        broadcast("command.device.on", new DeviceChangeEvent(
                                n.getChannel(),
                                SourceProtocol.NOOLITE,
                                StandartDeviceValueLabel.LEVEL.getName(),
                                StandartDeviceValue.FULL_ON.getValue(),
                                ValueType.BYTE)
                        );
                        break;
                    case TURN_OFF:
                        logger.info("Turn OFF device on channel {}", n.getChannel());
                        pc.turnOff(Byte.valueOf(n.getChannel()));
                        broadcast("command.device.off", new DeviceChangeEvent(
                                n.getChannel(),
                                SourceProtocol.NOOLITE,
                                StandartDeviceValueLabel.LEVEL.getName(),
                                StandartDeviceValue.FULL_OFF.getValue(),
                                ValueType.BYTE)
                        );
                        break;
                    case SET_LEVEL:
                        if (n.getClazz().equals(DataLevel.class)) {
                            DataLevel data = (DataLevel) n.getData();
                            logger.info("Set level {} on channel {}", data.getTo(), n.getChannel());
                            pc.setLevel(Byte.valueOf(n.getChannel()), (Short.valueOf(data.getTo())).byteValue());
                            broadcast("command.device.level", new DeviceChangeEvent(
                                    n.getChannel(),
                                    SourceProtocol.NOOLITE,
                                    StandartDeviceValueLabel.LEVEL.getName(),
                                    data.getTo(),
                                    ValueType.BYTE)
                            );
                        }
                        break;
                    case BIND_TX:
                        logger.info("Incoming bind TX to channel {} request", n.getChannel());
                        pc.bindChannel(Byte.valueOf(n.getChannel()));
                        break;
                    case UNBIND_TX:
                        logger.info("Incoming unbind TX from channel {} request", n.getChannel());
                        pc.unbindChannel(Byte.valueOf(n.getChannel()));
                        break;
                    default:
                        logger.info("Received unknown request for noolitetx service! Class: {}", event.getData().getClass());
                        break;
                }
            }
        };
    }
}
