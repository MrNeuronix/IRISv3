package ru.iris.commons.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.EventLabel;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.models.protocol.enums.SourceProtocol;

@Component
@Slf4j
public class DeviceHelper {

    private final EventBus r;
    private final DeviceRegistry deviceRegistry;

    @Autowired
    public DeviceHelper(EventBus r, DeviceRegistry deviceRegistry) {
        this.r = r;
        this.deviceRegistry = deviceRegistry;
    }

    public void on(SourceProtocol protocol, String channel) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.TURN_ON)));
        }
    }

    public void off(SourceProtocol protocol, String channel) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.TURN_OFF)));
        }
    }

    public void level(SourceProtocol protocol, String channel, String level) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.SET_LEVEL, level)));
        }
    }

    public void on(SourceProtocol protocol, String channel, int subchannel) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.TURN_ON)));
        }
    }

    public void off(SourceProtocol protocol, String channel, int subchannel) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.TURN_OFF)));
        }
    }

    public void level(SourceProtocol protocol, String channel, int subchannel, String level) {
        Device device = deviceRegistry.getDevice(protocol, channel);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.SET_LEVEL, level)));
        }
    }

}
