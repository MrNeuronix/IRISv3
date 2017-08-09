package ru.iris.commons.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.database.model.Device;
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
                    device.getChannel(), device.getSource(), "TurnOn")));
        }
    }

    public void off(String ident) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), "TurnOff")));
        }
    }

    public void level(String ident, Object level) {
        Device device = deviceRegistry.getDevice(ident);
        if (device != null) {
            r.notify("command.device", Event.wrap(new DeviceCommandEvent(
                    device.getChannel(), device.getSource(), "SetLevel", level)));
        }
    }

}
