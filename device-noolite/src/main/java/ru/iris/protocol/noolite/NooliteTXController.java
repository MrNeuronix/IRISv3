package ru.iris.protocol.noolite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.fn.Consumer;
import ru.iris.models.bus.Queue;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.DeviceType;
import ru.iris.models.protocol.enums.EventLabel;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.StandartDeviceValue;
import ru.iris.models.protocol.enums.StandartDeviceValueLabel;
import ru.iris.models.protocol.enums.State;
import ru.iris.models.protocol.enums.ValueType;
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
    private DeviceRegistry registry;

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
        addSubscription(Queue.COMMAND_DEVICE);
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

                String channel = n.getChannel();
	              Device device = registry.getDevice(SourceProtocol.NOOLITE, channel);

		            if (device == null) {
			            device = new Device();
			            device.setSource(SourceProtocol.NOOLITE);
			            device.setHumanReadable("noolite/channel/" + channel);
			            device.setState(State.ACTIVE);
			            device.setType(DeviceType.BINARY_SWITCH);
			            device.setManufacturer("Nootechnika");
			            device.setChannel(channel);

			            device = registry.addOrUpdateDevice(device);
			            broadcast(Queue.EVENT_DEVICE_ADDED, new DeviceProtocolEvent(channel, SourceProtocol.NOOLITE, "DeviceAdded"));
		            }

                switch (EventLabel.parse(n.getEventLabel())) {
                    case TURN_ON:
                        logger.info("Turn ON device on channel {}", n.getChannel());
                        pc.turnOn(Byte.valueOf(n.getChannel()));

		                    registry.addChange(
				                    device,
				                    StandartDeviceValueLabel.LEVEL.getName(),
				                    StandartDeviceValue.FULL_ON.getValue(),
				                    ValueType.BYTE
		                    );

                        broadcast(Queue.EVENT_DEVICE_ON, new DeviceChangeEvent(
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

		                    registry.addChange(
				                    device,
				                    StandartDeviceValueLabel.LEVEL.getName(),
				                    StandartDeviceValue.FULL_OFF.getValue(),
				                    ValueType.BYTE
		                    );

                        broadcast(Queue.EVENT_DEVICE_OFF, new DeviceChangeEvent(
                                n.getChannel(),
                                SourceProtocol.NOOLITE,
                                StandartDeviceValueLabel.LEVEL.getName(),
                                StandartDeviceValue.FULL_OFF.getValue(),
                                ValueType.BYTE)
                        );
                        break;
                    case SET_LEVEL:
                        if (n.getData() instanceof DataLevel) {
                            DataLevel data = (DataLevel) n.getData();
                            logger.info("Set level {} on channel {}", data.getTo(), n.getChannel());
                            pc.setLevel(Byte.valueOf(n.getChannel()), (Short.valueOf(data.getTo())).byteValue());

		                        registry.addChange(
				                        device,
				                        StandartDeviceValueLabel.LEVEL.getName(),
				                        data.getTo(),
				                        ValueType.BYTE
		                        );

                            broadcast(Queue.EVENT_DEVICE_LEVEL, new DeviceChangeEvent(
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
