package ru.iris.protocol.noolite;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.bus.devices.DeviceProtocolEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.protocol.enums.*;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.noolite4j.receiver.RX2164;
import ru.iris.noolite4j.watchers.BatteryState;
import ru.iris.noolite4j.watchers.Notification;
import ru.iris.noolite4j.watchers.SensorType;
import ru.iris.noolite4j.watchers.Watcher;

import static ru.iris.commons.protocol.enums.DeviceType.TEMP_HUMI_SENSOR;

@Component
@Profile("noolite")
@Qualifier("nooliterx")
@Slf4j
public class NooliteRXController extends AbstractProtocolService {

    private final EventBus r;
    private final ConfigLoader config;
    private final DeviceRegistry registry;
    private RX2164 rx;

    @Autowired
    public NooliteRXController(EventBus r,
                               ConfigLoader config,
                               DeviceRegistry registry) {
        this.r = r;
        this.config = config;
        this.registry = registry;
    }

    @Override
    public void onStartup() {
        logger.info("NooliteRXController started");
        if (!config.loadPropertiesFormCfgDirectory("noolite"))
            logger.error("Cant load noolite-specific configs. Check noolite.property if exists");
    }

    @Override
    public void onShutdown() {
        logger.info("NooliteRXController stopping");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("command.device");
        addSubscription("event.device");
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
                    case BIND_RX:
                        logger.debug("Get BindRXChannel advertisement (channel {})", n.getChannel());
                        logger.info("Binding device to RX channel {}", n.getChannel());
                        rx.bindChannel(Byte.valueOf(n.getChannel()));
                        break;
                    case UNBIND_RX:
                        logger.debug("Get UnbindRXChannel advertisement (channel {})", n.getChannel());
                        logger.info("Unbinding device from RX channel {}", n.getChannel());
                        rx.unbindChannel(Byte.valueOf(n.getChannel()));
                        break;
                    case UNBIND_ALL_RX:
                        logger.debug("Get UnbindAllRXChannel advertisement");
                        logger.info("Unbinding all RX channels");
                        rx.unbindAllChannels();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    @Async
    public void run() {
        try {
            rx = new RX2164();
            rx.open();

            Watcher watcher = this::doWork;

            rx.addWatcher(watcher);
            rx.start();
        } catch (Throwable t) {
            logger.error("Noolite RX error!");
            t.printStackTrace();
        }
    }

    private void doWork(Notification notification) {
        String channel = String.valueOf(notification.getChannel());
        SensorType sensor = (SensorType) notification.getValue("sensortype");

        logger.debug("Message to RX from channel " + channel);

        Device device = registry.getDevice(SourceProtocol.NOOLITE, channel);

        if (device == null) {
            device = new Device();
            device.setSource(SourceProtocol.NOOLITE);
            device.setHumanReadable("noolite/channel/" + channel);
            device.setState(State.ACTIVE);
            device.setType(DeviceType.UNKNOWN);
            device.setManufacturer("Nootechnika");
            device.setChannel(channel);

            // device is sensor
            if (sensor != null) {
                switch (sensor) {
                    case PT111:
                        device.setType(TEMP_HUMI_SENSOR);
                        device.setProductName("PT111");
                        break;
                    case PT112:
                        device.setType(DeviceType.TEMP_SENSOR);
                        device.setProductName("PT112");
                        break;
                    default:
                        device.setType(DeviceType.UNKNOWN_SENSOR);
                }
            }

            device = registry.addOrUpdateDevice(device);
            broadcast("event.device.added", new DeviceProtocolEvent(channel, SourceProtocol.NOOLITE, "DeviceAdded"));
        }

        // turn off
        switch (notification.getType()) {
            case TURN_OFF:
                logger.info("Channel {}: Got OFF command", channel);

                // device product name unkown
                if (StringUtils.isEmpty(device.getProductName())) {
                    device.setProductName("Generic Switch");
                    device.setType(DeviceType.BINARY_SWITCH);
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_OFF.getValue(),
                        ValueType.BYTE
                );

                broadcast("event.device.off", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_OFF.getValue(),
                        ValueType.BYTE)
                );
                break;

            case SLOW_TURN_OFF:
                logger.info("Channel {}: Got DIM command", channel);

                // device product name unkown
	              if (StringUtils.isEmpty(device.getProductName())) {
                    device.setProductName("Generic Switch");
                    device.setType(DeviceType.BINARY_SWITCH);
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_OFF.getValue(),
                        ValueType.BYTE
                );

                broadcast("event.device.dim", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_OFF.getValue(),
                        ValueType.BYTE)
                );
                break;

            case TURN_ON:
                logger.info("Channel {}: Got ON command", channel);

                // device product name unkown
	              if (StringUtils.isEmpty(device.getProductName())) {
                    device.setProductName("Generic Switch");
                    device.setType(DeviceType.BINARY_SWITCH);
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_ON.getValue(),
                        ValueType.BYTE
                );

                broadcast("event.device.on", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_ON.getValue(),
                        ValueType.BYTE)
                );

                break;

            case SLOW_TURN_ON:
                logger.info("Channel {}: Got BRIGHT command", channel);
                // we only know, that the user hold ON button

                // device product name unkown
	              if (StringUtils.isEmpty(device.getProductName())) {
                    device.setProductName("Generic Switch");
                    device.setType(DeviceType.BINARY_SWITCH);
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_ON.getValue(),
                        ValueType.BYTE
                );

                broadcast("event.device.bright", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        StandartDeviceValue.FULL_ON.getValue(),
                        ValueType.BYTE)
                );

                break;

            case SET_LEVEL:
                logger.info("Channel {}: Got SETLEVEL command.", channel);

                // device product name unkown
	              if (StringUtils.isEmpty(device.getProductName()) || device.getType().equals(DeviceType.BINARY_SWITCH)) {
                    device.setProductName("Generic Dimmer");
                    device.setType(DeviceType.MULTILEVEL_SWITCH);
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        notification.getValue(StandartDeviceValueLabel.LEVEL.getName()).toString(),
                        ValueType.BYTE
                );

                broadcast("event.device.level", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.LEVEL.getName(),
                        notification.getValue(StandartDeviceValueLabel.LEVEL.getName()).toString(),
                        ValueType.BYTE)
                );

                break;
            case STOP_DIM_BRIGHT:
                logger.info("Channel {}: Got STOPDIMBRIGHT command.", channel);

                broadcast("event.device.stopdimbright", new DeviceProtocolEvent(channel, SourceProtocol.NOOLITE, EventLabel.STOP_DIM_BRIGHT.getName()));
                break;

            case TEMP_HUMI:
                BatteryState battery = (BatteryState) notification.getValue(StandartDeviceValueLabel.BATTERY.getName());
                logger.info("Channel {}: Got TEMP_HUMI command.", channel);

                ru.iris.commons.protocol.enums.BatteryState batteryState;

                switch (battery) {
                    case OK:
                        batteryState = ru.iris.commons.protocol.enums.BatteryState.OK;
                        break;
                    case REPLACE:
                        batteryState = ru.iris.commons.protocol.enums.BatteryState.LOW;
                        break;
                    default:
                        batteryState = ru.iris.commons.protocol.enums.BatteryState.UNKNOWN;
                }

                logger.info("Channel {}: Battery: {}", channel, batteryState);
                logger.info("Channel {}: Temperature: {}C", channel, notification.getValue("temp"));

                if (device.getType().equals(TEMP_HUMI_SENSOR)) {
                    logger.info("Channel {}: Humidity: {}%", channel, notification.getValue("humi"));
                }

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.TEMPERATURE.getName(),
                        notification.getValue("temp").toString(),
                        ValueType.DOUBLE
                );

                registry.addChange(
                        device,
                        StandartDeviceValueLabel.BATTERY.getName(),
                        batteryState.name(),
                        ValueType.STRING
                );

                broadcast("event.device.temperature", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.TEMPERATURE.getName(),
                        notification.getValue("temp").toString(),
                        ValueType.DOUBLE)
                );

                broadcast("event.device.battery", new DeviceChangeEvent(
                        channel,
                        SourceProtocol.NOOLITE,
                        StandartDeviceValueLabel.BATTERY.getName(),
                        batteryState.toString(),
                        ValueType.STRING)
                );

                if (device.getType().equals(TEMP_HUMI_SENSOR)) {
                    registry.addChange(
                            device,
                            StandartDeviceValueLabel.HUMIDITY.getName(),
                            notification.getValue("humi").toString(),
                            ValueType.DOUBLE
                    );

                    broadcast("event.device.humidity", new DeviceChangeEvent(
                            channel,
                            SourceProtocol.NOOLITE,
                            StandartDeviceValueLabel.HUMIDITY.getName(),
                            notification.getValue("humi").toString(),
                            ValueType.BYTE
                    ));
                }

                break;

            case BATTERY_LOW:
                logger.info("Channel {}: Got BATTERYLOW command.", channel);

                if (device.getType().equals(DeviceType.BINARY_SWITCH)) {
                    device.setType(DeviceType.MOTION_SENSOR);
                    device.setProductName("PM111");
                    registry.addOrUpdateDevice(device);
                }

                broadcast("event.device.battery", new DeviceProtocolEvent(channel, SourceProtocol.NOOLITE, "BatteryLow"));
                break;

            default:
                logger.info("Unknown command: {}", notification.getType().name());
        }
    }


}
