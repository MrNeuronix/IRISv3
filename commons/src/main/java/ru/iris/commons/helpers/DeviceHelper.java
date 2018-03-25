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

    public void on(String ident) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.TURN_ON)));
        }
    }

    public void off(String ident) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.TURN_OFF)));
        }
    }

    public void level(String ident, String level) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), EventLabel.SET_LEVEL, level)));
        }
    }

    public void on(String ident, int subchannel) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.TURN_ON)));
        }
    }

    public void off(String ident, int subchannel) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.TURN_OFF)));
        }
    }

    public void level(String ident, int subchannel, String level) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), subchannel, device.getSource(), EventLabel.SET_LEVEL, level)));
        }
    }

}
